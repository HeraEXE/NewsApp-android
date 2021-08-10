package com.hera.newsapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hera.newsapp.data.model.Article
import com.hera.newsapp.data.model.NewsResponse
import com.hera.newsapp.data.repository.Repository
import com.hera.newsapp.util.Constants.THEME_LIGHT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private  val repository: Repository
) : ViewModel() {

    // Network
    var breakingNewsPage = MutableStateFlow(1)
    var breakingNews: List<Article>? = null
    val breakingNewsFlow = breakingNewsPage.flatMapLatest { page ->
        repository.getBreakingNews(page = page)
    }

    val query = MutableStateFlow("")
    val searchNewsPage = MutableStateFlow(1)
    var searchNews: List<Article>? = null
    val searchNewsFlow = combine(query, searchNewsPage) { query, searchNewsPage ->
        Pair(query, searchNewsPage)
    }.flatMapLatest { (query, searchNewsPage) ->
        repository.getSearchNews(query, searchNewsPage)
    }


    // Database
    val allSavedArticlesFlow = repository.getAllSavedArticles()

    fun insert(article: Article) = viewModelScope.launch {
        repository.insert(article)
    }

    fun delete(article: Article) = viewModelScope.launch {
        repository.delete(article)
    }

    var theme = THEME_LIGHT
}