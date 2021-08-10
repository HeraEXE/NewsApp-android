package com.hera.newsapp.data.repository

import android.util.Log
import com.hera.newsapp.data.database.NewsDao
import com.hera.newsapp.data.model.Article
import com.hera.newsapp.data.network.NewsApi
import com.hera.newsapp.util.Result
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class Repository @Inject constructor(
    private val newsApi: NewsApi,
    private val newsDao: NewsDao
) {
    fun getBreakingNews(page: Int) = flow {
        emit(Result.Loading())
        val response = try {
            newsApi.getBreakingNews(page = page)
        } catch (e: IOException) {
            emit(Result.Failure(e.message))
            return@flow
        } catch (e: HttpException) {
            emit(Result.Failure(e.message))
            return@flow
        }
        if (response.isSuccessful && response.body() != null) {
            emit(Result.Success(response.body()!!))
        }
    }
 

    fun getSearchNews(query: String, page: Int) = flow {
        emit(Result.Loading())
        val response = try {
            newsApi.getSearchNews(query, page)
        } catch (e: IOException) {
            emit(Result.Failure(e.message))
            return@flow
        } catch (e: HttpException) {
            emit(Result.Failure(e.message))
            return@flow
        }
        if (response.isSuccessful && response.body() != null) {
            val data = response.body()!!
            if (data.totalResults == 0) {
                emit(Result.Failure("404"))
                return@flow
            } else {
                emit(Result.Success(data))
            }
        }
    }


    suspend fun insert(article: Article) = newsDao.insert(article)


    suspend fun delete(article: Article) = newsDao.deleteArticle(article)


    fun getAllSavedArticles() = newsDao.getAllSavedArticles()
}