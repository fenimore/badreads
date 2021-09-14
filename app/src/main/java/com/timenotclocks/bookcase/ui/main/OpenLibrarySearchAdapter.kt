/*Fenimore Love 2021*/

package com.timenotclocks.bookcase.ui.main

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.Klaxon
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.NewBookActivity
import com.timenotclocks.bookcase.R
import com.timenotclocks.bookcase.TAG_NEW
import com.timenotclocks.bookcase.database.Book
import com.timenotclocks.bookcase.ui.main.OpenLibrarySearchAdapter.SearchViewHolder


const val EXTRA_OPEN_LIBRARY_SEARCH = "open_library_search_extra"


class OpenLibrarySearchAdapter() : ListAdapter<Book, SearchViewHolder>(SEARCH_COMPARATOR) {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder.create(viewGroup)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: SearchViewHolder, position: Int) {
        val current = getItem(position)

        viewHolder.bindTo(current)

        viewHolder.itemView.setOnClickListener {
            val intent = Intent(it.context, NewBookActivity::class.java).apply {
                val book: String = Klaxon().toJsonString(current)
                putExtra(EXTRA_BOOK, book)
            }
            it.context.startActivity(intent)
        }
    }

    class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val coverView: ImageView = itemView.findViewById(R.id.book_list_cover_view)
        private val mainView: TextView = itemView.findViewById(R.id.book_list_main_view)
        private val subView: TextView = itemView.findViewById(R.id.book_list_sub_view)
        private val captView1: TextView = itemView.findViewById(R.id.book_list_caption_view_1)
        private val captView2: TextView = itemView.findViewById(R.id.book_list_caption_view_2)
        private val captView3: TextView = itemView.findViewById(R.id.book_list_caption_view_3)
        private val emptyCoverView: TextView = itemView.findViewById(R.id.book_list_empty_cover)
        // private val captView4: TextView = itemView.findViewById(R.id.book_list_caption_view_4)
        // private val captView5: TextView = itemView.findViewById(R.id.book_list_caption_view_5)
        // private val captView6: TextView = itemView.findViewById(R.id.book_list_caption_view_6)

        fun bindTo(book: Book?) {
            book ?: return

//            book.cover("M").let {
//                Picasso.get().load(it).into(coverView, object : Callback {
//                    override fun onSuccess() {
//                        emptyCoverView.visibility = View.VISIBLE
//                    }
//                    override fun onError(e: Exception) {}
//                })
//            }
            coverView.drawable ?: run {
                emptyCoverView.visibility = View.VISIBLE
                emptyCoverView.text = book.titleString()
            }

            mainView.text = book.titleString()
            subView.text = book.authorString()

            book.isbn13?.let {captView1.text = "$it" }
            Log.i(TAG_NEW, "FFF bindTo book: $book")
            book.yearString()?.let { captView2.text = "$it" }
        }

        companion object {
            fun create(parent: ViewGroup): SearchViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.book_view_list_item , parent, false)
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

