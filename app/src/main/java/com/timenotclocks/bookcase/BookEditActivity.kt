package com.timenotclocks.bookcase


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.beust.klaxon.Klaxon
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.database.*
import com.timenotclocks.bookcase.ui.main.EXTRA_BOOK
import java.time.LocalDate


const val RESULT_DELETED = 140
const val EXTRA_SAVED_BOOK = "book_changes_saved"
const val LOG_EDIT = "BookEdit"

class BookEditActivity : AppCompatActivity() {

    private var book: Book? = null
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
        Log.i(LOG_EDIT, "Editing a book")
        intent.extras?.getLong(EXTRA_ID)?.let { bookId ->
            bookViewModel.getBook(bookId).observe(this, { observable ->
                observable?.let {
                    book = it
                    populateViews(it)
                }
            })
        }
    }

    private fun populateViews(current: Book) {
        current.cover("L").let { Picasso.get().load(it).into(findViewById<ImageView>(R.id.book_edit_cover_image)) }

        val titleEdit = findViewById<TextInputLayout>(R.id.book_edit_title_layout)?.editText
        titleEdit?.setText(current.title)
        titleEdit?.doAfterTextChanged { editable -> current.title = editable.toString() }

        val subTitleEdit = findViewById<TextInputLayout>(R.id.book_edit_subtitle).editText
        subTitleEdit?.doAfterTextChanged { editable -> current.subtitle = editable.toString().ifEmpty { null } }
        current.subtitle?.let { subTitleEdit?.setText(it) }

        val authorEdit = findViewById<TextInputLayout>(R.id.book_edit_author).editText
        authorEdit?.doAfterTextChanged { editable -> current.author = editable.toString().ifEmpty { null } }
        current.author?.let { authorEdit?.setText(it) }

        val extrasEdit = findViewById<TextView>(R.id.book_edit_author_extras)
        extrasEdit.doAfterTextChanged { editable -> current.authorExtras = editable.toString().ifEmpty { null } }
        current.authorExtras?.let { extrasEdit.text = it }

        val isbn10Edit = findViewById<EditText>(R.id.book_edit_isbn10)
        isbn10Edit.doAfterTextChanged { editable -> current.isbn10 = editable.toString().ifEmpty { null } }
        current.isbn10?.let { isbn10Edit.setText(it) }

        val isbn13Edit = findViewById<EditText>(R.id.book_edit_isbn13)
        isbn13Edit.doAfterTextChanged { editable -> current.isbn13 = editable.toString().ifEmpty { null } }
        current.isbn13?.let { isbn -> isbn13Edit.setText(isbn) }

        val publisherEdit = findViewById<EditText>(R.id.book_edit_publisher)
        publisherEdit.doAfterTextChanged { editable -> current.publisher = editable.toString().ifEmpty { null } }
        current.publisher?.let { publisherEdit.setText(it) }

        val yearEdit = findViewById<EditText>(R.id.book_edit_year)
        yearEdit.doAfterTextChanged { editable -> editable.toString().toIntOrNull()?.let { year -> current.year = year } }
        current.year?.let { yearEdit.setText(it.toString()) }

        val originalYearEdit = findViewById<EditText>(R.id.book_edit_original_year)
        originalYearEdit.doAfterTextChanged { editable ->
            editable.toString().toIntOrNull()?.let { year ->
                current.originalYear = year
            }
        }
        current.originalYear?.let { originalYearEdit.setText(it.toString()) }

        val dateRead = findViewById<DatePicker>(R.id.book_edit_date_shelved)
        current.dateRead?.let {
            dateRead.init(it.year, it.monthValue, it.dayOfMonth, object : DatePicker.OnDateChangedListener {
                override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                    current.dateRead = LocalDate.of(year, monthOfYear, dayOfMonth)
                }
            })
        }
        val dateAdded = findViewById<DatePicker>(R.id.book_edit_date_added)
        current.dateAdded?.let {
            dateAdded.init(it.year, it.monthValue, it.dayOfMonth, object : DatePicker.OnDateChangedListener {
                override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                    current.dateAdded = LocalDate.of(year, monthOfYear, dayOfMonth)
                }
            })
        }
        val notesEdit = findViewById<EditText>(R.id.book_edit_notes)
        current.notes?.let { notesEdit.setText(it) }
        notesEdit.doAfterTextChanged { editable -> current.notes = editable.toString() }

        val shelfDropdown = findViewById<Button>(R.id.book_edit_shelf_dropdown)
        current.shelf.let {
            shelfDropdown.text = it
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
                book?.let { bookViewModel.update(it) }
                val intent = Intent(applicationContext, BookViewActivity::class.java).apply {
                    book?.let {
                        putExtra(EXTRA_BOOK, Klaxon().fieldConverter(KlaxonDate::class, dateConverter).toJsonString(it))
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
                        putExtra(EXTRA_BOOK, Klaxon().fieldConverter(KlaxonDate::class, dateConverter).toJsonString(it))
                    }
                }
                setResult(RESULT_OK, intent)
                finish();
            }
        }

        return super.onOptionsItemSelected(item)
    }
}