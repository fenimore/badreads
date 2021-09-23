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

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * View Model to keep a reference to the word repository and
 * an up-to-date list of all words.
 */

const val LOG_SORT = "BookSort"

class BookViewModel(private val repository: BookRepository) : ViewModel() {

    // - Repository is completely separated from the UI through the ViewModel.
    val allBooks: LiveData<List<Book>> = repository.allBooks.asLiveData()
    val readShelf: LiveData<List<Book>> = repository.sortedReadShelf.asLiveData()
    val toReadShelf: LiveData<List<Book>> = repository.sortedToReadShelf.asLiveData()
    val currentShelf: LiveData<List<Book>> = repository.sortedCurrentShelf.asLiveData()

    fun getBook(id: Long): LiveData<Book> {
        return repository.getBook(id)
    }

    fun getBookOnce(id: Long): Book {
        return repository.getBookOnce(id)
    }

    fun query(term: String): LiveData<List<Book>> {
        return repository.query(term).asLiveData() as MutableLiveData<List<Book>>
    }

    fun findAlike(bookId: Long, title: String, subtitle: String?, isbn10: String?, isbn13: String?): LiveData<Book> {
        return repository.findAlike(bookId, title, subtitle, isbn10, isbn13)
    }

    fun authorSort(shelfType: ShelfType): LiveData<List<Book>> {
        return repository.authorSort(shelfType).asLiveData()
    }

    fun yearSort(shelfType: ShelfType): LiveData<List<Book>> {
        return repository.yearSort(shelfType).asLiveData()
    }

    fun ratingSort(shelfType: ShelfType): LiveData<List<Book>> {
        return repository.ratingSort(shelfType).asLiveData()
    }

    fun pageNumbersSortAsc(shelfType: ShelfType): LiveData<List<Book>> {
        return repository.pageNumbersSortAsc(shelfType).asLiveData()
    }

    fun pageNumbersSortDesc(shelfType: ShelfType): LiveData<List<Book>> {
        return repository.pageNumbersSortDesc(shelfType).asLiveData()
    }

    fun dateReadSort(shelfType: ShelfType): LiveData<List<Book>> {
        return repository.dateReadSort(shelfType).asLiveData()
    }

    fun dateStartedSort(shelfType: ShelfType): LiveData<List<Book>> {
        return repository.dateStartedSort(shelfType).asLiveData()
    }

    fun sortShelf(shelfType: ShelfType, sortColumn: SortColumn): LiveData<List<Book>> {
        return repository.sortShelf(shelfType, sortColumn).asLiveData()
    }

    fun booksReadAllYear(): LiveData<Int> {
        return repository.booksReadSince(LocalDate.ofYearDay(LocalDate.now().year, 1).toEpochDay())
    }
    fun booksReadAllTime(): LiveData<Int> {
        return repository.booksReadSince(0.toLong())
    }
    fun averagePageNumbersReadAllYear():LiveData<Int> {
        return repository.averagePageNumbersReadSince(LocalDate.ofYearDay(LocalDate.now().year, 1).toEpochDay())
    }
    fun averagePageNumbersReadAllTime():LiveData<Int> {
        return repository.averagePageNumbersReadSince(0.toLong())
    }
    fun sumPageNumbersReadAllYear():LiveData<Int> {
        return repository.sumPageNumbersReadSince(LocalDate.ofYearDay(LocalDate.now().year, 1).toEpochDay())
    }
    fun sumPageNumbersReadAllTime():LiveData<Int> {
        return repository.sumPageNumbersReadSince(0.toLong())
    }
    fun topPublishers() : LiveData<List<PublisherCount>> {
        return repository.topPublishers().asLiveData()
    }
    fun booksReadLastTwelveMonths() : LiveData<List<ChartResult>> {
        return repository.booksReadLastTwelveMonths().asLiveData()
    }

    /**
     *
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(book: Book) = viewModelScope.launch {
        repository.insertBook(book)
    }

    fun insertSync(book: Book): LiveData<Long> {
        val result = MutableLiveData<Long>()
        viewModelScope.launch {
            val bookId = repository.insertBook(book)
            result.postValue(bookId)
        }
        return result
    }

    fun delete(book: Book) = viewModelScope.launch {
        repository.delete(book)
    }

    fun update(book: Book) = viewModelScope.launch {
        repository.update(book)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}

class BookViewModelFactory(private val repository: BookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
