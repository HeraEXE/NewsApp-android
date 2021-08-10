package com.hera.newsapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hera.newsapp.data.model.Article

@Database(entities = [Article::class], version = 1)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun getNewsDao(): NewsDao
}