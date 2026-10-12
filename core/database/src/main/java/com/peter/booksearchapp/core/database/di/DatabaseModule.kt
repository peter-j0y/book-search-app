package com.peter.booksearchapp.core.database.di

import android.content.Context
import androidx.room.Room
import com.peter.booksearchapp.core.database.BookSearchDatabase
import com.peter.booksearchapp.core.database.dao.FavoriteBookDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    private const val DATABASE_NAME = "book_search.db"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BookSearchDatabase =
        Room.databaseBuilder(context, BookSearchDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun provideFavoriteBookDao(database: BookSearchDatabase): FavoriteBookDao = database.favoriteBookDao()
}
