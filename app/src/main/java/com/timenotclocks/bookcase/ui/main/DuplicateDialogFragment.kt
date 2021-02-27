package com.timenotclocks.bookcase.ui.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.beust.klaxon.Klaxon
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.BookViewActivity
import com.timenotclocks.bookcase.R
import com.timenotclocks.bookcase.database.*


const val DIALOG_TAG = "DuplicateDialog"
const val EXTRA_SAVE = "duplicate_save_message"

class DuplicateDialogFragment(old: Book, new: Book) : DialogFragment() {
    private var toolbar: Toolbar? = null

    private val oldBook = old
    private val newBook = new

    private val serializer = Klaxon().fieldConverter(KlaxonDate::class, dateConverter)
    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((activity?.application as BooksApplication).repository)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_duplicate_dialog, container)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Add Old Book Information
        view.findViewById<TextView>(R.id.old_duplicate_title).text = oldBook.title
        oldBook.subtitle?.let {view.findViewById<TextView>(R.id.old_duplicate_subtitle).text = it}
        oldBook.author?.let {view.findViewById<TextView>(R.id.old_duplicate_author).text = it}
        oldBook.authorExtras?.let {view.findViewById<TextView>(R.id.old_duplicate_author_extras).text = it}
        oldBook.publisher?.let {view.findViewById<TextView>(R.id.old_duplicate_publisher).text = it}
        oldBook.year?.let {view.findViewById<TextView>(R.id.old_duplicate_year).text = it.toString()}
        oldBook.originalYear?.let {view.findViewById<TextView>(R.id.old_duplicate_original_year).text = it.toString()}
        oldBook.isbn10?.let {view.findViewById<TextView>(R.id.old_duplicate_isbn10).text = it}
        oldBook.isbn13?.let {
            view.findViewById<TextView>(R.id.old_duplicate_isbn13).text = it
            Picasso.get().load("https://covers.openlibrary.org/b/isbn/$it-M.jpg")
                    .into(view.findViewById<ImageView>(R.id.old_duplicate_cover_image))
        }
        // Add New Book Information
        view.findViewById<TextView>(R.id.new_duplicate_title).text = newBook.title
        newBook.subtitle?.let {view.findViewById<TextView>(R.id.new_duplicate_subtitle).text = it}
        newBook.author?.let {view.findViewById<TextView>(R.id.new_duplicate_author).text = it}
        newBook.authorExtras?.let {view.findViewById<TextView>(R.id.new_duplicate_author_extras).text = it}
        newBook.publisher?.let {view.findViewById<TextView>(R.id.new_duplicate_publisher).text = it}
        newBook.year?.let {view.findViewById<TextView>(R.id.new_duplicate_year).text = it.toString()}
        newBook.originalYear?.let {view.findViewById<TextView>(R.id.new_duplicate_original_year).text = it.toString()}
        newBook.isbn10?.let {view.findViewById<TextView>(R.id.new_duplicate_isbn10).text = it}
        newBook.isbn13?.let {
            view.findViewById<TextView>(R.id.new_duplicate_isbn13).text = it
            Picasso.get().load("https://covers.openlibrary.org/b/isbn/$it-M.jpg")
                    .into(view.findViewById<ImageView>(R.id.new_duplicate_cover_image))
        }

        toolbar = view.findViewById(R.id.duplicate_toolbar)
        toolbar?.setNavigationOnClickListener { dismiss() }
        toolbar?.inflateMenu(R.menu.duplicate_dialog_menu)
        toolbar?.setOnMenuItemClickListener { item: MenuItem? ->
            item?.itemId?.let {
                launchView(it)
                dismiss()
            }
            true
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun launchView(actionId: Int) {
        when(actionId) {
            R.id.duplicate_action_replace -> {
                newBook.bookId = oldBook.bookId
                bookViewModel.update(newBook)
            }
            R.id.duplicate_action_merge -> {
                var merged = mergeBooks(oldBook, newBook)
                merged.shelf = oldBook.shelf
                bookViewModel.update(merged)
            }
            R.id.duplicate_action_add -> {
                // Can't launch the book view because I don' know the ID
                bookViewModel.insert(newBook)
            }
            R.id.duplicate_action_cancel -> {
                dismiss()
            }
            else -> {null}
        }
        dismiss()
    }
}
