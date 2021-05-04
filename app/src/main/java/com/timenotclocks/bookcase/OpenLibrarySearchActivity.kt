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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.integration.android.IntentIntegrator
import com.timenotclocks.bookcase.api.MAX_SEARCH_RESULTS
import com.timenotclocks.bookcase.api.OpenLibraryViewModel
import com.timenotclocks.bookcase.ui.main.OpenLibrarySearchAdapter


/**
 * Activity for entering a word.
 */


const val LOG_SEARCH = "BookSearch"
const val EXTRA_SEARCH = "search_open_library_extra"
const val EXTRA_SCAN = "scan_open_library_extra"

class OpenLibrarySearchActivity : AppCompatActivity() {

    private val openLibraryViewModel: OpenLibraryViewModel by viewModels()

    private val adapter = OpenLibrarySearchAdapter()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_library_search)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.search_result_view)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(applicationContext)
    }

    private fun searchOpenLibrary(searchView: androidx.appcompat.widget.SearchView?, progressBar: ProgressBar?, numResultsView: TextView?) {
        openLibraryViewModel.numResults.observe(this, Observer<Int> {
            progressBar?.visibility = View.GONE
            it?.let {
                when {
                    it > MAX_SEARCH_RESULTS -> {
                        numResultsView?.text = "Showing $MAX_SEARCH_RESULTS results of $it"
                    }
                    it > 0 -> {
                        numResultsView?.text = "Showing $it results"
                    }
                    it == 0 -> {
                        numResultsView?.text = "No results"

                    }
                }
            }
        })
        openLibraryViewModel.getSearches().observe(this, { books ->
            progressBar?.visibility = View.GONE
            books?.let {
                adapter.submitList(it)
            }
        })

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener, androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.i(LOG_SEARCH, "OpenLibrary Query $query?")

                query?.let { openLibraryViewModel.searchOpenLibrary(it) }
                searchView.clearFocus()
                progressBar?.visibility = View.VISIBLE
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })
    }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.open_library_search_menu, menu)
        val searchItem: MenuItem = menu.findItem(R.id.search_menu_item)
        val searchView = searchItem.actionView as? androidx.appcompat.widget.SearchView
        searchItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                return true;
            }
            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                finish()
                return true;
            }
        })

        searchView?.queryHint = "Title, Author, or ISBN"
        searchItem.expandActionView()

        val progressBar = findViewById<ProgressBar>(R.id.search_progress_bar)
        val numResultsView = findViewById<TextView>(R.id.num_results_view)

        searchOpenLibrary(searchView, progressBar, numResultsView)
        numResultsView.text = "Search OpenLibrary.org for new books"

        if (intent.getBooleanExtra(EXTRA_SCAN, false)) {
            val integrator = IntentIntegrator(this)
            integrator.setPrompt("Scan Barcode");
            integrator.setBeepEnabled(false);
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        }

        val searchIntent = intent.getStringExtra(EXTRA_SEARCH)
        searchIntent?.let{ query ->
            Log.i(LOG_SEARCH, "Direct Query $query")
            openLibraryViewModel.searchOpenLibrary(query)
            searchView?.clearFocus()
            progressBar?.visibility = View.VISIBLE
        }



        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                val myIntent = Intent(applicationContext, MainActivity::class.java)
                startActivityForResult(myIntent, 0)
            }
            R.id.search_scan_item -> {
                val integrator = IntentIntegrator(this)
                integrator.setPrompt("Scan your book's barcode");
                integrator.setBeepEnabled(false);
                integrator.setOrientationLocked(false)
                integrator.initiateScan();
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        result?.contents?.let{ barcode ->
            Toast.makeText(this, "Found: $barcode", Toast.LENGTH_LONG).show()
            Log.d(LOG_TAG, "Barcode read: $barcode");
            findViewById<TextView>(R.id.num_results_view)?.text = barcode
            openLibraryViewModel.searchOpenLibrary(barcode)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}