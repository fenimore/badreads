/*
* 2021 Fenimore Love
* */

package com.timenotclocks.bookcase.ui.main

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.Klaxon
import com.google.android.material.chip.Chip
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.BookViewActivity
import com.timenotclocks.bookcase.EXTRA_ID
import com.timenotclocks.bookcase.LOG_TAG
import com.timenotclocks.bookcase.R
import com.timenotclocks.bookcase.database.Book
import com.timenotclocks.bookcase.database.KlaxonDate
import com.timenotclocks.bookcase.database.ShelfType
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
                putExtra(EXTRA_ID, current.bookId)
            }
            it.context.startActivity(intent)
        }
    }

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.book_list_main_view)
        private val authorView: TextView = itemView.findViewById(R.id.book_list_sub_view)
        private val coverView: ImageView = itemView.findViewById(R.id.book_list_cover_view)
        private val yearView: TextView = itemView.findViewById(R.id.book_list_caption_view_1)
        private val dateView: TextView = itemView.findViewById(R.id.book_list_caption_view_2)
        private val shelfView: Chip = itemView.findViewById(R.id.book_list_caption_view_3)

        fun bind(book: Book?) {
            book?.let{ b ->
                titleView.text = b.subtitle?.let{ it -> b.title +  ": $it"} ?: b.title
                authorView.text = "by ${b.authorString()}"
                b.dateAdded?.let { dateView.text = "$it" }
                b.yearString()?.let { yearView.text = it }
                b.cover("M").let {Picasso.get().load(it).into(coverView)}
                shelfView.text = when(b.shelf) {
                    ShelfType.CurrentShelf.shelf -> {
                        shelfView.chipBackgroundColor = ContextCompat.getColorStateList(itemView.context, R.color.cyan)
                        "Started"
                    }
                    ShelfType.ReadShelf.shelf -> {
                        shelfView.chipBackgroundColor = ContextCompat.getColorStateList(itemView.context, R.color.secondaryColor)
                        "Read"
                    }
                    ShelfType.ToReadShelf.shelf -> {
                        shelfView.chipBackgroundColor = ContextCompat.getColorStateList(itemView.context, R.color.yellow)
                        "To Read"
                    }
                    else -> {
                        "Unshelved"
                    }
                }
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
