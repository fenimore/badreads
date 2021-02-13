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

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertBook(book: Book) {
        bookDao.insertBook(book)
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

    @WorkerThread
    fun findAlike(title: String, isbn10: String?, isbn13: String?): List<Book> {
        return bookDao.findAlike(title, isbn10, isbn13)
    }
}
