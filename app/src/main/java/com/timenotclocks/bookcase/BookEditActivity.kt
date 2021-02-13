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
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.database.*
import com.timenotclocks.bookcase.ui.main.DuplicateDialogFragment
import com.timenotclocks.bookcase.ui.main.EXTRA_BOOK
import com.timenotclocks.bookcase.ui.main.EXTRA_OPEN_LIBRARY_SEARCH
import java.time.LocalDate
import java.util.*


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

        val bookInfo: String = intent.extras?.get(EXTRA_BOOK).toString()
        book = Klaxon().fieldConverter(KlaxonDate::class, dateConverter).parse<Book>(bookInfo)

        val isSearch = intent.getBooleanExtra(EXTRA_OPEN_LIBRARY_SEARCH, false)
        if (isSearch) {
            book?.let { search ->
                val alike = bookViewModel.findAlike("%${search.title}%", search.isbn10, search.isbn13)
                alike.observe(this,{ observed ->
                    observed?.let { books ->
                        Log.i(LOG_TAG, "Found duplicate book entries ${books.size}")
                        if (books.isNotEmpty()) {
                            val dialog = DuplicateDialogFragment(search, books.first())
                            dialog.show(supportFragmentManager, LOG_TAG)
                        }
                    }
                })
            }
        }

        book?.let { current ->
            // current.bookId.let { findViewById<TextView>(R.id.book_edit_id).text = it.toString() }
            current.title.let {
                val titleEdit = findViewById<EditText>(R.id.book_edit_title)
                titleEdit.doAfterTextChanged { editable -> current.title = editable.toString() }
                titleEdit.setText(it)
            }
            current.subtitle?.let {
                val subTitleEdit = findViewById<EditText>(R.id.book_edit_subtitle)
                subTitleEdit.doAfterTextChanged { editable -> current.subtitle = editable.toString() }
                subTitleEdit.setText(it)
            }
            current.author?.let {
                val authorEdit = findViewById<EditText>(R.id.book_edit_author)
                authorEdit.doAfterTextChanged { editable -> current.author = editable.toString() }
                authorEdit.setText(it)
            }
            current.authorExtras?.let { findViewById<TextView>(R.id.book_edit_author_extras).text = it }
            current.isbn10?.let {
                val isbn10Edit = findViewById<EditText>(R.id.book_edit_isbn10)
                isbn10Edit.doAfterTextChanged { editable -> current.isbn10 = editable.toString() }
                isbn10Edit.setText(it)
            }
            current.isbn13?.let { isbn ->
                val url = "https://covers.openlibrary.org/b/isbn/$isbn-L.jpg?default=false"
                val coverView = findViewById<ImageView>(R.id.book_edit_cover_image)
                Picasso.get().load(url).into(coverView)
                val isbn13Edit = findViewById<EditText>(R.id.book_edit_isbn13)
                isbn13Edit.doAfterTextChanged { editable -> current.isbn13 = editable.toString() }
                isbn13Edit.setText(isbn)
            }
            current.publisher?.let {
                val publisherEdit = findViewById<EditText>(R.id.book_edit_publisher)
                publisherEdit.doAfterTextChanged { editable -> current.publisher = editable.toString() }
                publisherEdit.setText(it)
            }
            current.year?.let {
                val yearEdit = findViewById<EditText>(R.id.book_edit_year)
                yearEdit.doAfterTextChanged { editable ->
                    editable.toString().toIntOrNull()?.let { year ->
                        current.year = year
                    }
                }
                yearEdit.setText(it.toString())
            }
            current.originalYear?.let {
                val originalYearEdit = findViewById<EditText>(R.id.book_edit_original_year)
                originalYearEdit.doAfterTextChanged { editable ->
                    editable.toString().toIntOrNull()?.let { year ->
                        current.originalYear = year
                    }
                }
                originalYearEdit.setText(it.toString())
            }
            current.dateRead?.let {
                val dateRead = findViewById<DatePicker>(R.id.book_edit_date_shelved)
                dateRead.init(it.year, it.monthValue, it.dayOfMonth,  object : DatePicker.OnDateChangedListener {
                    override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                        current.dateRead = LocalDate.of(year, monthOfYear, dayOfMonth)
                    }
                })
            }
            current.dateAdded?.let {
                val dateAdded = findViewById<DatePicker>(R.id.book_edit_date_added)
                dateAdded.init(it.year, it.monthValue, it.dayOfMonth,  object : DatePicker.OnDateChangedListener {
                    override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                        current.dateAdded = LocalDate.of(year, monthOfYear, dayOfMonth)
                    }
                })
            }
            current.notes?.let {
                val notesEdit = findViewById<EditText>(R.id.book_edit_notes)
                notesEdit.doAfterTextChanged { editable -> current.notes = editable.toString() }
                notesEdit.setText(it)
            }
            val shelfDropdown = findViewById<Button>(R.id.book_edit_shelf_dropdown)
            current.shelf.let {
                shelfDropdown.text = it
                shelfDropdown.setOnClickListener { view ->
                    view?.let { v ->
                        null  //showMenu(v, R.menu.shelf_menu)
                    }
                }
            }
            val ratingBar = findViewById<RatingBar>(R.id.book_edit_rating_bar)
            current.rating?.let { ratingBar.setRating(it.toFloat()) }
            ratingBar.onRatingBarChangeListener = object : RatingBar.OnRatingBarChangeListener {
                override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
                    current.rating = rating.toInt()
                }
            }
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
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
                                setResult(RESULT_CANCELED, intent)
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
                Log.i("BK", "Saving Book")


                val searchExtra: Boolean = intent.extras?.getBoolean(EXTRA_OPEN_LIBRARY_SEARCH)
                        ?: false
                if (searchExtra) {
                    Log.i("BK", "Got the extra!")

                    book?.let {
                        //val alike = createIfDoesntExist(it)
                        //Log.i("BK", alike.toString())
                        bookViewModel.insert(it)
                        Snackbar.make(
                                findViewById(R.id.book_edit_activity),
                                "You've added ${it.title} to your library",
                                Snackbar.LENGTH_LONG
                        ).setAction("Action", null).show()
                    }

                }

                book?.let { bookViewModel.update(it) }
                val intent = Intent(applicationContext, BookViewActivity::class.java).apply {
                    book?.let {
                        putExtra(EXTRA_BOOK, Klaxon().toJsonString(book))
                    }
                }
                setResult(RESULT_OK, intent)
                finish();
            }
        }

        return super.onOptionsItemSelected(item)
    }
}