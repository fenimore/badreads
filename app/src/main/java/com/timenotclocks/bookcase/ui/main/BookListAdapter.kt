/*
* 2021 Fenimore Love
* */

package com.timenotclocks.bookcase.ui.main

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.Klaxon
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.BookViewActivity
import com.timenotclocks.bookcase.LOG_TAG
import com.timenotclocks.bookcase.R
import com.timenotclocks.bookcase.database.Book
import com.timenotclocks.bookcase.database.KlaxonDate
import com.timenotclocks.bookcase.database.dateConverter


const val LOG_BOOK_ADAPTER = "BookAdapter"
const val EXTRA_BOOK = "Bookintent"

class BookListAdapter : ListAdapter<Book, BookListAdapter.BookViewHolder>(BOOKS_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {

        return BookViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val current = getItem(position)

        holder.bind(current)
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, BookViewActivity::class.java).apply {
                val book: String = Klaxon().fieldConverter(KlaxonDate::class, dateConverter).toJsonString(current)
                Log.i(LOG_BOOK_ADAPTER, book)
                putExtra(EXTRA_BOOK, book)
            }
            it.context.startActivity(intent)
        }
    }

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.book_view_item_title)
        private val authorView: TextView = itemView.findViewById(R.id.author_view)
        private val coverView: ImageView = itemView.findViewById(R.id.cover_view)
        private val dateView: TextView = itemView.findViewById(R.id.date_added_view)

        fun bind(book: Book?) {
            book?.let{ b ->
                titleView.setText(b.subtitle?.let{ it -> b.title +  ": $it"} ?: b.title)
                b.author?.let { authorView.text = "by " + it }
                b.dateRead?.let {
                    dateView.text = "Read: $it"
                } ?: b.dateAdded?.let {
                    dateView.text = "Added: $it"
                }
                b.cover("M").let {Picasso.get().load(it).into(coverView)}
            }
        }

        companion object {
            fun create(parent: ViewGroup): BookViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.book_view_list_item, parent, false)
                return BookViewHolder(view)
            }
        }
    }

    companion object {
        private val BOOKS_COMPARATOR = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem.title == newItem.title
            }
        }
    }
}
