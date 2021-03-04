package com.timenotclocks.bookcase

import android.content.Intent
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
import androidx.core.widget.doAfterTextChanged
import com.beust.klaxon.Klaxon
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.database.*
import com.timenotclocks.bookcase.ui.main.DuplicateDialogFragment
import com.timenotclocks.bookcase.ui.main.EXTRA_BOOK
import com.timenotclocks.bookcase.ui.main.EXTRA_OPEN_LIBRARY_SEARCH
import java.time.LocalDate


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
        current.title.let { findViewById<TextView>(R.id.book_view_title).text = it }
        current.cover("L").let {
            val coverView = findViewById<ImageView>(R.id.book_view_cover_image)
            Picasso.get().load(it).into(coverView, object: com.squareup.picasso.Callback {
                override fun onSuccess() { coverView.visibility = View.VISIBLE }
                override fun onError(e: java.lang.Exception?) { }
            })

        }
        current.subtitle?.let { findViewById<TextView>(R.id.book_view_subtitle).text = it }
        current.author?.let { findViewById<TextView>(R.id.book_view_author).text = it }
        current.authorExtras?.let { findViewById<TextView>(R.id.book_view_author_extras).text = it }
        current.isbn10?.let { findViewById<TextView>(R.id.book_view_isbn10).text = it }
        current.isbn13?.let {  isbn -> findViewById<TextView>(R.id.book_view_isbn13).text = isbn}
        current.publisher?.let { findViewById<TextView>(R.id.book_view_publisher).text = it }
        current.yearString()?.let { findViewById<TextView>(R.id.book_view_year).text = it }
        current.dateAdded?.let {findViewById<TextView>(R.id.book_view_date_added).text = it.toString()}
        current.dateStarted?.let {findViewById<TextView>(R.id.book_view_date_started).text = it.toString()}
        current.dateRead?.let { findViewById<TextView>(R.id.book_view_date_shelved).text = it.toString() }
        current.notes?.let { findViewById<TextView>(R.id.book_view_notes).text = it }
        val shelfDropdown = findViewById<Button>(R.id.book_view_shelf_dropdown)
        current.shelf.let {
            shelfDropdown.text = it
            shelfDropdown.setOnClickListener { view ->
                view?.let { v -> showMenu(v, R.menu.shelf_menu) }
            }
        }

        val ratingBar = findViewById<RatingBar>(R.id.book_view_rating_bar)
        current.rating?.let { ratingBar.rating = it.toFloat() }
        ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener {
            rateBar, rating, fromUser ->
            current.rating = rating.toInt()
            bookViewModel.update(current)
            Snackbar.make(
                    findViewById(R.id.book_view_activity),
                    "Your new rating has been recorded",
                    Snackbar.LENGTH_LONG
            ).setAction("Action", null).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.menu_edit -> {
                Log.i(LOG_BOOK_VIEW, "Editing this book")

                book?.let{ it ->
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
        when(resultCode) {
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
            RESULT_CANCELED -> {}
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(applicationContext, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            val btn = findViewById<Button>(R.id.book_view_shelf_dropdown)
            when(menuItem.itemId) {
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



