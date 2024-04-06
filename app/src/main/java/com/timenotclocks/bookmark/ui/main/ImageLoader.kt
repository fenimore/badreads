package com.timenotclocks.bookmark.ui.main

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide



class ImageLoader {

    fun load(context: Context, imageUrl: String, coverView: ImageView, placeholder: Int) {
        Glide.with(context).load(imageUrl).placeholder(placeholder).into(coverView);
    }
}