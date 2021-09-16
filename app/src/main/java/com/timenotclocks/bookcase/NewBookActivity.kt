package com.timenotclocks.bookcase

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.beust.klaxon.Klaxon
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.api.OpenLibraryViewModel
import com.timenotclocks.bookcase.database.*
import com.timenotclocks.bookcase.ui.main.EXTRA_BOOK
import java.time.LocalDate

const val TAG_NEW = "BookNew"
const val EXTRA_NEW = "new_book"
const val EXTRA_ID = "book_id"

class NewBookActivity : AppCompatActivity() {
    private val openLibraryViewModel: OpenLibraryViewModel by viewModels()
    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_book)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val progressBar = findViewById<ProgressBar>(R.id.duplicate_progress_bar)

        val bookInfo: String? = intent.extras?.getString(EXTRA_BOOK)

        val newBook = bookInfo?.let { Klaxon().parse<Book>(it) }
        var duplicateBook: Book? = null

        openLibraryViewModel.bookDetails.observe(this, { observable ->
            observable?.let { details ->
                newBook?.isbn13 = details.isbn13 ?: newBook?.isbn13
                newBook?.isbn10 = details.isbn10 ?: newBook?.isbn10
                newBook?.publisher = details.publisher ?: newBook?.publisher
                newBook?.year = details.publishYear ?: newBook?.year
                newBook?.numberPages = details.numberPages ?: newBook?.numberPages
                newBook?.description = details.description ?: newBook?.description
                newBook?.series = details.series ?: newBook?.series
                newBook?.language = details.language ?: newBook?.language
                newBook?.selfLink = details.selfLink ?: newBook?.selfLink
                newBook?.let{ displayNewBook(it) }
            }
        })

        newBook?.let {

            it.selfLink?.let {
                openLibraryViewModel.getCobissBookDetails(it)
//                openLibraryViewModel.getGoogleBookDetails(it)
            }

//            it.isbn13?.let {
//                openLibraryViewModel.getBookDetails(it)
//            }
            displayNewBook(it)
        }

        val btnAdd = findViewById<MaterialButton>(R.id.new_book_btn_add)
        btnAdd.setOnClickListener {
            newBook?.let { bookToAdd ->
                bookViewModel.insertSync(bookToAdd).observe(this, { observed ->
                    if (observed > 0) {
                        bookToAdd.bookId = observed
                        Log.i(TAG_NEW, "Adding book: $bookToAdd")
                        val intent = Intent(this, BookViewActivity::class.java).apply {
                            putExtra(EXTRA_ID, observed)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e(TAG_NEW, "Id was not inserted: $observed")
                    }

                })
            }
        }
        val btnCancel = findViewById<MaterialButton>(R.id.new_book_btn_cancel)
        btnCancel.setOnClickListener {
            finish()
        }

        val btnReplace = findViewById<MaterialButton>(R.id.new_book_btn_replace)
        btnReplace.setOnClickListener { _ ->
            newBook?.let { newDetails ->
                duplicateBook?.let { oldDetails ->
                    newDetails.bookId = oldDetails.bookId
                    newDetails.dateAdded = oldDetails.dateAdded
                    newDetails.dateStarted = oldDetails.dateStarted
                    newDetails.dateRead = oldDetails.dateRead
                    newDetails.rating = oldDetails.rating
                    newDetails.notes = oldDetails.notes
                    newDetails.shelf = oldDetails.shelf
                }

                bookViewModel.update(newDetails)
                Log.i(TAG_NEW, "Adding new details old book id: $newDetails")
                val intent = Intent(this, BookViewActivity::class.java).apply {
                    putExtra(EXTRA_ID, newDetails.bookId)
                }
                startActivity(intent)
                finish()
            }
        }
        newBook?.let {
            progressBar?.visibility = View.VISIBLE
            bookViewModel.findAlike(it.bookId, it.title, it.subtitle, it.isbn10, it.isbn13)
        }?.observe(this, { observed ->
            progressBar?.visibility = View.GONE
            observed?.let { alike ->
                duplicateBook = alike
                displayDuplicate(alike)
            }

        })
    }

    private fun displayNewBook(current: Book) {
//        current.cover("M").let { Picasso.get().load(it).into(findViewById<ImageView>(R.id.new_book_cover_image)) }
        findViewById<TextView>(R.id.new_book_title).text = current.titleString()
        findViewById<TextView>(R.id.new_book_author).text = current.authorString()
        findViewById<TextView>(R.id.new_book_description).text = current.description
        current.yearString()?.let { findViewById<TextView>(R.id.new_book_year).text = it }
        current.isbn10?.let {
            findViewById<TextView>(R.id.new_book_isbn10).text = it
        }
        current.isbn13?.let { isbn ->
            findViewById<TextView>(R.id.new_book_isbn13).text = isbn
            findViewById<MaterialButton>(R.id.new_book_button_open_library).setOnClickListener { view ->
                val url = "https://openlibrary.org/isbn/$isbn"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        }
        current.publisher?.let { findViewById<TextView>(R.id.new_book_publisher).text = it }
        current.dateAdded = LocalDate.now().toEpochDay()
        findViewById<TextView>(R.id.new_book_page_numbers).text = current.numberPages?.let {"Pages: $it"}
        findViewById<TextView>(R.id.new_book_series).text = current.series?.let {  "Series: $it" }
        findViewById<TextView>(R.id.new_book_language).text = current.language?.let {  "Language: $it" }

        val shelfDropdown = findViewById<MaterialButton>(R.id.new_book_shelf_dropdown)
        shelfDropdown.text = current.shelf
        shelfDropdown.setOnClickListener { view ->
            view?.let { v -> showMenu(current, v, R.menu.shelf_menu) }
        }
    }

    private fun displayDuplicate(alike: Book) {
        alike.cover("M")?.let {
            Picasso.get().load(it).into(findViewById<ImageView>(R.id.duplicate_book_cover_image))
        }
        findViewById<TextView>(R.id.duplicate_book_title).text = alike.titleString()
        findViewById<TextView>(R.id.duplicate_book_author).text = alike.authorString()
        alike.isbn10?.let { findViewById<TextView>(R.id.duplicate_book_isbn10).text = it }
        alike.isbn13?.let { isbn -> findViewById<TextView>(R.id.duplicate_book_isbn13).text = isbn }
        alike.publisher?.let { findViewById<TextView>(R.id.duplicate_book_publisher).text = it }
        alike.yearString()?.let { findViewById<TextView>(R.id.duplicate_book_year).text = it }
        alike.dateAdded = LocalDate.now().toEpochDay()

        val dupShelfDropdown = findViewById<MaterialButton>(R.id.duplicate_book_shelf_dropdown)
        dupShelfDropdown.text = alike.shelfString()
        dupShelfDropdown.isClickable = false

        findViewById<TextView>(R.id.new_book_duplicates_header).visibility = View.VISIBLE
        findViewById<CardView>(R.id.duplicate_card_view).visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.new_book_btn_add).text = "Add Anyway"
        findViewById<MaterialButton>(R.id.new_book_btn_replace).visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.duplicate_book_view_book_button).setOnClickListener { view ->
            val intent = Intent(applicationContext, BookViewActivity::class.java).apply {
                putExtra(EXTRA_ID, alike.bookId)
            }
            startActivity(intent)
        }
    }

    private fun showMenu(b: Book, v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(applicationContext, v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        val btn = findViewById<Button>(R.id.new_book_shelf_dropdown)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.shelf_to_read -> {
                    b.shelve(ShelfType.ToReadShelf, btn, bookViewModel)
                }
                R.id.shelf_currently_reading -> {
                    b.shelve(ShelfType.CurrentShelf, btn, bookViewModel)
                }
                R.id.shelf_read -> {
                    b.shelve(ShelfType.ReadShelf, btn, bookViewModel)
                }
            }
            true
        }
        popup.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }

}