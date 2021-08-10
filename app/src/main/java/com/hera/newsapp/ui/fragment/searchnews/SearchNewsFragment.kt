package com.hera.newsapp.ui.fragment.searchnews

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hera.newsapp.R
import com.hera.newsapp.data.model.Article
import com.hera.newsapp.data.model.NewsResponse
import com.hera.newsapp.databinding.FragmentSearchNewsBinding
import com.hera.newsapp.ui.MainActivity
import com.hera.newsapp.ui.MainViewModel
import com.hera.newsapp.ui.adapter.NewsAdapter
import com.hera.newsapp.util.Result
import com.hera.newsapp.util.extention.setOnQueryListener
import com.hera.newsapp.util.extention.setupScrollListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SearchNewsFragment : Fragment(R.layout.fragment_search_news), NewsAdapter.Listener {

    private lateinit var viewModel: MainViewModel
    private val newsAdapter = NewsAdapter(this)
    private var _binding: FragmentSearchNewsBinding? = null
    private val binding get() = _binding!!
    private var searchJob: Job? = null
    private var isObserverInitialized = false
    private var isPageLoading = false
    private var isLastPage = false
    private var isQueryChanged = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = (activity as MainActivity).mainViewModel
        setHasOptionsMenu(true)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchNewsBinding.bind(view)
        setupRecyclerView()
        setupSwipeRefreshLayout()
        getPreservedNewsOrLoadNew()
    }


    private fun setupRecyclerView() {
        binding.rvSearchNews.apply {
             adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setupScrollListener { recyclerView ->
                val shouldPaginate = !recyclerView.canScrollVertically(1) && !isPageLoading && !isLastPage && viewModel.searchNews != null
                if (shouldPaginate) {
                    isPageLoading = true
                    viewModel.searchNewsPage.value++
                    if (!isObserverInitialized)
                        observeSearchNews()
                }
            }
        }
    }


    private fun setupSwipeRefreshLayout() {
        binding.refreshSearchNews.setOnRefreshListener {
            viewModel.apply {
                if(searchNews != null) {
                    searchNews = null
                    if (searchNewsPage.value == 1)
                        observeSearchNews()
                    else {
                        isLastPage = false
                        searchNewsPage.value = 1
                        if (!isObserverInitialized)
                            observeSearchNews()
                    }
                }
                else
                    binding.refreshSearchNews.isRefreshing = false
            }
        }
    }


    private fun getPreservedNewsOrLoadNew() {
        when {
            viewModel.searchNews != null -> {
                newsAdapter.differ.submitList(viewModel.searchNews)
            }
            viewModel.query.value.isEmpty() -> {
                return
            }
            !isObserverInitialized -> {
                observeSearchNews()
            }
        }
    }


    private fun observeSearchNews() = lifecycleScope.launch {
        if (!isObserverInitialized)
            isObserverInitialized = true
        viewModel.searchNewsFlow.collect { result ->
            when(result) {
                is Result.Loading -> { onLoading() }
                is Result.Failure -> { onFailure(result.message!!) }
                is Result.Success -> { onSuccess(result.data!!) }
            }
        }
    }


    private fun onLoading() {
        binding.apply {
            if (refreshSearchNews.isRefreshing)
                pbLoadingSearchNews.visibility = View.GONE
            else
                binding.pbLoadingSearchNews.visibility = View.VISIBLE
        }
    }


    private fun onFailure(message: String) {
        binding.apply {
            pbLoadingSearchNews.visibility = View.GONE
            rvSearchNews.visibility = View.GONE
            if ( message == "404" ) {
                llNoInternetSearchNews.visibility = View.GONE
                llNoResultsSearchNews.visibility = View.VISIBLE
            } else {
                llNoResultsSearchNews.visibility = View.GONE
                llNoInternetSearchNews.visibility = View.VISIBLE
            }
            if (refreshSearchNews.isRefreshing)
                refreshSearchNews.isRefreshing = false
            if (isPageLoading)
                isPageLoading = false
            if (isLastPage)
                isLastPage = false
        }
        viewModel.searchNewsPage.value = 1
    }


    private fun onSuccess(newsResponse: NewsResponse) {
        binding.apply {
            pbLoadingSearchNews.visibility = View.GONE
            llNoResultsSearchNews.visibility = View.GONE
            llNoInternetSearchNews.visibility = View.GONE
            rvSearchNews.visibility = View.VISIBLE
            if (refreshSearchNews.isRefreshing)
                refreshSearchNews.isRefreshing = false
            if (isPageLoading)
                isPageLoading = false
        }
        viewModel.apply {
            if (!isQueryChanged) {
                val prevArticles = searchNews ?: listOf()
                val currentArticles = newsResponse.articles ?: listOf()
                searchNews = prevArticles + currentArticles
                if (currentArticles.isNullOrEmpty())
                    isLastPage = true
            } else {
                searchNews = newsResponse.articles
                isQueryChanged = false
            }
            newsAdapter.differ.submitList(searchNews)
        }
    }


    override fun onItemClick(article: Article) {
        val action = SearchNewsFragmentDirections.actionSearchNewsFragmentToArticleFragment(article)
        findNavController().navigate(action)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setup()
    }


    private fun SearchView.setup() {
        this.queryHint = "Search..."
        this.setOnQueryListener { query ->
            searchJob?.cancel()
            searchJob = MainScope().launch {
                delay(500)
                isQueryChanged = true
                viewModel.searchNewsPage.value = 1
                viewModel.query.value = query
                if(!isObserverInitialized)
                    observeSearchNews()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}