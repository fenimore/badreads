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

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.timenotclocks.bookcase.database.Book
import com.timenotclocks.bookcase.database.BookDao
import kotlinx.coroutines.flow.Flow

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
class BookRepository(private val bookDao: BookDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allBooks: Flow<List<Book>> = bookDao.allBooks()
    val readShelf: Flow<List<Book>> = bookDao.readShelf()
    val toReadShelf: Flow<List<Book>> = bookDao.toReadShelf()
    val currentShelf: Flow<List<Book>> = bookDao.currentShelf()

    val sortedCurrentShelf: Flow<List<Book>> = bookDao.dateStartedSort(ShelfType.CurrentShelf.shelf)
    val sortedReadShelf: Flow<List<Book>> = bookDao.dateReadSort(ShelfType.ReadShelf.shelf)
    val sortedToReadShelf: Flow<List<Book>> = bookDao.dateAddedSort(ShelfType.ToReadShelf.shelf)

/*    fun sortBy(sortingColumn: SortColumn, shelfType: ShelfType) {
        when (sortingColumn) {
            *//* If you're handling your DB operations with coroutines, this function
             * should be suspendable and you should set the value to allDecks
             * with postValue
             *//*
            SortColumn.DateStarted -> theShelf.value = bookDao.dateStartedSort(shelfType.shelf)
            SortColumn.DateRead -> theShelf.value = bookDao.dateReadSort(shelfType.shelf)
            SortColumn.DateAdded -> theShelf.value = bookDao.sortShelf(shelfType.shelf, sortingColumn.column)
            SortColumn.Author -> theShelf.value = bookDao.authorSort(shelfType.shelf)
            SortColumn.Year -> theShelf.value = bookDao.yearSort(shelfType.shelf)
        }
    }*/

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun insertBook(book: Book): Long {
        return bookDao.insertBook(book)
    }

    @WorkerThread
    suspend fun delete(book: Book) {
        bookDao.delete(book)
    }
    @WorkerThread
    suspend fun update(book: Book) {
        bookDao.update(book)
    }
    @WorkerThread
    suspend fun deleteAll() {
        bookDao.deleteAll()
    }

    fun getBook(id: Long): LiveData<Book> {
        return bookDao.getBook(id)
    }

    fun query(term: String): Flow<List<Book>> {
        return bookDao.fullSearch(term)
    }

    fun dateStartedSort(shelfType: ShelfType): Flow<List<Book>> {
        return bookDao.dateStartedSort(shelfType.shelf)
    }

    fun dateReadSort(shelfType: ShelfType): Flow<List<Book>> {
        return bookDao.dateReadSort(shelfType.shelf)
    }

    fun yearSort(shelfType: ShelfType): Flow<List<Book>> {
        return bookDao.yearSort(shelfType.shelf)
    }

    fun authorSort(shelfType: ShelfType): Flow<List<Book>> {
        return bookDao.authorSort(shelfType.shelf)
    }

    fun sortShelf(shelfType: ShelfType, sortColumn: SortColumn): Flow<List<Book>> {
        return bookDao.sortShelf(shelfType.shelf, sortColumn.column)
    }

    fun findAlike(bookId: Long, title: String, isbn10: String?, isbn13: String?): LiveData<Book> {
        return bookDao.findAlike(bookId, title, isbn10, isbn13)
    }
}
