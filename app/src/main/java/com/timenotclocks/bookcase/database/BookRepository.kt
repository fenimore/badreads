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

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */

class PublisherCount (
    val publisher: String,
    val count: Int
)

class ChartResult (
        val label: String,
        val value: Float
)

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

    fun getBookOnce(id: Long): Book {
        return bookDao.getBookOnce(id)
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

    fun ratingSort(shelfType: ShelfType): Flow<List<Book>> {
        return bookDao.ratingSort(shelfType.shelf)
    }

    fun pageNumbersSortAsc(shelfType: ShelfType): Flow<List<Book>> {
        return bookDao.pageNumbersSortAsc(shelfType.shelf)
    }

    fun pageNumbersSortDesc(shelfType: ShelfType): Flow<List<Book>> {
        return bookDao.pageNumbersSortDesc(shelfType.shelf)
    }

    fun authorSort(shelfType: ShelfType): Flow<List<Book>> {
        return bookDao.authorSort(shelfType.shelf)
    }

    fun sortShelf(shelfType: ShelfType, sortColumn: SortColumn): Flow<List<Book>> {
        return bookDao.sortShelf(shelfType.shelf, sortColumn.column)
    }

    fun findAlike(bookId: Long, title: String, subtitle: String?, isbn10: String?, isbn13: String?): LiveData<Book> {
        // TODO: pass in book more simply..
        return bookDao.findAlike(bookId, title, subtitle, isbn10, isbn13)
    }

    fun booksReadSince(since: Long): LiveData<Int> {
        return bookDao.booksReadSince(since)
    }

    fun averagePageNumbersReadSince(since: Long): LiveData<Int> {
        return bookDao.averagePageNumbersReadSince(since)
    }

    fun sumPageNumbersReadSince(since: Long): LiveData<Int> {
        return bookDao.sumPageNumbersReadSince(since)
    }

    fun topPublishers() : Flow<List<PublisherCount>> {
        return bookDao.topPublishers()
    }
    fun booksReadLastTwelveMonths(): Flow<List<ChartResult>> {
        return bookDao.booksReadLastTwelveMonths()
    }
}
