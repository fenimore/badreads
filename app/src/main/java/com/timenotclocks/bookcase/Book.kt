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

package com.timenotclocks.bookcase

import androidx.room.*

/**
 * TODO: too dumb to figure out how many to many works with Room
 */

@Entity(tableName = "books")
data class Book(
        @PrimaryKey(autoGenerate = true) val bookId: Long,
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "cover_url") val coverUrl: String?,
        @ColumnInfo(name = "isbn") val isbn: Int,
        @ColumnInfo(name = "isbn13") val isbn13: Long,
        @ColumnInfo(name = "authorFirst") val authorFirst: String?,
        @ColumnInfo(name = "authorLast") val authorLast: String?,
        @ColumnInfo(name = "publisher") val publisher: String?,
        @ColumnInfo(name = "year") val year: Int?,
        @ColumnInfo(name = "originalYear") val originalYear: Int?,

        @ColumnInfo(name = "rating") val rating: Int?,
        @ColumnInfo(name = "shelf") val shelf: String,
        @ColumnInfo(name = "notes") val notes: String?
)


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
