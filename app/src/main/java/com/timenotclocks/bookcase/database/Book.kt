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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * TODO: too dumb to figure out how many to many works with Room
 */

///notes: , dateAdded: Sat Jan 09 00:00:00 EST 2021,
// authorExtras: , year: 2019, author: Mark Miodownik,
// isbn13: 9780544850194,
// bookId: 4, title: Liquid Rules: The Delightful and Dangerous Substances That Flow Through Our Lives, rating: 0, numberPages: 256, originalYear: 2018, shelf: currently-reading, publisher: Houghton Mifflin Harcourt]

@Entity(
        tableName = "books",
        indices = arrayOf(Index(value = ["title", "author"], unique = true)),
)
data class Book @JvmOverloads constructor(  // TODO: can I remove overloads? i'ts for the converter
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
        @KlaxonDate @ColumnInfo(name = "dateRead") var dateRead: LocalDate?,
        @KlaxonDate @ColumnInfo(name = "dateAdded") var dateAdded: LocalDate?,
        @ColumnInfo(name = "rating") var rating: Int?,
        @ColumnInfo(name = "shelf") var shelf: String,
        @ColumnInfo(name = "notes") var notes: String?
)

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


fun mergeBooks(old: Book, new: Book): Book {
    return old merge new
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
            dateRead = null,
    )
}


//Book Id,Title,Author,Author l-f,Additional Authors,ISBN,ISBN13,My Rating,Average Rating,Publisher,Binding,Number of Pages,Year Published,Original Publication Year,Date Read,Date Added,Bookshelves,Bookshelves with positions,Exclusive Shelf,My Review,Spoiler,Private Notes,Read Count,Recommended For,Recommended By,Owned Copies,Original Purchase Date,Original Purchase Location,Condition,Condition Description,BCID

/*


@Entity(tableName = "authors")
 data class Author(
             @PrimaryKey(autoGenerate = true) val authorId: Long,
     val firstName: String,
     val lastName: String
 )
 
 @Entity(tableName = "publications")
 data class Publication(
             @PrimaryKey(autoGenerate = true) val publicationId: Long,
     val publisher: String,
     val year: Int,
     val originalYear: Int
 )
 
 
 @Entity(primaryKeys = ["authorId", "bookId"])
 data class AuthorCrossRef(
                 val authorId: Long,
                 val bookId: Long
         )
 
 @Entity(primaryKeys = ["publicationId", "bookId"])
 data class PublicationCrossRef(
                 val publicationId: Long,
                 val bookId: Long
         )
 
 data class BookWithAuthors(
                 @Embedded val book: Book,
                 @Relation(
                         parentColumn = "bookId",
                 entityColumn = "authorId",
                 associateBy = Junction(AuthorCrossRef::class)
         )
         val authors: List<Author>
 )
 
 data class AuthorWithBooks(
                 @Embedded val author: Author,
                 @Relation(
                         parentColumn = "authorId",
                 entityColumn = "bookId",
                 associateBy = Junction(AuthorCrossRef::class)
         )
         val books: List<Book>
 )
 
 data class BookWithPublications(
                 @Embedded val book: Book,
                 @Relation(
                         parentColumn = "bookId",
                 entityColumn = "publicationId",
                 associateBy = Junction(PublicationCrossRef::class)
         )
         val publication: List<Publication>
 )
*/
