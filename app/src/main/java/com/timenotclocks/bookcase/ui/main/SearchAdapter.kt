/*Fenimore Love 2021*/

package com.timenotclocks.bookcase.ui.main

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.timenotclocks.bookcase.R
import com.timenotclocks.bookcase.ui.main.SearchAdapter.SearchViewHolder
import com.timenotclocks.bookcase.database.Book

class SearchAdapter() : ListAdapter<Book, SearchViewHolder>(SEARCH_COMPARATOR) {


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): SearchViewHolder {
        Log.i("BK", "Search Create view Holder")
        return SearchViewHolder.create(viewGroup)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: SearchViewHolder, position: Int) {
        Log.i("BK", "Search View minding")

        val current = getItem(position)

        viewHolder.bind(current)
    }

    class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.row_item_title_view)

        fun bind(book: Book?) {
            Log.i("BK", "Get that book ${book.toString()}")
            book?.title?.let { titleView.setText(it) }
        }

        companion object {
            fun create(parent: ViewGroup): SearchViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.row_item, parent, false)
                Log.i("BK", "Creating the Holder")
                return SearchViewHolder(view)
            }
        }
    }

    companion object {
        private val SEARCH_COMPARATOR = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
                Log.i("BK", "DUHH $oldItem and=== $newItem")
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
                Log.i("BK", "$oldItem and $newItem")
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

    override fun submitList(list: List<Book>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

}

