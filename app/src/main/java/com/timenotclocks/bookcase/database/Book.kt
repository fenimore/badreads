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

import androidx.room.*
import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

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
data class Book(
        @PrimaryKey(autoGenerate = true) val bookId: Long,
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

val dateConverter = object: Converter {
    var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    //val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    override fun canConvert(cls: Class<*>)
            = cls == Date::class.java

    override fun fromJson(jv: JsonValue) =
            if (jv.string != null) {  // ugh
                LocalDate.parse(jv.string, formatter)
            } else {
                null
            }

    override fun toJson(o: Any)
                =
            """ { "date" : $o } """
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
