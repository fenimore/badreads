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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * View Model to keep a reference to the word repository and
 * an up-to-date list of all words.
 */

class BookViewModel(private val repository: BookRepository) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allBooks: LiveData<List<Book>> = repository.allBooks.asLiveData()
    val readShelf: LiveData<List<Book>> = repository.readShelf.asLiveData()
    val toReadShelf: LiveData<List<Book>> = repository.toReadShelf.asLiveData()
    val currentShelf: LiveData<List<Book>> = repository.currentShelf.asLiveData()

    fun findAlike(title: String, isbn10: String?, isbn13: String?): LiveData<List<Book>> {
        return repository.findAlike(title, isbn10, isbn13).asLiveData()
    }
    /**
     *
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(book: Book) = viewModelScope.launch {
        repository.insertBook(book)
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
