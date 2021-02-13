package com.timenotclocks.bookcase


import android.content.DialogInterface
import android.content.Intent
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
import androidx.lifecycle.LiveData
import com.beust.klaxon.Klaxon
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.database.Book
import com.timenotclocks.bookcase.database.BooksApplication
import com.timenotclocks.bookcase.database.KlaxonDate
import com.timenotclocks.bookcase.database.dateConverter
import com.timenotclocks.bookcase.database.BookViewModel
import com.timenotclocks.bookcase.database.BookViewModelFactory
import com.timenotclocks.bookcase.ui.main.EXTRA_BOOK
import com.timenotclocks.bookcase.ui.main.EXTRA_OPEN_LIBRARY_SEARCH
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_edit)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bookInfo: String = intent.extras?.get(EXTRA_BOOK).toString()
        book = Klaxon().fieldConverter(KlaxonDate::class, dateConverter).parse<Book>(bookInfo)

        val searchExtra: Boolean = intent.extras?.getBoolean(EXTRA_OPEN_LIBRARY_SEARCH) ?: false
        if (searchExtra) {
            Log.i("BK", "Got the extra!")
            //val alike = createIfDoesntExist(it)
            book?.let {
                bookViewModel.insert(it)
                Snackbar.make(
                        findViewById(R.id.book_edit_activity),
                        "You've added ${it.title} to your library",
                        Snackbar.LENGTH_LONG
                ).setAction("Action", null).show()
            }

        }

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

        val titleEdit = findViewById<EditText>(R.id.book_view_title)
        book?.title.let {titleEdit.setText(it)}
        titleEdit.doAfterTextChanged { editable -> book?.title = editable.toString()}

        val subTitleEdit = findViewById<EditText>(R.id.book_view_subtitle)
        book?.subtitle?.let {subTitleEdit.setText(it)}
        subTitleEdit.doAfterTextChanged { editable -> book?.subtitle = editable.toString()}

        val authorEdit = findViewById<EditText>(R.id.author)
        book?.author?.let {authorEdit.setText(it)}
        authorEdit.doAfterTextChanged { editable -> book?.author = editable.toString()}

        val isbn10Edit = findViewById<EditText>(R.id.book_view_isbn10)
        book?.isbn10?.let {isbn10Edit.setText(it.toString())}
        isbn10Edit.doAfterTextChanged { editable -> book?.isbn10 = editable.toString()}

        val isbn13Edit = findViewById<EditText>(R.id.book_view_isbn13)
        book?.isbn13?.let {isbn13Edit.setText(it.toString())}
        isbn13Edit.doAfterTextChanged { editable -> book?.isbn13 = editable.toString()}

        val publisherEdit = findViewById<EditText>(R.id.book_view_publisher)
        book?.publisher?.let {publisherEdit.setText(it.toString())}
        publisherEdit.doAfterTextChanged { book?.publisher = it.toString()}

        val yearEdit = findViewById<EditText>(R.id.book_view_year)
        book?.year?.let {yearEdit.setText(it.toString())}
        yearEdit.doAfterTextChanged { editable ->
            editable.toString().toIntOrNull().let {
                if (it is Int) {book?.year = it}
            }
        }

        val originalYearEdit = findViewById<EditText>(R.id.book_view_original_year)
        book?.originalYear?.let {originalYearEdit.setText((it.toString()))}
        originalYearEdit.doAfterTextChanged { editable ->
            editable.toString().toIntOrNull().let {
                if (it is Int) {book?.originalYear = it}
            }
        }

        val shelfSpinner = findViewById<Spinner>(R.id.shelf_spinner)
        ArrayAdapter.createFromResource(
                this,
                R.array.shelf_spinner,
                android.R.layout.simple_spinner_item,
        ).also {
            adapter ->  // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            shelfSpinner.adapter = adapter
            when(book?.shelf) {
                "to-read" -> { shelfSpinner.setSelection(0) }
                "currently-reading" -> { shelfSpinner.setSelection(1) }
                "read" -> { shelfSpinner.setSelection(2) }
            }
        }
        shelfSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position) {
                    0 -> {book?.shelf = "to-read"}
                    1 -> {book?.shelf = "currently-reading"}
                    2 -> {book?.shelf = "read"}
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val ratingBar = findViewById<RatingBar>(R.id.book_view_rating_bar)
        book?.rating?.let {ratingBar.setRating(it.toFloat())}
        ratingBar.onRatingBarChangeListener = object : RatingBar.OnRatingBarChangeListener {
            override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
                book?.rating = rating.toInt()
            }
        }

        val notesEdit = findViewById<EditText>(R.id.book_view_notes)
        book?.notes?.let {notesEdit.setText(it)}
        notesEdit.doAfterTextChanged { book?.notes = it.toString()}

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
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
                book?.let{bookViewModel.update(it)}
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

    private fun createIfDoesntExist(book: Book): List<Book> {
        return bookViewModel.findAlike(book.title, book.isbn10, book.isbn13)
    }

}