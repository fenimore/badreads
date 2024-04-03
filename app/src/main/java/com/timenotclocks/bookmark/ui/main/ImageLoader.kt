package com.timenotclocks.bookmark.ui.main.ImageLoader

import android.R
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso


class ImageLoader {
    fun load(imageUrl: String, coverView: ImageView, emptyCoverView: TextView?) {
        Picasso.get().load(imageUrl).into(coverView, object : Callback {
            override fun onSuccess() {
                emptyCoverView?.visibility = View.INVISIBLE
            }
            override fun onError(e: Exception) {}
        })

        Picasso.get().load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE)
            .into(coverView, object : Callback {
                override fun onSuccess() {
                    emptyCoverView?.visibility = View.INVISIBLE
                }
                override fun onError(e: Exception) {
                    // The first attempt looks at the offline cache
                    // If that fails, fetch it from the server
                    // Hacky: https://stackoverflow.com/questions/23978828/how-do-i-use-disk-caching-in-picasso
                    Picasso.get().load(imageUrl).into(coverView, object : Callback {
                            override fun onSuccess() {
                                emptyCoverView?.visibility = View.INVISIBLE
                            }
                            override fun onError(e: Exception) {}
                        })
                }
            })
    }
    override fun toString(): String {
        return "ImageLoader()"
    }

}