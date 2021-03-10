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
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.timenotclocks.bookcase.api.MAX_SEARCH_RESULTS
import com.timenotclocks.bookcase.api.OpenLibraryViewModel
import com.timenotclocks.bookcase.barcodereader.BarcodeActivity.RC_BARCODE_CAPTURE
import com.timenotclocks.bookcase.barcodereader.BarcodeCaptureActivity
import com.timenotclocks.bookcase.database.Book
import com.timenotclocks.bookcase.ui.main.OpenLibrarySearchAdapter


/**
 * Activity for entering a word.
 */


const val LOG_SEARCH = "BookSearch"


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
        openLibraryViewModel?.numResults.observe(this, Observer<Int> {
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
        openLibraryViewModel?.getSearches().observe(this, { books ->
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
        var searchView = searchItem.actionView as? androidx.appcompat.widget.SearchView
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

        if (intent.getBooleanExtra(EXTRA_SCAN, false)) {
            val intent = Intent(this, BarcodeCaptureActivity::class.java)
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, true)
            intent.putExtra(BarcodeCaptureActivity.UseFlash, false)
            startActivityForResult(intent, RC_BARCODE_CAPTURE)
        }

        numResultsView.text = "Search OpenLibrary.org for new books"

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                val myIntent = Intent(applicationContext, MainActivity::class.java)
                startActivityForResult(myIntent, 0)
            }
            R.id.search_scan_item -> {
                val intent = Intent(this, BarcodeCaptureActivity::class.java)
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true)
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false)
                startActivityForResult(intent, RC_BARCODE_CAPTURE)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_BARCODE_CAPTURE -> {
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    val barcode = data?.getParcelableExtra<Barcode>(BarcodeCaptureActivity.BarcodeObject)
                    Log.d(LOG_TAG, "Barcode read: " + barcode?.displayValue);
                    barcode?.displayValue?.let {

                        findViewById<TextView>(R.id.num_results_view)?.text = it
                        openLibraryViewModel.searchOpenLibrary(it)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}