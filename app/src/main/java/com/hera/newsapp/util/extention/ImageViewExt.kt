package com.hera.newsapp.util.extention

import android.widget.ImageView
import com.hera.newsapp.R
import com.squareup.picasso.Picasso

fun ImageView.loadUrl(url: String) {
    Picasso.get()
        .load(url)
        .placeholder(R.drawable.placeholder_img)
        .error(R.drawable.ic_warning)
        .into(this)
}