package com.timenotclocks.bookcase

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.timenotclocks.PublisherCountcase.ui.main.LOG_PUB
import com.timenotclocks.PublisherCountcase.ui.main.PublisherCountAdapter
import com.timenotclocks.bookcase.database.BookViewModel
import com.timenotclocks.bookcase.database.BookViewModelFactory
import com.timenotclocks.bookcase.database.BooksApplication
import com.timenotclocks.bookcase.database.PublisherCount


class ChartActivity : AppCompatActivity() {
    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookViewModel.booksReadAllYear().observe(this, { observable ->
            findViewById<TextView>(R.id.chart_books_to_year).text = String.format("%,d", observable)
        })
        bookViewModel.booksReadAllTime().observe(this, { observable ->
            findViewById<TextView>(R.id.chart_books_all_time).text = String.format("%,d", observable)
        })
        bookViewModel.averagePageNumbersReadAllTime().observe(this, { observable ->
            findViewById<TextView>(R.id.chart_books_average_page).text = String.format("%,d", observable)
        })
        bookViewModel.sumPageNumbersReadAllTime().observe(this, { observable ->
            findViewById<TextView>(R.id.chart_books_sum_page).text = String.format("%,d", observable)
        })

        val topPubView = findViewById<RecyclerView>(R.id.chart_top_publishers)
        val adapter = PublisherCountAdapter()
        topPubView.adapter = adapter
        topPubView.layoutManager = LinearLayoutManager(applicationContext)

        bookViewModel.topPublishers().observe(this, { observable: List<PublisherCount> ->
            Log.d(LOG_PUB, "TODO: Doesn't work $observable")
            observable.let{adapter.submitList(it)}
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