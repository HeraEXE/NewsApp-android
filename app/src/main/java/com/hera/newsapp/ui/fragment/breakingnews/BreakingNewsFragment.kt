 package com.hera.newsapp.ui.fragment.breakingnews

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AbsListView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hera.newsapp.R
import com.hera.newsapp.data.model.Article
import com.hera.newsapp.data.model.NewsResponse
import com.hera.newsapp.databinding.FragmentBreakingNewsBinding
import com.hera.newsapp.ui.MainActivity
import com.hera.newsapp.ui.MainViewModel
import com.hera.newsapp.ui.adapter.NewsAdapter
import com.hera.newsapp.util.Constants
import com.hera.newsapp.util.Constants.THEME_DARK
import com.hera.newsapp.util.Constants.THEME_LIGHT
import com.hera.newsapp.util.Result
import com.hera.newsapp.util.dataStore
import com.hera.newsapp.util.extention.setupScrollListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news), NewsAdapter.Listener {

    private lateinit var viewModel: MainViewModel
    private val newsAdapter = NewsAdapter(this)
    private var _binding: FragmentBreakingNewsBinding? = null
    private val binding get() = _binding!!
    private val themeKey = stringPreferencesKey(Constants.THEME_KEY)
    private lateinit var themeFlow: Flow<String>
    private var isObserverInitialized = false
    private var isPageLoading = false
    private var isLastPage = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = (activity as MainActivity).mainViewModel
        setHasOptionsMenu(true)
        setPreferencesFlow()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBreakingNewsBinding.bind(view)
        getPreferencesData()
        setupRecyclerView()
        setupSwipeRefreshLayout()
        getPreservedNewsOrLoadNew()
    }


    private fun setupRecyclerView() {
        binding.rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setupScrollListener { recyclerView ->
                val shouldPaginate = !recyclerView.canScrollVertically(1) && !isPageLoading && !isLastPage && viewModel.breakingNews != null
                if (shouldPaginate) {
                    isPageLoading = true
                    viewModel.breakingNewsPage.value++
                    if (!isObserverInitialized)
                        observeBreakingNews()
                }
            }
        }
    }


    private fun setPreferencesFlow() {
        themeFlow = requireContext().dataStore.data.map { preferences ->
            preferences[themeKey] ?: THEME_LIGHT
        }
    }


    private fun setupSwipeRefreshLayout() {
        binding.refreshBreakingNews.setOnRefreshListener {
            viewModel.apply {
                breakingNews = null
                if (breakingNewsPage.value == 1)
                    observeBreakingNews()
                else {
                    isLastPage = false
                    breakingNewsPage.value = 1
                    if (!isObserverInitialized)
                        observeBreakingNews()
                }
            }
        }
    }


    private fun getPreservedNewsOrLoadNew() {
        when {
            viewModel.breakingNews != null -> {
                newsAdapter.differ.submitList(viewModel.breakingNews)
            }
            !isObserverInitialized -> observeBreakingNews()
        }
    }


    private fun observeBreakingNews() = lifecycleScope.launch {
        if (!isObserverInitialized)
            isObserverInitialized = true
        viewModel.breakingNewsFlow.collect { result ->
            when(result) {
                is Result.Loading -> { onLoading() }
                is Result.Failure -> { onFailure() }
                is Result.Success -> { onSuccess(result.data!!) }
            }
        }
    }


    private fun onLoading() {
        binding.apply {
            if (refreshBreakingNews.isRefreshing)
                pbLoadingBreakingNews.visibility = View.GONE
            else
                pbLoadingBreakingNews.visibility = View.VISIBLE
        }
    }


    private fun onFailure() {
        binding.apply {
            pbLoadingBreakingNews.visibility = View.GONE
            rvBreakingNews.visibility = View.GONE
            llNoInternetBreakingNews.visibility = View.VISIBLE
            if(refreshBreakingNews.isRefreshing)
                refreshBreakingNews.isRefreshing = false
            if (isPageLoading)
                isPageLoading = false
            if (isLastPage)
                isLastPage = false
        }
        viewModel.breakingNewsPage.value = 1
    }


    private fun onSuccess(newsResponse: NewsResponse) {
        binding.apply {
            pbLoadingBreakingNews.visibility = View.GONE
            llNoInternetBreakingNews.visibility = View.GONE
            rvBreakingNews.visibility = View.VISIBLE
            if(refreshBreakingNews.isRefreshing)
                refreshBreakingNews.isRefreshing = false
            if (isPageLoading)
                isPageLoading = false
        }
        viewModel.apply {
            val prevArticles = breakingNews ?: listOf()
            val currentArticles = newsResponse.articles ?: listOf()
            breakingNews = prevArticles + currentArticles
            newsAdapter.differ.submitList(breakingNews)
            if (currentArticles.isNullOrEmpty())
                isLastPage = true
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_theme, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem) = if(item.itemId == R.id.action_theme) {
        setPreferencesData()
        getPreferencesData()
        true
    } else
        super.onOptionsItemSelected(item)


    private fun getPreferencesData() = MainScope().launch {
        themeFlow.collect {
            viewModel.theme = it
            if (viewModel.theme == THEME_LIGHT) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }


    private fun setPreferencesData() = MainScope().launch {
        requireContext().dataStore.edit { preferences ->
            val theme = preferences[themeKey] ?: THEME_LIGHT
            if (theme == THEME_LIGHT) { preferences[themeKey] = THEME_DARK }
            else { preferences[themeKey] = THEME_LIGHT }
        }
    }


    override fun onItemClick(article: Article) {
        val action = BreakingNewsFragmentDirections.actionBreakingNewsFragmentToArticleFragment(article)
        findNavController().navigate(action)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}