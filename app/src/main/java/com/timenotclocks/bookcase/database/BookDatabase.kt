/*
  Fenimore Love | 2021

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/


package com.timenotclocks.bookcase.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class Converters {
    var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @TypeConverter
    fun fromDateString(value: String?): LocalDate? {
        if (value != "null" && value != null) {
            return LocalDate.parse(value, formatter)
        }
        return null  // this is dumb
    }


    @TypeConverter
    fun toDateString(date: LocalDate?): String? {
        return date.toString()
    }
}


/**
 * This is the backend. The database. This used to be done by the OpenHelper.
 * The fact that this has very few comments emphasizes its coolness.
 */
@Database(entities = [
    Book::class, BooksFts::class
], version = 5)
@TypeConverters(Converters::class)
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
            // context.deleteDatabase("books")  // TODO: delete  # TODO: refresh on load
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
        }
    }
}
