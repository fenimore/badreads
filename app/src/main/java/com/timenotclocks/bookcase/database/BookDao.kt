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

    @Query("SELECT * FROM books WHERE shelf = :shelf ORDER BY rating DESC")
    fun ratingSort(shelf: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE shelf = :shelf ORDER BY numberPages DESC")
    fun pageNumbersSortDesc(shelf: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE shelf = :shelf ORDER BY numberPages ASC")
    fun pageNumbersSortAsc(shelf: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE shelf = 'read' ORDER BY dateAdded DESC")
    fun readShelf(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE shelf = 'to-read' ORDER BY dateAdded DESC")
    fun toReadShelf(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE (title LIKE :title  OR subtitle LIKE :subtitle OR isbn10 LIKE :isbn10 OR isbn13 LIKE :isbn13) AND :bookId != bookId LIMIT 1")
    fun findAlike(bookId: Long, title: String, subtitle: String?, isbn10: String?, isbn13: String?): LiveData<Book>

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

    @Query("SELECT * FROM books WHERE bookId=:id ")
    fun getBookOnce(id: Long): Book

    @Query("SELECT * FROM books")
    fun getBooks(): Flow<List<Book>>

    @Query("""
        SELECT * FROM books 
        JOIN books_fts ON books.bookId == books_fts.bookId 
        WHERE books_fts MATCH :term 
        GROUP BY books.bookId
        """ )
    fun fullSearch(term: String): Flow<List<Book>>


    @Query("""
        SELECT COUNT(*) FROM books
        WHERE shelf = 'read'
         AND dateRead > :sinceTimeStamp
        """ )
    fun booksReadSince(sinceTimeStamp: Long): LiveData<Int>

    @Query("""
        SELECT AVG(numberPages) FROM books
        WHERE shelf = 'read'
         AND dateRead > :sinceTimeStamp        
    """)
    fun averagePageNumbersReadSince(sinceTimeStamp: Long): LiveData<Int>

    @Query("""
        SELECT SUM(numberPages) FROM books
        WHERE shelf = 'read'
         AND dateRead > :sinceTimeStamp        
    """)
    fun sumPageNumbersReadSince(sinceTimeStamp: Long): LiveData<Int>

    @Query("""
        SELECT publisher, count(*) AS 'count' FROM books
        WHERE publisher IS NOT NULL
        GROUP BY publisher
        HAVING count(*) > 1
        ORDER BY count DESC
        LIMIT 20
    """)
    fun topPublishers() : Flow<List<PublisherCount>>

    @Query("""
        SELECT 
          strftime('%m', dateRead * 86400, 'unixepoch') AS label, 
          CAST(count(*) AS FLOAT) AS value 
        FROM books 
        WHERE dateRead IS NOT NULL AND shelf = 'read'
         AND datetime(dateRead * 86400, 'unixepoch') > datetime('now' , 'start of month', '-12 months') 
        GROUP BY strftime('%Y-%m', dateRead * 86400, 'unixepoch') 
        ORDER BY strftime('%Y-%m', dateRead * 86400, 'unixepoch')
    """)
    fun booksReadLastTwelveMonths() : Flow<List<ChartResult>>

    // Average page numbers
    // Rate of book per week
    // Rate of book per day
    // Average length of time to read
}
