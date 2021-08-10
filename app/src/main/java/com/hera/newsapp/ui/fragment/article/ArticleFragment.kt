package com.hera.newsapp.ui.fragment.article

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.hera.newsapp.R
import com.hera.newsapp.ui.MainActivity
import com.hera.newsapp.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArticleFragment : Fragment(R.layout.fragment_article) {

    private val args: ArticleFragmentArgs by navArgs()
    private lateinit var viewModel: MainViewModel
    private lateinit var webView: WebView
    private lateinit var fabSave: FloatingActionButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = (activity as MainActivity).mainViewModel
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupWebView()
        setupFabSave(view)
    }


    private fun initViews(view: View) {
        webView = view.findViewById(R.id.web_view_article)
        fabSave = view.findViewById(R.id.fab_save_article)
    }


    private fun setupWebView() {
        webView.apply {
            webViewClient = WebViewClient()
            loadUrl(args.article.url)
        }
    }


    private fun setupFabSave(view: View) {
        fabSave.setOnClickListener {
            viewModel.insert(args.article)
            Snackbar.make(view, "Article was saved", Snackbar.LENGTH_LONG).show()
        }
    }
}