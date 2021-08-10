package com.hera.newsapp.data.database

import androidx.room.*
import com.hera.newsapp.data.model.Article
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: Article)


    @Delete
    suspend fun deleteArticle(article: Article)


    @Query("SELECT * FROM article_table")
    fun getAllSavedArticles(): Flow<List<Article>>
}