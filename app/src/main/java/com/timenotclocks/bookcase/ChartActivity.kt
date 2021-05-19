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
import com.db.williamchart.view.HorizontalBarChartView
import com.timenotclocks.bookcase.database.*
import java.time.LocalDate


class ChartActivity : AppCompatActivity() {
    val animationDuration = 2000L
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

        bookViewModel.topPublishers().observe(this, { observable: List<PublisherCount> ->
            Log.d(LOG_TAG, "Listing the best publishers")
            val horizontalBarSet = observable.map{
                val ellipsed = if (it.publisher.length > 18) it.publisher.take(18) + "..." else it.publisher
                Pair<String, Float>("${ellipsed} ${it.count}", it.count.toFloat())
            }.reversed()
            val horizontalBarChart = findViewById<HorizontalBarChartView>(R.id.chart_publishers_horizontal_bar)

            horizontalBarChart.animation.duration = animationDuration
            horizontalBarChart.animate(horizontalBarSet)
        })

        bookViewModel.booksReadLastTwelveMonths().observe(this, { observable: List<ChartResult> ->
            Log.d(LOG_TAG, "Charting last twelve months")
            val mapLabels: Map<String, String> = mapOf(
                    "01" to "Jan", "02" to "Feb", "03" to "Mar", "04" to "Apr", "05" to "May",
                    "06" to "Jun", "07" to "Jul", "08" to "Aug", "09" to "Sept", "10" to "Oct",
                    "11" to "Nov", "12" to "Jan"
            )
            val monthlyBar = findViewById<BarChartView>(R.id.chart_bar_monthly_read)
            monthlyBar.animation.duration = animationDuration
            monthlyBar.animate(observable.map{
                Log.d(LOG_TAG, mapLabels.toString() + mapLabels["01"])
                val label: String = mapLabels[it.label].orEmpty()
                Pair<String, Float>(label, it.value )
            })
        })

        // Goals: Year to date read
        findViewById<TextView>(R.id.chart_label_books_to_year).text = "Books ${LocalDate.now().year}:"
        findViewById<TextView>(R.id.chart_bar_years_subtitle).text = "${LocalDate.now().year - 1} - ${LocalDate.now().year}"
        val yearlyGoal = 52.0.toFloat()
        val goalDonut = findViewById<DonutChartView>(R.id.chart_donut_goal)
        goalDonut.donutColors = intArrayOf(
                Color.parseColor("#00cbcc"),
                Color.parseColor("#EAEAEA")
        )
        goalDonut.animation.duration = animationDuration
        goalDonut.animate(listOf(0F, 100F))
        bookViewModel.booksReadAllYear().observe(this, { observable ->
            val percentComplete = observable.toFloat() / yearlyGoal * 100.0.toFloat()
            findViewById<TextView>(R.id.chart_label_books_to_year_percent).text = "Goal of ${yearlyGoal.toInt()}"
            findViewById<TextView>(R.id.chart_books_to_year_percent).text = "${percentComplete.toInt()} %"
            findViewById<TextView>(R.id.chart_books_to_year).text = String.format("%,d read", observable)
            goalDonut.animate(listOf(percentComplete, 100F))
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