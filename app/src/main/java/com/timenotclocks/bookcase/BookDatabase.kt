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

package com.timenotclocks.bookcase

import android.content.Context
import android.util.Log
import android.util.Log.INFO
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * This is the backend. The database. This used to be done by the OpenHelper.
 * The fact that this has very few comments emphasizes its coolness.
 */
@Database(entities = [
    Book::class,
], version = 1)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: BookDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): BookDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            context.deleteDatabase("books")  // TODO: delete
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "books"
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not part of this codelab.
                    .fallbackToDestructiveMigration()
                    .addCallback(BookDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        private class BookDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            /**
             * Override the onCreate method to populate the database.
             */
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // If you want to keep the data through app restarts,
                // comment out the following line.
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.bookDao())
                    }
                }
            }
        }

        /**
         * Populate the database in a new coroutine.
         * If you want to start with more words, just add them.
         */
        suspend fun populateDatabase(bookDao: BookDao) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.
            // bookDao.deleteAll()
            val title = "The Address Book: What Street Addresses Reveal About Identity, Race, Wealth, and Power"
            val isbn = 1250134765
            val isbn13 = 9781250134769
            var book = Book(
                    bookId = 0,
                    title = title,
                    coverUrl = null,
                    isbn = isbn,
                    isbn13 = isbn13,
                    authorFirst = "Deirdre",
                    authorLast = "Mask",
                    publisher = "St. Martin's Press,Hardcover",
                    year = 2020,
                    originalYear = 2020,
                    rating = null,
                    shelf ="want to read",
                    notes = null
            )
            var result = bookDao.insertBook(book)
            //val author = // Deirdre Mask,"Mask, Deirdre",,"=""1250134765""","=""9781250134769""",0,4.13,St. Martin's Press,Hardcover,336,2020,2020,,2020/11/25,to-read,to-read (#100),to-read,,,,0,,,0,,,,,

            //var book = Book("Hello")
            //bookDao.insert(book)
            //book = Book("World!")
            //bookDao.insert(book)
        }
    }
}
