package com.hera.newsapp.util.extention

import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.delay

fun SearchView.setOnQueryListener(listener: (String) -> Unit) {
    this.setOnQueryTextListener(
        object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.isNotEmpty()) {
                    listener(newText)
                }
                return true
            }
        }
    )
}