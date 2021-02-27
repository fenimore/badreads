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

        val bookInfo: String = intent.extras?.get(EXTRA_BOOK).toString()
        book = Klaxon().fieldConverter(KlaxonDate::class, dateConverter).parse<Book>(bookInfo)

        if (intent.getBooleanExtra(EXTRA_NEW, false)) {
            book?.let {
                bookViewModel.insert(it)
                Snackbar.make(
                        findViewById(R.id.book_view_activity),
                        "You've added ${it.title} to your library",
                        Snackbar.LENGTH_LONG
                ).setAction("Action", null).show()
            }
        }
        if (intent.getBooleanExtra(EXTRA_OPEN_LIBRARY_SEARCH, false)) {
            book?.let { search ->
                if (search.bookId == 0.toLong()) {
                    val alike = bookViewModel.findAlike("%${search.title}%", search.isbn10, search.isbn13)
                    alike.observe(this,{ observed ->
                        observed?.let { books ->
                            if (books.isNotEmpty()) {
                                Log.i(LOG_TAG, "Found duplicate book entries ${books.size}")
                                val dialog = DuplicateDialogFragment(books.first(), search)

                                dialog.show(supportFragmentManager, LOG_TAG)
                            } else {
                                book?.let {
                                    bookViewModel.insert(it)
                                    Snackbar.make(
                                            findViewById(R.id.book_view_activity),
                                            "You've added ${it.title} to your library",
                                            Snackbar.LENGTH_LONG
                                    ).setAction("Action", null).show()
                                }
                            }
                        }
                    })
                }
            }
        }

        Log.i(LOG_BOOK_VIEW, book.toString())

        book?.let { current -> populateViews(current) }
    }

    private fun populateViews(current: Book) {
        current.title.let { findViewById<TextView>(R.id.book_view_title).text = it }
        current.cover("L").let { Picasso.get().load(it).into(findViewById<ImageView>(R.id.book_view_cover_image)) }
        current.subtitle?.let { findViewById<TextView>(R.id.book_view_subtitle).text = it }
        current.author?.let { findViewById<TextView>(R.id.book_view_author).text = it }
        current.authorExtras?.let { findViewById<TextView>(R.id.book_view_author_extras).text = it }
        current.isbn10?.let { findViewById<TextView>(R.id.book_view_isbn10).text = it }
        current.isbn13?.let {  isbn -> findViewById<TextView>(R.id.book_view_isbn13).text = isbn}
        current.publisher?.let { findViewById<TextView>(R.id.book_view_publisher).text = it }
        current.year?.let { findViewById<TextView>(R.id.book_view_year).text = it.toString() }
        current.originalYear?.let { findViewById<TextView>(R.id.book_view_original_year).text = it.toString() }
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
                val intent = Intent(applicationContext, BookEditActivity::class.java).apply {
                    book?.let {
                        putExtra(EXTRA_BOOK, Klaxon().fieldConverter(KlaxonDate::class, dateConverter).toJsonString(it))
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
                val bookInfo: String = data?.getStringExtra(EXTRA_SAVED_BOOK).toString()
                book = Klaxon().fieldConverter(KlaxonDate::class, dateConverter).parse<Book>(bookInfo)
                book?.let{populateViews(it)}
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

    private fun setShelf(shelf: String): Boolean {
        book?.let {
            if (it.shelf != shelf) {
                it.shelf = shelf
                when(shelf){
                    "read" -> {it.dateRead = LocalDate.now()}
                    "currently-reading" -> {it.dateStarted = LocalDate.now()}
                }
                bookViewModel.update(it)
                findViewById<Button>(R.id.book_view_shelf_dropdown).text = shelf
                Snackbar.make(
                        findViewById(R.id.book_view_activity),
                        "You've shelved ${it.title}",
                        Snackbar.LENGTH_LONG
                ).setAction("Action", null).show()
            }
        }
        return true
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(applicationContext, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when(menuItem.itemId) {
                R.id.shelf_to_read -> {setShelf("to-read")}
                R.id.shelf_currently_reading -> {setShelf("currently-reading")}
                R.id.shelf_read -> {setShelf("read")}
                else -> {true}
            }
        }
        popup.show()
    }

}



