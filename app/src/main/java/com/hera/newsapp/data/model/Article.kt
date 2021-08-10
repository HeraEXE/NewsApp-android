package com.hera.newsapp.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "article_table")
data class Article(
    @Embedded
    val source: Source?,
    val author: String?,
    val title: String?,
    val description: String?,
    @PrimaryKey(autoGenerate = false)
    val url: String,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?,
) : Parcelable


@Parcelize
data class Source(
    val id: String?,
    val name: String?
) : Parcelable
