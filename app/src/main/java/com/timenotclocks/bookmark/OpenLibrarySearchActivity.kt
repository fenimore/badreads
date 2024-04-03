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

package com.timenotclocks.bookmark

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.zxing.integration.android.IntentIntegrator
import com.timenotclocks.bookmark.api.MAX_SEARCH_RESULTS
import com.timenotclocks.bookmark.api.OpenLibraryViewModel
import com.timenotclocks.bookmark.database.BookViewModel
import com.timenotclocks.bookmark.database.BookViewModelFactory
import com.timenotclocks.bookmark.database.BooksApplication
import com.timenotclocks.bookmark.database.emptyBook
import com.timenotclocks.bookmark.ui.main.OpenLibrarySearchAdapter


/**
 * Activity for entering a word.
 */


const val LOG_SEARCH = "BookSearch"
const val EXTRA_SEARCH = "search_open_library_extra"
const val EXTRA_SCAN = "scan_open_library_extra"

class OpenLibrarySearchActivity : AppCompatActivity() {

    private val openLibraryViewModel: OpenLibraryViewModel by viewModels()

    private val adapter = OpenLibrarySearchAdapter()
    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_library_search)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.search_result_view)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(applicationContext)
        findViewById<Button>(R.id.search_button_add_manual)?.visibility = View.GONE
    }

    private fun searchOpenLibrary(
            searchView: androidx.appcompat.widget.SearchView?,
            progressBar: ProgressBar?,
            numResultsView: TextView?,
            manualBtn: MaterialButton?,
            recyclerView: RecyclerView?,
    ) {
        openLibraryViewModel.numResults.observe(this, Observer<Int> {
            progressBar?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
            it?.let {
                val searchHeader = numResultsView?.text
                when {
                    it > MAX_SEARCH_RESULTS -> {
                        numResultsView?.text = "$searchHeader ($MAX_SEARCH_RESULTS results of $it)"
                    }
                    it > 0 -> {
                        numResultsView?.text = "$searchHeader ($it results)"
                    }
                    it == 0 -> {
                        Log.d(LOG_TAG, "No Result")
                        // numResultsView?.text = "Not found"
                        manualBtn?.visibility = View.VISIBLE
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

                query?.let {
                    numResultsView?.text = "Searching $it"
                    openLibraryViewModel.searchOpenLibrary(it)
                    assignManual(false, it)
                }
                searchView.clearFocus()
                recyclerView?.visibility = View.GONE
                progressBar?.visibility = View.VISIBLE
                manualBtn?.visibility = View.INVISIBLE
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
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                finish()
                return true
            }
        })

        searchView?.queryHint = "Title, Author, or ISBN"
        searchItem.expandActionView()

        val progressBar = findViewById<ProgressBar>(R.id.search_progress_bar)
        val numResultsView = findViewById<TextView>(R.id.num_results_view)
        val manualBtn = findViewById<MaterialButton>(R.id.search_button_add_manual)
        val recyclerView = findViewById<RecyclerView>(R.id.search_result_view)
        assignManual(false, null)
        searchOpenLibrary(searchView, progressBar, numResultsView, manualBtn, recyclerView)
        numResultsView.text = "Search OpenLibrary.org"

        if (intent.getBooleanExtra(EXTRA_SCAN, false)) {
            val integrator = IntentIntegrator(this)
            integrator.setPrompt("Scan Barcode")
            integrator.setBeepEnabled(false)
            integrator.setOrientationLocked(false)
            integrator.initiateScan()
        }

        val searchIntent = intent.getStringExtra(EXTRA_SEARCH)
        searchIntent?.let { query ->
            Log.i(LOG_SEARCH, "Direct Query $query")
            numResultsView.text = "Searching $query"
            openLibraryViewModel.searchOpenLibrary(query)
            searchView?.clearFocus()
            progressBar?.visibility = View.VISIBLE
            manualBtn?.visibility = View.INVISIBLE
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val myIntent = Intent(applicationContext, MainActivity::class.java)
                startActivityForResult(myIntent, 0)
            }
            R.id.search_scan_item -> {
                val integrator = IntentIntegrator(this)
                integrator.setPrompt("Scan your book's barcode")
                integrator.setBeepEnabled(false)
                integrator.setOrientationLocked(false)
                integrator.initiateScan()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        result?.contents?.let { barcode ->
            Toast.makeText(this, "Barcode: $barcode", Toast.LENGTH_LONG).show()
            Log.d(LOG_TAG, "Barcode read: $barcode")
            findViewById<TextView>(R.id.num_results_view)?.text = "Scanned: $barcode"
            openLibraryViewModel.searchOpenLibrary(barcode)
            findViewById<ProgressBar>(R.id.search_progress_bar)?.visibility = View.VISIBLE
            findViewById<MaterialButton>(R.id.search_button_add_manual)?.visibility = View.INVISIBLE
            findViewById<RecyclerView>(R.id.search_result_view)?.visibility = View.INVISIBLE
            assignManual(true, barcode)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun assignManual(isBarcode: Boolean, value: String?) {

        val (isbn, isbn13, isbn10) = if (!isBarcode) {
            listOf(null, null, null)
        } else {
            value?.let {
                listOf(
                        it,
                        if (it.length > 10) it else null,
                        if (it.length < 11) it else null
                )
            } ?: listOf(null, null, null)
        }


        val manualBtn = findViewById<Button>(R.id.search_button_add_manual)
        manualBtn?.setOnClickListener { view ->
            val builder = Builder(this)
            builder.setTitle("Manual Book Entry")

            val input = EditText(this)
            // TODO: input type doesn't work?
            input.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS

            if (isBarcode) {
                builder.setMessage("Scanned ISBN: $isbn\nEnter new book title:")
            } else {
                builder.setMessage("Enter new book title")
                value?.let { input.setText(it) }
            }

            builder.setView(input)
            builder.setPositiveButton("Ok") { dialog, id ->
                input.text?.let { editable ->
                    val newBook = emptyBook(
                            fullTitle = editable.toString(), author = null, isbn10 = isbn10, isbn13 = isbn13
                    )

                    bookViewModel.insertSync(newBook).observe(this, { observed ->
                        if (observed > 0) {
                            Log.i(TAG_NEW, "Added Book Manually: $observed $editable")
                            val intent = Intent(this, BookEditActivity::class.java).apply {
                                putExtra(EXTRA_ID, observed)
                            }
                            startActivity(intent)
                        } else {
                            Log.e(TAG_NEW, "Manual Book could not be added: $observed $editable")
                        }
                    })
                }
            }
            builder.show()
        }
    }
}