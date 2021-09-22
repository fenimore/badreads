package com.timenotclocks.bookcase

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.MenuRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.database.*
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

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_view)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        intent.extras?.getLong(EXTRA_ID)?.let { id ->
            bookViewModel.getBook(id).observe(this, { observable ->
                observable?.let {
                    book = it
                    populateViews(it)
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        intent.extras?.getLong(EXTRA_ID)?.let { id ->
            bookViewModel.getBook(id).observe(this, { observable ->
                observable?.let {
                    book = it
                    populateViews(it)
                }
            })
        }
    }

    /**
     * Show book on BookViewActivity
     *
     * @param book Book object of currently viewd book
     */
    private fun populateViews(current: Book) {

        supportActionBar?.title = current.title
        val coverView = findViewById<ImageView>(R.id.book_view_cover_image)
        val emptyCoverView = findViewById<TextView>(R.id.book_view_empty_cover)
        current.cover("L").let {

            Picasso.get().load(it).into(coverView, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    emptyCoverView.visibility = View.INVISIBLE
                }
                override fun onError(e: java.lang.Exception?) {
                    Log.e("Picasso failed Book view activity", "onLoadFailed")
                }
            })
        }

        emptyCoverView?.text = current.titleString() + "\n\n" + current.authorString()
        coverView.drawable ?: run {
            emptyCoverView.visibility = View.VISIBLE
        }

        current.title.let {findViewById<TextView>(R.id.book_view_title).text = it}
        current.subtitle?.let {
            val subView = findViewById<TextView>(R.id.book_view_subtitle)
            subView.text = it
            subView.visibility = View.VISIBLE
        }
        findViewById<TextView>(R.id.book_view_author).text = "By: ${current.authorString()}"
        current.isbn10?.let { findViewById<TextView>(R.id.book_view_isbn10).text = "ISBN 10: $it" }
        current.isbn13?.let { findViewById<TextView>(R.id.book_view_isbn13).text = "ISBN 13: $it" }
        current.numberPages?.let { findViewById<TextView>(R.id.book_view_page_number).text = "Pages: $it" }
        current.series?.let { findViewById<TextView>(R.id.book_view_series).text = "Series: $it" }
        current.language?.let { findViewById<TextView>(R.id.book_view_language).text = "Language: $it" }
        current.progress?.let { findViewById<TextView>(R.id.book_view_progress).text = "Progress: $it" }
        current.yearString()?.let { findViewById<TextView>(R.id.book_view_year).text = "$it" }
        current.publisher?.let { findViewById<TextView>(R.id.book_view_publisher).text = it }
        current.dateAdded?.let { findViewById<TextView>(R.id.book_view_date_added).text = LocalDate.ofEpochDay(it).format(viewDateFormatter) }
        current.dateStarted?.let { findViewById<TextView>(R.id.book_view_date_started).text = LocalDate.ofEpochDay(it).format(viewDateFormatter) }
        current.dateRead?.let { findViewById<TextView>(R.id.book_view_date_shelved).text = LocalDate.ofEpochDay(it).format(viewDateFormatter) }
        current.notes?.let {
            val notesView = findViewById<TextView>(R.id.book_view_notes)
            notesView.visibility = View.VISIBLE
            notesView.text = it
        }
        current.description?.let {
            val desc = findViewById<TextView>(R.id.book_view_description)
            findViewById<ImageView>(R.id.book_view_desc_horizontal).visibility = View.VISIBLE
            desc.visibility = View.VISIBLE
            desc.text = current.description
        }

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

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.menu_with_badreads -> {
                val intent = Intent(applicationContext, OpenLibrarySearchActivity::class.java).apply {
                    putExtra(EXTRA_SEARCH, book?.titleString())
                }
                startActivity(intent)
            }
            R.id.menu_edit, R.id.menu_edit_text -> {
                Log.i(LOG_BOOK_VIEW, "Editing this book")
                book?.let { it ->
                    val intent = Intent(applicationContext, BookEditActivity::class.java).apply {
                        putExtra(EXTRA_ID, it.bookId)
                    }
                    startActivityForResult(intent, 100)
                }
            }
            R.id.menu_open_library, R.id.menu_open_library_text -> {
                book?.let{ b ->
                    val term = b.isbn13 ?: b.titleString()
                    val url = "https://openlibrary.org/search?q=$term"
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }
            }
            R.id.menu_delete -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.apply {
                    setPositiveButton("OK",
                            DialogInterface.OnClickListener { dialog, id ->
                                // User clicked OK button
                                book?.let { bookViewModel.delete(it) }
                                val intent = Intent(applicationContext, MainActivity::class.java)
                                setResult(RESULT_DELETED, intent)
                                finish();
                            })
                    setNegativeButton("CANCEL",
                            DialogInterface.OnClickListener { dialog, id ->
                                // User cancelled the dialog
                            })
                }
                builder.setMessage("Delete this book?")
                builder.create()
                builder.show()
            }
            R.id.menu_greenlight -> {
                book?.let { b ->
                    val url: String = b.isbn13?.let {
                        "https://www.greenlightbookstore.com/book/$it"

                    } ?: run {
                        "https://www.greenlightbookstore.com/search/site/${b.titleString().replace(" ", "+").replace(":", "")}"
                    }
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG_NEW, "FFF BookViewActivity onActivityResult data: $data")
        when (resultCode) {
            RESULT_OK -> {
                intent.extras?.getLong(EXTRA_ID)?.let { bookId ->
                    bookViewModel.getBook(bookId).observe(this, { observable ->
                        observable?.let{populateViews(it)}
                    })
                }
                Snackbar.make(
                        findViewById(R.id.book_view_activity),
                        "Book information has been saved", Snackbar.LENGTH_LONG
                ).setAction("Action", null).show()
            }
            RESULT_DELETED -> {
                val intent = Intent(applicationContext, MainActivity::class.java)
                setResult(RESULT_DELETED, intent)
                finish()
            }
            RESULT_CANCELED -> {
                intent.extras?.getLong(EXTRA_ID)?.let { bookId ->
                    bookViewModel.getBook(bookId).observe(this, { observable ->
                        observable?.let{populateViews(it)}
                    })
                }
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



