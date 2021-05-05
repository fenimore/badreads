package com.timenotclocks.bookcase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import com.timenotclocks.bookcase.database.BookViewModel
import com.timenotclocks.bookcase.database.BookViewModelFactory
import com.timenotclocks.bookcase.database.BooksApplication

class ChartActivity : AppCompatActivity() {
    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookViewModel.booksReadAllYear().observe(this, {observable ->
            findViewById<TextView>(R.id.chart_books_to_year).text = "$observable"
        })
        bookViewModel.booksReadAllTime().observe(this, {observable ->
            findViewById<TextView>(R.id.chart_books_all_time).text = "$observable"
        })
        bookViewModel.averagePageNumbersReadAllTime().observe(this, {observable ->
            findViewById<TextView>(R.id.chart_books_average_page).text = "$observable"
        })
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }

}