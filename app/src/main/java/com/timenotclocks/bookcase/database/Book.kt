/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License
 Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing
 software
 * distributed under the License is distributed on an "AS IS" BASIS

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND
 either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.timenotclocks.bookcase.database

import android.widget.Button
import androidx.room.*
import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * TODO: too dumb to figure out how many to many works with Room
 */

// TODO: Add converter to Room data class
enum class ShelfType(val shelf: String) {
    CurrentShelf("currently-reading"), ReadShelf("read"), ToReadShelf("to-read")
}

enum class SortColumn(val column: String) {
    DateAdded("dateAdded"), DateRead("dateRead"), DateStarted("dateStarted"),
    Title("title"), Author("author"), Year("year")
}



@Entity(
        tableName = "books",
        indices = arrayOf(Index(value = ["title", "author"], unique = true)),
)
data class Book(  // TODO: can I remove overloads? i'ts for the converter
        @PrimaryKey(autoGenerate = true) var bookId: Long,
        @ColumnInfo(name = "title") var title: String,
        @ColumnInfo(name = "subtitle") var subtitle: String?,
        @ColumnInfo(name = "isbn10") var isbn10: String?,
        @ColumnInfo(name = "isbn13") var isbn13: String?,
        @ColumnInfo(name = "author") var author: String?,
        @ColumnInfo(name = "authorExtras") var authorExtras: String?,
        @ColumnInfo(name = "publisher") var publisher: String?,
        @ColumnInfo(name = "year") var year: Int?,
        @ColumnInfo(name = "originalYear") var originalYear: Int?,
        @ColumnInfo(name = "numberPages") var numberPages: Int?,
        @KlaxonDate @ColumnInfo(name = "dateAdded") var dateAdded: LocalDate?,
        @KlaxonDate @ColumnInfo(name = "dateStarted") var dateStarted: LocalDate?,
        @KlaxonDate @ColumnInfo(name = "dateRead") var dateRead: LocalDate?,
        // create column for every shelf added
        @ColumnInfo(name = "rating") var rating: Int?,
        @ColumnInfo(name = "shelf") var shelf: String,
        @ColumnInfo(name = "notes") var notes: String?
) {
    fun cover(size: String = "M"): String? {
        val isbn = isbn13 ?: isbn10
        if (isbn.isNullOrBlank()) {
            return null
        }
        return "https://covers.openlibrary.org/b/isbn/$isbn-$size.jpg?default=false"
    }

    fun titleString(): String {
        return subtitle?.let {
            sub -> "$title: $sub"
        } ?: title
    }
    fun yearString(): String? {
        if (year != null && originalYear != null) {
            return "$year ($originalYear)"
        }

        (year ?: originalYear)?.let {
            return it.toString()
        }

        return null
    }

    fun shelve(target: ShelfType, button: Button?, bookViewModel: BookViewModel?): Boolean {
        if (shelf != target.shelf) {
            shelf = target.shelf
            when (target) {
                ShelfType.ReadShelf -> {
                    dateRead = LocalDate.now()
                }
                ShelfType.CurrentShelf -> {
                    dateStarted = LocalDate.now()
                }
            }
            if (dateAdded == null) {
                dateAdded = LocalDate.now()
            }
            button?.text = shelf

            bookViewModel?.let{ it.update(this) }
        }
        return true
    }
}



@Target(AnnotationTarget.FIELD)
annotation class KlaxonDate

val dateConverter = object : Converter {
    var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun canConvert(cls: Class<*>) = cls == LocalDate::class.java

    override fun fromJson(jv: JsonValue): LocalDate? {
        if (jv.string != null) {
            return LocalDate.parse(jv.string, formatter)
        } else {
            return null
        }
    }

    override fun toJson(o: Any): String {
        return """ "$o" """
    }
}

inline infix fun <reified T : Any> T.merge(other: T): T {
    val propertiesByName = T::class.declaredMemberProperties.associateBy { it.name }
    val primaryConstructor = T::class.primaryConstructor
            ?: throw IllegalArgumentException("merge type must have a primary constructor")
    val args = primaryConstructor.parameters.associateWith { parameter ->
        val property = propertiesByName[parameter.name]
                ?: throw IllegalStateException("no declared member property found with name '${parameter.name}'")
        (property.get(this) ?: property.get(other))
    }
    return primaryConstructor.callBy(args)
}


fun mergeBooks(first: Book, second: Book): Book {
    var newBook = first merge second
    newBook.bookId = second.bookId
    return newBook
}

fun fakeBook(
        id: Long = 0, title: String = "Dark Money",
        author: String? = "Jane Mayer",
        authorExtras: String? = "edit. Someoneelse",
        isbn13: String? = "978-0-385-53559-5",
        year: Int? = null,
        publisher: String? = null,
): Book {
    return Book(
            bookId = id,
            title = title,
            subtitle = "How Subs Change Books",
            isbn10 = "0123456789",
            isbn13 = isbn13,
            author = author,
            authorExtras = authorExtras,
            publisher = publisher,
            year = year,
            originalYear = 2010,
            numberPages = 290,
            rating = null,
            shelf = "currently-reading",
            notes = null,
            dateAdded = LocalDate.now(),
            dateStarted = null,
            dateRead = null,
    )
}


// Full Text Search Table
@Fts4(contentEntity = Book::class)
@Entity(tableName = "books_fts")
class BooksFts(val bookId: Long, val title: String, val subtitle: String?, val author: String?, val shelf: String)
