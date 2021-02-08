package com.timenotclocks.bookcase

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.StringReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class BookView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_view)
        setSupportActionBar(findViewById(R.id.toolbar))

        val bookInfo: String = intent.extras?.get(EXTRA_BOOK).toString()

        val book = Klaxon().fieldConverter(KlaxonDate::class, dateConverter).parse<Book>(bookInfo)

        if (book != null) {

            if (book.isbn13 != null) {
                val url = "http://covers.openlibrary.org/b/isbn/${book.isbn13}-L.jpg"
                Log.i("BK", url.toString())
                val coverView = findViewById<ImageView>(R.id.coverImage)
                Picasso.get().load(url).into(coverView);
            }

            val titleEdit = findViewById<EditText>(R.id.title)
            book.title.let {
                titleEdit.setText(it)
            }
            val authorEdit = findViewById<EditText>(R.id.author)
            book.author.let {
                authorEdit.setText(it)
            }
            val isbnEdit = findViewById<EditText>(R.id.isbn)
            book.isbn?.let {
                Log.i("BK", "WTF tho $it")
                isbnEdit.setText(it.toString())
            }
            val isbn13Edit = findViewById<EditText>(R.id.isbn13)
            book.isbn13?.let {
                isbn13Edit.setText(it.toString())
            }
            val publisherEdit = findViewById<EditText>(R.id.publisher)
            book.publisher?.let {
                publisherEdit.setText(it.toString())
            }
            val yearEdit = findViewById<EditText>(R.id.year)
            book.year?.let {
                yearEdit.setText(it.toString())
            }
            val originalYearEdit = findViewById<EditText>(R.id.originalYear)
            book.originalYear?.let {
                originalYearEdit.setText("(it.toString())")
            }
        }
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }
}