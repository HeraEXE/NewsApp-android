package com.hera.newsapp.di

import android.content.Context
import androidx.room.Room
import com.hera.newsapp.data.database.NewsDao
import com.hera.newsapp.data.database.NewsDatabase
import com.hera.newsapp.data.network.NewsApi
import com.hera.newsapp.util.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNewsApi(): NewsApi {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(NewsApi::class.java)
    }


    @Provides
    @Singleton
    fun provideNewsDao(
        @ApplicationContext context: Context
    ): NewsDao {
        val database = Room
            .databaseBuilder(
                context,
                NewsDatabase::class.java,
                "news_database"
            )
            .build()
        return database.getNewsDao()
    }
}