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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.timenotclocks.bookcase.database.Book
import com.timenotclocks.bookcase.ui.main.SearchAdapter
import com.timenotclocks.bookcase.ui.main.SearchViewModel

/**
 * Activity for entering a word.
 */




class SearchActivity : AppCompatActivity() {

    private lateinit var searchViewModel: SearchViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_book)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
        searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        val searchView = parent?.findViewById<SearchView>(R.id.search_view)
        searchView?.requestFocus()
        val numResultsView = parent?.findViewById<TextView>(R.id.num_results_view)
        searchViewModel.numResults.observe(this, Observer<Int> {
            Log.i("BK", "Observable")
            it?.let { numResultsView?.text = it.toString() }
        })

        val recyclerView = parent?.findViewById<RecyclerView>(R.id.search_result_view)
        recyclerView?.layoutManager = LinearLayoutManager(applicationContext)
        val adapter = SearchAdapter()
        recyclerView?.adapter = adapter
        searchViewModel.getSearches().observe(this, Observer<List<Book>> {
            it?.let {
                Log.i("BK", "Observable Submit ${it.size}")
                adapter.submitList(it)

            }
        })

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                Log.i("BK", "Query $query?")

                query?.let{
                    searchViewModel.searchOpenLibrary(it)
                }

                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.i("BKTEST", "Okay so ${searchViewModel.getSearches().value?.size}")
                Log.i("BKTEST", "Okay so ${searchViewModel.getSearches().value?.toString()}")
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