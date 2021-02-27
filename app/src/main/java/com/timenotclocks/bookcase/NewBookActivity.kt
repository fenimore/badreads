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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.Klaxon
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.database.*
import com.timenotclocks.bookcase.ui.main.DuplicateBookAdapter
import com.timenotclocks.bookcase.ui.main.EXTRA_BOOK
import java.time.LocalDate

const val TAG_NEW = "BookNew"
const val EXTRA_NEW = "new_book"

class NewBookActivity : AppCompatActivity() {
    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.new_book_menu, menu)


        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_book)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = DuplicateBookAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.new_book_duplicates)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(applicationContext)
        val progressBar = findViewById<ProgressBar>(R.id.duplicate_progress_bar)
        val bookId = findViewById<TextView>(R.id.new_book_id_view)

        val bookInfo: String = intent.extras?.get(EXTRA_BOOK).toString()
        Log.i(TAG_NEW, bookInfo)
        val newBook = Klaxon().fieldConverter(KlaxonDate::class, dateConverter).parse<Book>(bookInfo)
        Log.i(TAG_NEW, newBook.toString())
        Log.i(TAG_NEW, newBook.toString())
        bookId.text = "New Book ${newBook?.bookId}"
        newBook?.let { current ->
            current.cover("L").let { Picasso.get().load(it).into(findViewById<ImageView>(R.id.new_book_cover_image)) }
            current.subtitle?.let {
                findViewById<TextView>(R.id.new_book_title).text = "$current.title: $it"
            } ?: run { findViewById<TextView>(R.id.new_book_title).text = current.title }
            current.author?.let { findViewById<TextView>(R.id.new_book_author).text = it }
            current.isbn10?.let { findViewById<TextView>(R.id.new_book_isbn10).text = it }
            current.isbn13?.let { isbn -> findViewById<TextView>(R.id.new_book_isbn13).text = isbn }
            current.publisher?.let { findViewById<TextView>(R.id.new_book_publisher).text = it }
            current.year?.let { findViewById<TextView>(R.id.new_book_year).text = it.toString() }
            current.originalYear?.let { findViewById<TextView>(R.id.new_book_original_year).text = it.toString() }
            current.dateAdded = LocalDate.now()

            val shelfDropdown = findViewById<MaterialButton>(R.id.new_book_shelf_dropdown)
            shelfDropdown.text = current.shelf
            shelfDropdown.setOnClickListener { view ->
                view?.let { v -> showMenu(current, v, R.menu.shelf_menu) }
            }
        }
        val btnAdd = findViewById<MaterialButton>(R.id.new_book_btn_add)
        btnAdd.setOnClickListener {
            newBook?.let {
                val intent = Intent(this, BookViewActivity::class.java).apply {
                    putExtra(EXTRA_BOOK, Klaxon().fieldConverter(KlaxonDate::class, dateConverter).toJsonString(it))
                    putExtra(EXTRA_NEW, true)
                }
                startActivity(intent)
                finish()
            }
        }
        val btnReplace = findViewById<MaterialButton>(R.id.new_book_btn_replace)
        btnReplace.setOnClickListener {
            if (adapter.itemCount > 0) {
                Log.i(TAG_NEW, adapter.item(0).toString())
            }
        }
        val btnMerge = findViewById<MaterialButton>(R.id.new_book_btn_merge)
        btnMerge.setOnClickListener {
            if (adapter.itemCount > 0) {
                Log.i(TAG_NEW, adapter.item(0).toString())
            }
        }
        val alike = bookViewModel.findAlike("%${newBook?.title}%", newBook?.isbn10, newBook?.isbn13)
        progressBar?.visibility = View.VISIBLE
        alike.observe(this, { observed ->
            observed?.let { books ->
                progressBar?.visibility = View.GONE
                adapter.submitList(books)
                if (books.isNotEmpty()) {
                    findViewById<TextView>(R.id.new_book_duplicates_header).visibility = View.VISIBLE
                    btnReplace.visibility = View.VISIBLE
                    btnMerge.visibility = View.VISIBLE
                }
            }
        })

    }

    private fun showMenu(b: Book, v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(applicationContext, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.shelf_to_read -> {
                    setShelf(b, "to-read")
                }
                R.id.shelf_currently_reading -> {
                    setShelf(b, "currently-reading")
                }
                R.id.shelf_read -> {
                    setShelf(b, "read")
                }
                else -> {
                    true
                }
            }
        }
        popup.show()
    }

    private fun setShelf(book: Book, shelf: String): Boolean {
        if (book.shelf != shelf) {
            book.shelf = shelf
            when(shelf){
                "read" -> {book.dateRead = LocalDate.now()}
                "currently-reading" -> {book.dateStarted = LocalDate.now()}
            }
            findViewById<Button>(R.id.new_book_shelf_dropdown).text = shelf
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }

}