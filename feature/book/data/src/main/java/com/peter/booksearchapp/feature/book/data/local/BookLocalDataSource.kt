package com.peter.booksearchapp.feature.book.data.local

import com.peter.booksearchapp.core.database.dao.FavoriteBookDao
import com.peter.booksearchapp.core.database.entity.FavoriteBookEntity
import javax.inject.Inject

internal class BookLocalDataSource @Inject constructor(
    private val favoriteBookDao: FavoriteBookDao,
) {
    suspend fun getFavoriteBooks(page: Int, size: Int): List<FavoriteBookEntity> =
        favoriteBookDao.getFavoriteBooks(limit = size, offset = (page - 1) * size)

    suspend fun addFavoriteBook(book: FavoriteBookEntity) = favoriteBookDao.insert(book)

    suspend fun removeFavoriteBook(isbn: String) = favoriteBookDao.deleteByIsbn(isbn)
}
