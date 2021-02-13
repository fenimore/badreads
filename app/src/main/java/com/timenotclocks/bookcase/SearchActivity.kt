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
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.timenotclocks.bookcase.database.Book
import com.timenotclocks.bookcase.ui.main.SearchAdapter
import com.timenotclocks.bookcase.ui.main.SearchViewModel
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

        val recyclerView = findViewById<RecyclerView>(R.id.search_result_view)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(applicationContext)
    }


    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
        val single_book = listOf<Book>(Book(
                bookId = 0,
                title = "One Thousand and One Winning Chess Sacrifices and Combinations",
                subtitle = "A Random Subtitle",
                isbn10 = "123458690",
                isbn13 = " 9780879801113",
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

        val searchView = parent?.findViewById<SearchView>(R.id.search_bar_view)
        val progressBar = parent?.findViewById<ProgressBar>(R.id.search_progress_bar)
        val numResultsView = parent?.findViewById<TextView>(R.id.num_results_view)
        searchView?.requestFocus()
        searchViewModel.numResults.observe(this, Observer<Int> {
            progressBar?.visibility = View.INVISIBLE
            it?.let {
                when {
                    it > 100 -> {numResultsView?.text = "Showing 100 results of $it"}
                    it > 0 ->  {numResultsView?.text = "Showing $it results"}
                    it == 0 ->  {numResultsView?.text = ""}
                }
            }
        })
        searchViewModel.getSearches().observe(this, Observer<List<Book>> {
            progressBar?.visibility = View.INVISIBLE
            it?.let { adapter.submitList(it) }
        })
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                Log.i("BK", "Query $query?")

                query?.let{searchViewModel.searchOpenLibrary(it)}
                searchView.clearFocus()
                progressBar?.visibility = View.VISIBLE
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
}