package com.timenotclocks.bookcase

import android.graphics.Color
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
import com.db.williamchart.view.BarChartView
import com.db.williamchart.view.DonutChartView
import com.timenotclocks.PublisherCountcase.ui.main.LOG_PUB
import com.timenotclocks.PublisherCountcase.ui.main.PublisherCountAdapter
import com.timenotclocks.bookcase.database.*
import java.time.LocalDate


class ChartActivity : AppCompatActivity() {
    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
            Log.d(LOG_PUB, "Listing the best publishers")
            adapter.submitList(observable)
        })

        bookViewModel.booksReadLastTwelveMonths().observe(this, { observable: List<ChartResult> ->
            Log.d(LOG_TAG, "Charting last twelve months")
            findViewById<BarChartView>(R.id.chart_bar_monthly_read)?.show(observable.map{
                Pair<String, Float>(it.label, it.value)
            })
        })

        // Goals: Year to date read
        val yearlyGoal = 52.0.toFloat()
        val goalDonut = findViewById<DonutChartView>(R.id.chart_donut_goal)
        goalDonut.donutColors = intArrayOf(
                Color.parseColor("#00cbcc"),
                Color.parseColor("#EAEAEA")
        )
        goalDonut.show(listOf(0F, 100F))
        bookViewModel.booksReadAllYear().observe(this, { observable ->
            val percentComplete = observable.toFloat() / yearlyGoal * 100.0.toFloat()
            findViewById<TextView>(R.id.chart_label_books_to_year_percent).text = "Goal of ${yearlyGoal.toInt()}"
            findViewById<TextView>(R.id.chart_books_to_year_percent).text = "${percentComplete.toInt()} %"
            findViewById<TextView>(R.id.chart_label_books_to_year).text = "Books ${LocalDate.now().year}:"
            findViewById<TextView>(R.id.chart_books_to_year).text = String.format("%,d read", observable)
            goalDonut.show(listOf(percentComplete, 100F))
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