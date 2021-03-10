/*Fenimore Love 2021*/

package com.timenotclocks.bookcase.ui.main

import android.content.DialogInterface
import android.content.Intent
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
import androidx.activity.viewModels
import com.timenotclocks.bookcase.*
import com.timenotclocks.bookcase.ui.main.OpenLibrarySearchAdapter.SearchViewHolder
import com.timenotclocks.bookcase.database.Book
import com.timenotclocks.bookcase.database.BookViewModel
import com.timenotclocks.bookcase.database.BookViewModelFactory
import com.timenotclocks.bookcase.database.BooksApplication


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
        private val captView4: TextView = itemView.findViewById(R.id.book_list_caption_view_4)
        private val captView5: TextView = itemView.findViewById(R.id.book_list_caption_view_5)
        private val captView6: TextView = itemView.findViewById(R.id.book_list_caption_view_6)

        fun bindTo(book: Book?) {
            book ?: return

            book.cover("M").let{ Picasso.get().load(it).into(coverView)}
            mainView.text = book.titleString()
            subView.text = book.authorString()

            book.numberPages?.let {captView1.text = "PageeS: $it }
            book.isbn13?.let {captView2.text = it }
            book.isbn10?.let {captView3.text = it }
            book.yearString()?.let { captView6.text = it }
            book.publisher?.let {captView4.text = it }


        }

        companion object {
            fun create(parent: ViewGroup): SearchViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.search_item_view, parent, false)
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

