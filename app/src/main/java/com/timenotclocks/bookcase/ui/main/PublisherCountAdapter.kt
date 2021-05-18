/*
* 2021 Fenimore Love
* */

package com.timenotclocks.PublisherCountcase.ui.main

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView
import com.timenotclocks.bookcase.ChartActivity
import com.timenotclocks.bookcase.R
import com.timenotclocks.bookcase.database.PublisherCount


const val LOG_PUB = "BookPublisherCount"

class PublisherCountAdapter : ListAdapter<PublisherCount, PublisherCountAdapter.PublisherCountViewHolder>(PUB_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublisherCountViewHolder {
        Log.d(LOG_PUB, "create view holder")
        return PublisherCountViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: PublisherCountViewHolder, position: Int) {
        Log.d(LOG_PUB, "Position $position")
        val current = getItem(position)
        holder.bind(current)
        // TODO: intent for activity with the books with this publisher listed
        // holder.itemView.setOnClickListener {
        //val intent = Intent(it.context, ChartActivity::class.java)
        //it.context.startActivity(intent)
        //}

    }

    class PublisherCountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val publisherView: TextView = itemView.findViewById(R.id.pub_cnt_publisher)
        private val countView: TextView = itemView.findViewById(R.id.pub_cnt_count)

        fun bind(pubCount: PublisherCount?) {
            pubCount?.let {
                Log.d(LOG_PUB, "Binding ${it.publisher}")
                publisherView.text = it.publisher
                countView.text = "${it.count}"
            }
        }

        companion object {
            fun create(parent: ViewGroup): PublisherCountViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.publisher_count_row, parent, false)
                Log.d(LOG_PUB, "Create")
                return PublisherCountViewHolder(view)
            }
        }
    }

    companion object {
        private val PUB_COMPARATOR = object : DiffUtil.ItemCallback<PublisherCount>() {
            override fun areItemsTheSame(oldItem: PublisherCount, newItem: PublisherCount): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: PublisherCount, newItem: PublisherCount): Boolean {
                return oldItem.publisher == newItem.publisher && oldItem.count == newItem.count
            }
        }
    }
}
