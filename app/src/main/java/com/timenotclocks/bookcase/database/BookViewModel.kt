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

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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

    fun dateReadSort(shelfType: ShelfType): LiveData<List<Book>> {
        return repository.dateReadSort(shelfType).asLiveData()
    }

    fun dateStartedSort(shelfType: ShelfType): LiveData<List<Book>> {
        return repository.dateStartedSort(shelfType).asLiveData()
    }

    fun sortShelf(shelfType: ShelfType, sortColumn: SortColumn): LiveData<List<Book>> {
        return repository.sortShelf(shelfType, sortColumn).asLiveData()
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
