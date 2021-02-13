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
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.timenotclocks.bookcase.api.RequestQueueSingleton
import com.timenotclocks.bookcase.database.Book
import com.timenotclocks.bookcase.ui.main.SearchAdapter
import com.timenotclocks.bookcase.ui.main.SearchAdapter2
import com.timenotclocks.bookcase.ui.main.SearchViewModel
import java.io.StringReader
import java.net.URLEncoder.encode
import java.time.LocalDate

/**
 * Activity for entering a word.
 */



class SearchActivity : AppCompatActivity() {

    private val searchViewModel: SearchViewModel by viewModels()
    private val adapter = SearchAdapter()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_book)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = parent?.findViewById<RecyclerView>(R.id.search_result_view)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(applicationContext)
    }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
        val single_book = listOf<Book>(Book(
                bookId = 0,
                title = "Test book",
                subtitle = "Test Subtitle",
                isbn10 = "123458690",
                isbn13 = "978123898998",
                author = "My author",
                authorExtras = null,
                publisher = "Shorman",
                year = 2009,
                originalYear = 2009,
                numberPages = 10,
                rating = 0,
                shelf = "to-read",
                notes = null,
                dateAdded = LocalDate.now(),
                dateRead = null,
        ))
        adapter.submitList(single_book)
        adapter.notifyDataSetChanged()

        val searchView = parent?.findViewById<SearchView>(R.id.search_bar_view)
        val numResultsView = parent?.findViewById<TextView>(R.id.num_results_view)
        searchView?.requestFocus()
        searchViewModel.numResults.observe(this, Observer<Int> {
            Log.i("BK", "Observable $it")
            it?.let {
                when {
                    it > 100 -> {numResultsView?.text = "Showing 100 of $it results"}
                    it > 0 ->  {numResultsView?.text = "$it results"}
                    it == 0 ->  {numResultsView?.text = ""}
                }
            }
        })
        searchViewModel.getSearches().observe(this, Observer<List<Book>> {
            it?.let {
                Log.i("BK", "Observable Submit ${it.size} ${it.first().title}")
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
                it.first().let { first ->
                    parent?.findViewById<TextView>(R.id.test_search_View)?.setText(first.title)
                }
            }
        })
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                Log.i("BK", "Query $query?")

                query?.let{searchViewModel.searchOpenLibrary(it)}

                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.i("BK", "Checking for something")
                return true
            }
        })

        return super.onCreateView(name, context, attrs)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                val myIntent = Intent(applicationContext, MainActivity::class.java)
                startActivityForResult(myIntent, 0)
            }
        }

        return super.onOptionsItemSelected(item)
    }
    private fun serializeSearchResults(results: JsonArray<JsonObject>): Array<Book> {
        return results.map { result ->
            val author: String? = (result["author_name"] as? JsonArray<*>)?.removeFirst() as String?
            val authorExtras: String? = (result["author_name"] as? JsonArray<*>)?.joinToString(", ")
            val publisher: String? = (result["publisher"] as? JsonArray<*>)?.removeFirst() as String?
            val year: Int? = (result["publish_date"] as? JsonArray<*>)?.removeFirst()?.toString()?.toIntOrNull()
            val originalYear: Int? = result["first_publish_date"].toString().toIntOrNull()
            val isbnArray: JsonArray<String>? = result["isbn"] as? JsonArray<String>
            val isbn10 = isbnArray?.firstOrNull { it.length <= 10 }
            val isbn13 = isbnArray?.firstOrNull { it.length == 13 }

            Book(
                    bookId = 0,
                    title = result["title"].toString(),
                    subtitle = result["subtitle"].toString(),
                    isbn10 = isbn10,
                    isbn13 = isbn13,
                    author = author,
                    authorExtras = authorExtras,
                    publisher = publisher,
                    year = year,
                    originalYear = originalYear,
                    numberPages = null,
                    rating = null,
                    shelf = "to-read",
                    notes = null,
                    dateAdded = LocalDate.now(),
                    dateRead = null,
            )
        }.toTypedArray()
    }
}