package com.timenotclocks.bookcase


import android.annotation.SuppressLint
import android.content.DialogInterface
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.beust.klaxon.Klaxon
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.api.OpenLibraryViewModel
import com.timenotclocks.bookcase.database.*
import com.timenotclocks.bookcase.ui.main.EXTRA_BOOK
import java.time.LocalDate


const val RESULT_DELETED = 140
const val EXTRA_SAVED_BOOK = "book_changes_saved"
const val LOG_EDIT = "BookEdit"

class BookEditActivity : AppCompatActivity() {

    private var book: Book? = null
    private val openLibraryViewModel: OpenLibraryViewModel by viewModels()
    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.book_edit_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_edit)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.extras?.getLong(EXTRA_ID)?.let { bookId ->
            bookViewModel.getBook(bookId).observe(this, { observable ->
                observable?.let {
                    Log.i(LOG_EDIT, "Editing a book $it")
                    book = it
                    populateViews(it)
                }
            })
        }

        openLibraryViewModel.bookDetails.observe(this) { observable ->
            observable?.let { details ->
                if (
                        book?.isbn13 != details.isbn13 ?: book?.isbn13 ||
                        book?.isbn10 != details.isbn10 ?: book?.isbn10 ||
                        book?.publisher != details.publisher ?: book?.publisher ||
                        book?.year != details.publishYear ?: book?.year ||
                        book?.numberPages != details.numberPages ?: book?.numberPages
                ) {
                    Snackbar.make(
                            findViewById(R.id.book_edit_activity),
                            "New details added, click Save (disk) to save.",
                            Snackbar.LENGTH_INDEFINITE
                    ).setAction("Ok", null).show()
                    book?.isbn13 = details.isbn13 ?: book?.isbn13
                    book?.isbn10 = details.isbn10 ?: book?.isbn10
                    book?.publisher = details.publisher ?: book?.publisher
                    book?.year = details.publishYear ?: book?.year
                    book?.numberPages = details.numberPages ?: book?.numberPages
                    book?.let { populateViews(it) }
                } else {
                    Snackbar.make(
                            findViewById(R.id.book_edit_activity),
                            "No new details found.",
                            Snackbar.LENGTH_LONG
                    ).setAction("Ok", null).show()
                }
            }
        }
    }

    private fun populateViews(current: Book) {
        val emptyCover = findViewById<TextView>(R.id.book_edit_empty_cover)
        emptyCover?.text = current.titleString() + "\n\n" + current.authorString()
        val coverView = findViewById<ImageView>(R.id.book_edit_cover_image)
        current.cover("L").let {
            Picasso.get().load(it).into(coverView, object : Callback {
                override fun onSuccess() {
                    emptyCover.visibility = View.INVISIBLE
                }
                override fun onError(e: Exception) {}
            })
        }
        coverView.drawable ?: run {
            Log.i(LOG_TAG, "Check is it nulll?")
            emptyCover.visibility = View.VISIBLE
        }

        current.cover("L").let {
            Picasso.get().load(it).into(coverView)
        }
        findViewById<Button>(R.id.book_edit_image_edit).setOnClickListener { view ->
            val term = current.isbn13 ?: current.titleString()
            val url = "https://openlibrary.org/search?q=$term"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        val titleEdit = findViewById<TextInputLayout>(R.id.book_edit_title_layout)?.editText
        titleEdit?.setText(current.title)
        titleEdit?.doAfterTextChanged { editable -> current.title = editable.toString() }

        val subTitleEdit = findViewById<TextInputLayout>(R.id.book_edit_subtitle).editText
        subTitleEdit?.doAfterTextChanged { editable -> current.subtitle = editable.toString().ifEmpty { null } }
        current.subtitle?.let { subTitleEdit?.setText(it) }

        val authorEdit = findViewById<TextInputLayout>(R.id.book_edit_author).editText
        authorEdit?.doAfterTextChanged { editable -> current.author = editable.toString().ifEmpty { null } }
        current.author?.let { authorEdit?.setText(it) }

        val extrasEdit = findViewById<TextInputLayout>(R.id.book_edit_author_extras).editText
        extrasEdit?.doAfterTextChanged { editable -> current.authorExtras = editable.toString().ifEmpty { null } }
        current.authorExtras?.let { extrasEdit?.setText(it) }

        val isbn10Edit = findViewById<TextInputLayout>(R.id.book_edit_isbn10).editText
        current.isbn10?.let { isbn10Edit?.setText(it) }
        isbn10Edit?.doAfterTextChanged { editable -> current.isbn10 = editable.toString().ifEmpty { null } }

        val isbn13Edit = findViewById<TextInputLayout>(R.id.book_edit_isbn13).editText
        current.isbn13?.let { isbn -> isbn13Edit?.setText(isbn) }
        isbn13Edit?.doAfterTextChanged { editable -> current.isbn13 = editable.toString().ifEmpty { null } }

        val publisherEdit = findViewById<TextInputLayout>(R.id.book_edit_publisher).editText
        current.publisher?.let { publisherEdit?.setText(it) }
        publisherEdit?.doAfterTextChanged { editable -> current.publisher = editable.toString().ifEmpty { null } }

        val yearEdit = findViewById<TextInputLayout>(R.id.book_edit_year).editText
        current.year?.let { yearEdit?.setText(it.toString()) }
        yearEdit?.doAfterTextChanged { editable -> editable.toString().toIntOrNull().let { year -> current.year = year } }

        val originalYearEdit = findViewById<TextInputLayout>(R.id.book_edit_original_year).editText
        current.originalYear?.let { originalYearEdit?.setText(it.toString()) }
        originalYearEdit?.doAfterTextChanged { editable ->
            editable.toString().toIntOrNull().let { year ->
                current.originalYear = year
            }
        }
        val pageNumbersEdit = findViewById<TextInputLayout>(R.id.book_edit_page_numbers).editText
        current.numberPages?.let { pageNumbersEdit?.setText(it.toString()) }
        pageNumbersEdit?.doAfterTextChanged { editable ->
            editable.toString().toIntOrNull().let { pages ->
                current.numberPages = pages
            }
        }

        val progressEdit = findViewById<TextInputLayout>(R.id.book_edit_progress).editText
        current.progress?.let { progressEdit?.setText(it.toString()) }
        progressEdit?.doAfterTextChanged { editable ->
            editable.toString().toIntOrNull().let { progress ->
                current.progress = progress
            }
        }

        val dateAddedView = findViewById<DatePicker>(R.id.book_edit_date_added)
        val dateAdded = current.dateAdded?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()
        dateAddedView.init(dateAdded.year, dateAdded.monthValue - 1, dateAdded.dayOfMonth, object : DatePicker.OnDateChangedListener {
            override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                current.dateAdded = LocalDate.of(year, monthOfYear + 1, dayOfMonth).toEpochDay()
            }
        })
        val dateStartedView = findViewById<DatePicker>(R.id.book_edit_date_started)
        val dateStarted = current.dateStarted?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()
        dateStartedView.init(dateStarted.year, dateStarted.monthValue - 1, dateStarted.dayOfMonth, object : DatePicker.OnDateChangedListener {
            override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                current.dateStarted = LocalDate.of(year, monthOfYear + 1, dayOfMonth).toEpochDay()
            }
        })
        val dateReadView = findViewById<DatePicker>(R.id.book_edit_date_read)
        val dateRead = current.dateRead?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()
        dateReadView.init(dateRead.year, dateRead.monthValue - 1, dateRead.dayOfMonth, object : DatePicker.OnDateChangedListener {
            override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                current.dateRead = LocalDate.of(year, monthOfYear + 1, dayOfMonth).toEpochDay()
            }
        })

        val notesEdit = findViewById<EditText>(R.id.book_edit_notes)
        current.notes?.let { notesEdit.setText(it) }
        notesEdit.doAfterTextChanged { editable -> current.notes = editable?.toString() }

        val shelfDropdown = findViewById<Button>(R.id.book_edit_shelf_dropdown)
        shelfDropdown.text = current.shelfString()
        shelfDropdown.setOnClickListener { view ->
            view?.let { v ->
                val popup = PopupMenu(applicationContext, v)
                popup.menuInflater.inflate(R.menu.shelf_menu, popup.menu)
                popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                    when (menuItem.itemId) {
                        R.id.shelf_to_read -> {
                            current.shelve(ShelfType.ToReadShelf, shelfDropdown, bookViewModel)
                        }
                        R.id.shelf_currently_reading -> {
                            current.shelve(ShelfType.CurrentShelf, shelfDropdown, bookViewModel)
                        }
                        R.id.shelf_read -> {
                            current.shelve(ShelfType.ReadShelf, shelfDropdown, bookViewModel)
                        }
                        else -> {
                            true
                        }
                    }
                }
                popup.show()
            }
        }
        val ratingBar = findViewById<RatingBar>(R.id.book_edit_rating_bar)
        current.rating?.let { ratingBar.setRating(it.toFloat()) }
        // TODO Doesn't work
        ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
            current.rating = rating.toInt()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(applicationContext, BookViewActivity::class.java).apply {
                    book?.let {
                        putExtra(EXTRA_ID, it.bookId)
                    }
                }
                setResult(RESULT_CANCELED, intent)
                finish()
            }
            R.id.menu_delete -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.apply {
                    setPositiveButton("OK",
                            DialogInterface.OnClickListener { dialog, id ->
                                // User clicked OK button
                                book?.let { bookViewModel.delete(it) }
                                val intent = Intent(applicationContext, BookViewActivity::class.java)
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
            R.id.menu_save -> {
                Log.i("BK", "Saving Book ${book?.title} ${book?.bookId}")
                book?.let { bookViewModel.update(it) }
                val intent = Intent(applicationContext, BookViewActivity::class.java).apply {
                    book?.let {
                        putExtra(EXTRA_ID, it.bookId)
                    }
                }
                setResult(RESULT_OK, intent)
                finish();
            }
            R.id.menu_meta -> {
                book?.isbn13?.let { openLibraryViewModel.getBookDetails(it) }
            }
        }

        return super.onOptionsItemSelected(item)
    }
}