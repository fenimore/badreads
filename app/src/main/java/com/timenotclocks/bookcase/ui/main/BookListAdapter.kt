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
import androidx.activity.viewModels

import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.BookViewActivity
import com.timenotclocks.bookcase.EXTRA_ID
import com.timenotclocks.bookcase.R
import com.timenotclocks.bookcase.database.*
import kotlin.coroutines.coroutineContext


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
            Log.i("BookId", "Loading book ${current.bookId}")
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
        private val emptyCoverView: TextView = itemView.findViewById(R.id.book_list_empty_cover)
        private val yearView: TextView = itemView.findViewById(R.id.book_list_caption_view_1)
        // private val dateView: TextView = itemView.findViewById(R.id.book_list_caption_view_2)
        // private val shelfView: TextView = itemView.findViewById(R.id.book_list_caption_view_3)
        private val ratingView: RatingBar = itemView.findViewById(R.id.book_list_rating_bar)
        private val bookmarkView: ImageView = itemView.findViewById(R.id.book_list_bookmark)

        fun bind(book: Book?) {
            book?.let { b ->
                titleView.text = b.titleString()
                authorView.text = "by ${b.authorString()}"
                emptyCoverView.text = b.titleString()
                b.yearString()?.let { yearView.text = it }
                b.cover("M").let {
                    Picasso.get().load(it).into(coverView, object : Callback {
                        override fun onSuccess() {
                            emptyCoverView.visibility = View.INVISIBLE
                        }
                        override fun onError(e: Exception) {}
                    })
                }
                coverView.drawable ?: run {
                    emptyCoverView.visibility = View.VISIBLE
                }
                if (b.bookmark) {
                    bookmarkView.setImageResource(R.drawable.ic_bookmark)
                } else {
                    bookmarkView.setImageResource(R.drawable.ic_bookmark_empty)
                }
                when (b.shelf) {
                    ShelfType.ReadShelf.shelf -> {
                        b.rating?.let {
                            ratingView.visibility = View.VISIBLE
                            ratingView.rating = it.toFloat()
                        }
                    }
                    // shelfView.text = "Currently Reading"
                    // shelfView.visibility = View.VISIBLE
                    else -> {
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
