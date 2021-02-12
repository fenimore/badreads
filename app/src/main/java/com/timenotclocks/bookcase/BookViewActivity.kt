package com.timenotclocks.bookcase

import android.R.attr
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.beust.klaxon.Klaxon
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.database.*
import com.timenotclocks.bookcase.ui.main.EXTRA_BOOK


class BookViewActivity : AppCompatActivity() {

    private var book: Book? = null
    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.book_view_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_view)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bookInfo: String = intent.extras?.get(EXTRA_BOOK).toString()

        book = Klaxon().fieldConverter(KlaxonDate::class, dateConverter).parse<Book>(bookInfo)

        if (book?.isbn13 != null) {
            val url = "https://covers.openlibrary.org/b/isbn/${book?.isbn13}-L.jpg?default=false"
            val coverView = findViewById<ImageView>(R.id.book_view_cover_image)
            Picasso.get().load(url).into(
                    coverView,
                    object : com.squareup.picasso.Callback {
                        override fun onSuccess() {}
                        override fun onError(e: java.lang.Exception?) {}
                    }
            )
        }

        val titleView = findViewById<TextView>(R.id.book_view_title)
        book?.title.let {titleView.setText(it)}

        val subTitleView = findViewById<TextView>(R.id.book_view_subtitle)
        book?.subtitle.let {subTitleView.setText(it)}

        val authorView = findViewById<TextView>(R.id.author)
        book?.author.let {authorView.setText(it)}

        val isbn10View = findViewById<TextView>(R.id.book_view_isbn10)
        book?.isbn10?.let {isbn10View.setText(it.toString())}

        val isbn13View = findViewById<TextView>(R.id.book_view_isbn13)
        book?.isbn13?.let {isbn13View.setText(it.toString())}

        val publisherView = findViewById<TextView>(R.id.book_view_publisher)
        book?.publisher?.let {publisherView.setText(it.toString())}

        val yearView = findViewById<TextView>(R.id.book_view_year)
        book?.year?.let {yearView.setText(it.toString())}

        val originalYearView = findViewById<TextView>(R.id.book_view_original_year)
        book?.originalYear?.let {originalYearView.setText((it.toString()))}

        val shelfSpinner = findViewById<Spinner>(R.id.book_view_shelf_spinner)
        ArrayAdapter.createFromResource(
                this,
                R.array.shelf_spinner,
                android.R.layout.simple_spinner_item,
        ).also { adapter ->  // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            shelfSpinner.adapter = adapter
            when(book?.shelf) {
                "to-read" -> {
                    shelfSpinner.setSelection(0)
                }
                "currently-reading" -> {
                    shelfSpinner.setSelection(1)
                }
                "read" -> {
                    shelfSpinner.setSelection(2)
                }
            }
        }
        shelfSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                book?.let {
                    var shelves: Array<String> = resources.getStringArray(R.array.shelf_spinner)
                    if (it.shelf != shelves[position]) {
                        it.shelf = shelves[position]
                        bookViewModel.update(it)
                        Snackbar.make(
                                findViewById(R.id.book_view_activity),
                                "You've shelved ${it.title}",
                                Snackbar.LENGTH_LONG
                        ).setAction("Action", null).show()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val ratingBar = findViewById<RatingBar>(R.id.book_view_rating_bar)
        book?.rating?.let {ratingBar.setRating(it.toFloat())}
        ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener {
            rateBar, rating, fromUser ->
            book?.rating = rating.toInt()
            book?.let {bookViewModel.update(it)}
            Snackbar.make(
                    findViewById(R.id.book_view_activity),
                    "Your new rating has been recorded",
                    Snackbar.LENGTH_LONG
            ).setAction("Action", null).show()
        }

        val notesView = findViewById<TextView>(R.id.book_view_notes)
        book?.notes?.let {notesView.setText(it)}
        notesView.doAfterTextChanged { book?.notes = it.toString()}

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.menu_edit -> {
                val intent = Intent(applicationContext, BookEditActivity::class.java).apply {
                    book?.let {
                        putExtra(EXTRA_BOOK, Klaxon().toJsonString(book))
                    }
                }
                startActivityForResult(intent, 100)

            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(resultCode) {
            RESULT_OK -> {
                Snackbar.make(
                        findViewById(R.id.book_view_activity),
                        "Book information has been saved", Snackbar.LENGTH_LONG
                ).setAction("Action", null).show()
            }
            RESULT_CANCELED -> {
                finish()
            }

        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}



