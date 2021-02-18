/*Fenimore Love 2021*/

package com.timenotclocks.bookcase.ui.main

import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.Klaxon
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.*
import com.timenotclocks.bookcase.ui.main.OpenLibrarySearchAdapter.SearchViewHolder
import com.timenotclocks.bookcase.database.Book


const val EXTRA_OPEN_LIBRARY_SEARCH = "open_library_search_extra"
enum class SearchTarget {
    LOCAL, OPENLIB
}


class OpenLibrarySearchAdapter() : ListAdapter<Book, SearchViewHolder>(SEARCH_COMPARATOR) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder.create(viewGroup)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: SearchViewHolder, position: Int) {
        val current = getItem(position)

        viewHolder.bindTo(current)

        viewHolder.itemView.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(viewHolder.itemView.context)
            builder.apply {
                setPositiveButton("OK"
                ) { dialog, id ->
                    val intent = Intent(it.context, BookEditActivity::class.java).apply {
                        val book: String = Klaxon().toJsonString(current)
                        putExtra(EXTRA_BOOK, book)
                        putExtra(EXTRA_OPEN_LIBRARY_SEARCH, true)
                    }
                    it.context.startActivity(intent)
                }
                setNegativeButton("CANCEL", DialogInterface.OnClickListener { dialog, id -> })
            }
            builder.setTitle(current.title)
            builder.setMessage("Add this book to your library?")
            builder.create()
            builder.show()
        }
    }

    class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val coverView: ImageView = itemView.findViewById(R.id.search_cover_view)
        private val mainView: TextView = itemView.findViewById(R.id.search_main_view)
        private val subView: TextView = itemView.findViewById(R.id.search_sub_view)
        private val bodyView: TextView = itemView.findViewById(R.id.search_body_view)
        private val captionView: TextView = itemView.findViewById(R.id.search_caption_view)
        private val isbn10View: TextView = itemView.findViewById(R.id.search_isbn10_view)
        private val isbn13View: TextView = itemView.findViewById(R.id.search_isbn13_view)

        fun bindTo(book: Book?) {
            book ?: return

            // TODO: doesnt work as expected
            Log.i(LOG_TAG, book.toString())
            Log.i(LOG_TAG, book.subtitle.toString())
            mainView.text = book.subtitle?.let{ book.title +  ": $it"} ?: book.title
            book.author?.let { subView.text = "by " + it }

            book.year?.let { bodyView.text = "$it" }
            book.originalYear?.let { captionView.text = "($it)" }

            book.cover("M").let{ Picasso.get().load(it).into(coverView)}
            book.isbn13?.let {
                isbn13View.text = it
            }
            book.isbn10?.let { isbn10View.text = it }
        }

        companion object {
            fun create(parent: ViewGroup): SearchViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.open_library_search_item_row, parent, false)
                return SearchViewHolder(view)
            }
        }
    }

    companion object {
        private val SEARCH_COMPARATOR = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem.title == newItem.title
                        && oldItem.bookId == newItem.bookId
                        && oldItem.isbn13 == newItem.isbn13
                        && oldItem.isbn10 == newItem.isbn10
                        && oldItem.numberPages == newItem.numberPages
                        && oldItem.author == newItem.author
                        && oldItem.authorExtras == newItem.authorExtras
                        && oldItem.notes == newItem.notes
                        && oldItem.year == newItem.year
                        && oldItem.originalYear == newItem.originalYear
            }
        }
    }
}

