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
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.timenotclocks.bookcase.database.BookViewModel
import com.timenotclocks.bookcase.database.BookViewModelFactory
import com.timenotclocks.bookcase.database.BooksApplication
import com.timenotclocks.bookcase.ui.main.OpenLibrarySearchAdapter
import com.timenotclocks.bookcase.ui.main.SearchAdapter

class SearchActivity : AppCompatActivity() {

    private val adapter = SearchAdapter()
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
    }

    private fun searchLibrary(searchView: SearchView?, progressBar: ProgressBar?, numResultsView: TextView?) {
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.i(LOG_SEARCH, "Searching Library: $query?")

                query?.let {
                    bookViewModel.query("*$it*").observe(this@SearchActivity, { observable ->
                        progressBar?.visibility = View.GONE
                        observable?.let{ books -> adapter.submitList(books) }
                    })
                }
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
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchItem: MenuItem = menu.findItem(R.id.search_menu_item)
        var searchView = searchItem.actionView as? SearchView
        searchView?.queryHint = "Title, Author, or ISBN"
        searchItem.expandActionView()
        val progressBar = findViewById<ProgressBar>(R.id.search_progress_bar)
        val numResultsView = findViewById<TextView>(R.id.num_results_view)
        searchLibrary(searchView, progressBar, numResultsView)
        numResultsView.text = "Search your library"
        return true
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