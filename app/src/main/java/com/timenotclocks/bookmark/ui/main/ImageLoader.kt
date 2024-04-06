package com.timenotclocks.bookmark.ui.main

import android.R
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.Layout
import android.util.Log
import android.util.TypedValue.TYPE_DIMENSION
import android.view.View
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide



class ImageLoader {

    fun load(context: Context, imageUrl: String, title: String, coverView: ImageView) {
        val placeholder = TextDrawable(context)

        placeholder.setText(title)
        //placeholder.textScaleX = 0.3.toFloat()

        placeholder.setTextSize(3, 0.05.toFloat())
        placeholder.textAlign = Layout.Alignment.ALIGN_CENTER
        Glide.with(context).load(imageUrl).placeholder(placeholder).into(coverView);
    }
}