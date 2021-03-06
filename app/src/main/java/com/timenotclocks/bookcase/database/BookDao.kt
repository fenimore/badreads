/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.timenotclocks.bookcase.database

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow


/**
 * The Room Magic is in this file, where you map a method call to an SQL query.
 *
 * When you are using complex data types, such as Date, you have to also supply type converters.
 * To keep this example basic, no types that require type converters are used.
 * See the documentation at
 * https://developer.android.com/topic/libraries/architecture/room.html#type-converters
 */

@Dao
public interface BookDao {

    // The flow always holds/caches latest version of data. Notifies its observers when the
    // data has changed.
    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    fun allBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE shelf = 'currently-reading' ORDER BY dateAdded ASC")
    fun currentShelf(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE shelf = :shelf ORDER BY :column DESC")
    fun sortShelf(shelf: String, column: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE shelf = :shelf ORDER BY dateAdded DESC")
    fun dateAddedSort(shelf: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE shelf = :shelf ORDER BY dateRead DESC")
    fun dateReadSort(shelf: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE shelf = :shelf ORDER BY dateStarted DESC")
    fun dateStartedSort(shelf: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE shelf = :shelf ORDER BY author ASC")
    fun authorSort(shelf: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE shelf = :shelf ORDER BY COALESCE(originalYear, year) ASC")
    fun yearSort(shelf: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE shelf = 'read' ORDER BY dateAdded DESC")
    fun readShelf(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE shelf = 'to-read' ORDER BY dateAdded DESC")
    fun toReadShelf(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE (title LIKE :title OR isbn10 LIKE :isbn10 OR isbn13 LIKE :isbn13) AND :bookId != bookId LIMIT 1")
    fun findAlike(bookId: Long, title: String, isbn10: String?, isbn13: String?): LiveData<Book>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBook(book: Book): Long

    @Delete
    suspend fun delete(book: Book)

    @Query("DELETE FROM books")
    suspend fun deleteAll()

    @Update
    suspend fun update(book: Book)

    @Query("SELECT * FROM books WHERE bookId=:id ")
    fun getBook(id: Long): LiveData<Book>

    @Query("SELECT * FROM books")
    fun getBooks(): Flow<List<Book>>

    @Query("""
        SELECT * FROM books 
        JOIN books_fts ON books.bookId == books_fts.bookId 
        WHERE books_fts MATCH :term 
        GROUP BY books.bookId
        """ )
    fun fullSearch(term: String): Flow<List<Book>>
}
