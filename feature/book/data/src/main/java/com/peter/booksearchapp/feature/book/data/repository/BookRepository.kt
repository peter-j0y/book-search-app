package com.peter.booksearchapp.feature.book.data.repository

import com.peter.booksearchapp.core.database.entity.FavoriteBookEntity
import com.peter.booksearchapp.core.network.ApiResponse
import com.peter.booksearchapp.feature.book.data.local.BookLocalDataSource
import com.peter.booksearchapp.feature.book.data.local.model.FavoriteBookCursor
import com.peter.booksearchapp.feature.book.data.local.model.FavoriteBookSort
import com.peter.booksearchapp.feature.book.data.remote.BookRemoteDataSource
import com.peter.booksearchapp.feature.book.data.remote.model.BookSearchResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookRepository @Inject internal constructor(
    private val remoteDataSource: BookRemoteDataSource,
    private val localDataSource: BookLocalDataSource,
) {
    suspend fun searchBooks(
        query: String,
        sort: String? = null,
        page: Int? = null,
        size: Int? = null,
    ): ApiResponse<BookSearchResponse> = remoteDataSource.searchBooks(query = query, sort = sort, page = page, size = size)

    suspend fun getFavoriteBooks(
        sort: FavoriteBookSort,
        after: FavoriteBookCursor?,
        size: Int,
        query: String? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
    ): List<FavoriteBookEntity> = localDataSource.getFavoriteBooks(
        sort = sort,
        after = after,
        query = query,
        minPrice = minPrice,
        maxPrice = maxPrice,
        size = size,
    )

    fun observeFavoriteIsbns(isbns: List<String>): Flow<List<String>> = localDataSource.observeFavoriteIsbns(isbns)

    suspend fun addFavoriteBook(book: FavoriteBookEntity) = localDataSource.addFavoriteBook(book)

    suspend fun removeFavoriteBook(isbn: String) = localDataSource.removeFavoriteBook(isbn)
}
