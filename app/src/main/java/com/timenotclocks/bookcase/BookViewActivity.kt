package com.timenotclocks.bookcase

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.database.*


const val LOG_BOOK_VIEW = "BookView"

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

        intent.extras?.getLong(EXTRA_ID)?.let { id ->
            bookViewModel.getBook(id).observe(this, { observable ->
                book = observable
                populateViews(observable)
            })
        }
    }

    private fun populateViews(current: Book) {
        current.cover("L").let {
            val coverView = findViewById<ImageView>(R.id.book_view_cover_image)
            Picasso.get().load(it).into(coverView, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    coverView.visibility = View.VISIBLE
                }

                override fun onError(e: java.lang.Exception?) {}
            })

        }
        current.title?.let {findViewById<TextView>(R.id.book_view_title).text = it}
        current.subtitle?.let {
            val subView = findViewById<TextView>(R.id.book_view_subtitle)
            subView.text = it
            subView.visibility = View.VISIBLE
        }
        findViewById<TextView>(R.id.book_view_author).text = "By: ${current.authorString()}"
        current.isbn10?.let { findViewById<TextView>(R.id.book_view_isbn10).text = "ISBN13: $it" }
        current.isbn13?.let { findViewById<TextView>(R.id.book_view_isbn13).text = "ISBN10: $it" }
        current.yearString()?.let { findViewById<TextView>(R.id.book_view_year).text = "$it" }
        current.publisher?.let { findViewById<TextView>(R.id.book_view_publisher).text = "From: $it" }
        current.dateAdded?.let { findViewById<TextView>(R.id.book_view_date_added).text = it.toString() }
        current.dateStarted?.let { findViewById<TextView>(R.id.book_view_date_started).text = it.toString() }
        current.dateRead?.let { findViewById<TextView>(R.id.book_view_date_shelved).text = it.toString() }
        current.notes?.let { findViewById<TextView>(R.id.book_view_notes).text = it }
        val shelfDropdown = findViewById<Button>(R.id.book_view_shelf_dropdown)
        shelfDropdown.text = current.shelfString()
        shelfDropdown.setOnClickListener { view ->
            view?.let { v -> showMenu(v, R.menu.shelf_menu) }
        }

        val ratingBar = findViewById<RatingBar>(R.id.book_view_rating_bar)
        current.rating?.let { ratingBar.rating = it.toFloat() }
        ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { rateBar, rating, fromUser ->
            current.rating = rating.toInt()
            bookViewModel.update(current)
            Snackbar.make(
                    findViewById(R.id.book_view_activity),
                    "Your new rating has been recorded",
                    Snackbar.LENGTH_LONG
            ).setAction("Action", null).show()
        }

        current.isbn13?.let {
            findViewById<MaterialButton>(R.id.book_view_button_open_library).setOnClickListener { view ->
                val url = "https://openlibrary.org/isbn/$it"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
            findViewById<MaterialButton>(R.id.book_view_button_greenlight).setOnClickListener { view ->
                val url = "https://www.greenlightbookstore.com/book/$it"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
            findViewById<MaterialButton>(R.id.book_view_button_bookshop).setOnClickListener { view ->
                val url = "https://bookshop.org/books?keywords=$it"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.menu_edit -> {
                Log.i(LOG_BOOK_VIEW, "Editing this book")

                book?.let { it ->
                    val intent = Intent(applicationContext, BookEditActivity::class.java).apply {
                        putExtra(EXTRA_ID, it.bookId)
                    }
                    startActivityForResult(intent, 100)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                intent.extras?.getLong(EXTRA_ID)?.let { bookId ->
                    bookViewModel.getBook(bookId).observe(this, { observable ->
                        populateViews(observable)
                    })
                }
                Snackbar.make(
                        findViewById(R.id.book_view_activity),
                        "Book information has been saved", Snackbar.LENGTH_LONG
                ).setAction("Action", null).show()
            }
            RESULT_DELETED -> {
                finish()
            }
            RESULT_CANCELED -> {
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(applicationContext, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            val btn = findViewById<Button>(R.id.book_view_shelf_dropdown)
            when (menuItem.itemId) {
                R.id.shelf_to_read -> {
                    book?.let { it.shelve(ShelfType.ToReadShelf, btn, bookViewModel) }
                }
                R.id.shelf_currently_reading -> {
                    book?.let { it.shelve(ShelfType.CurrentShelf, btn, bookViewModel) }
                }
                R.id.shelf_read -> {
                    book?.let { it.shelve(ShelfType.ReadShelf, btn, bookViewModel) }
                }
            }
            true
        }
        popup.show()
    }

}



