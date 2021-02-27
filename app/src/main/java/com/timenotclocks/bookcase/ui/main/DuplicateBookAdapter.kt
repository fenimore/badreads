package com.timenotclocks.bookcase.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.R
import com.timenotclocks.bookcase.database.Book

class DuplicateBookAdapter() : ListAdapter<Book, DuplicateBookAdapter.BookViewHolder>(BOOK_COMPARATOR) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BookViewHolder {
        return BookViewHolder.create(viewGroup)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: BookViewHolder, position: Int) {
        val current = getItem(position)

        viewHolder.bindTo(current)
        viewHolder.itemView.setOnClickListener {}
    }

    fun item(pos: Int): Book {
        return getItem(pos)
    }

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val coverView: ImageView = itemView.findViewById(R.id.search_cover_view)
        private val mainView: TextView = itemView.findViewById(R.id.search_main_view)
        private val subView: TextView = itemView.findViewById(R.id.search_sub_view)
        private val captView1: TextView = itemView.findViewById(R.id.search_caption_view_1)
        private val captView2: TextView = itemView.findViewById(R.id.search_caption_view_2)

        fun bindTo(book: Book?) {
            book ?: return

            // TODO: doesnt work as expected
            mainView.text = book.subtitle?.let{ book.title +  ": $it"} ?: book.title
            book.author?.let { subView.text = "by " + it }

            book.year?.let { captView1.text = "$it" }
            book.originalYear?.let { captView2.text = "($it)" }

            book.cover("M").let{ Picasso.get().load(it).into(coverView)}
        }

        companion object {
            fun create(parent: ViewGroup): BookViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.search_item_view, parent, false)
                return BookViewHolder(view)
            }
        }
    }

    companion object {
        private val BOOK_COMPARATOR = object : DiffUtil.ItemCallback<Book>() {
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