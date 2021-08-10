package com.hera.newsapp.util.extention

import android.widget.AbsListView
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.setupScrollListener(_onScrollStateChange: (RecyclerView) -> Unit,) {
    this.addOnScrollListener(
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                _onScrollStateChange(recyclerView)
            }
        }
    )
}