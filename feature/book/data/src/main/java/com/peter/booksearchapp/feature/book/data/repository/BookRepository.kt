package com.peter.booksearchapp.feature.book.data.repository

import com.peter.booksearchapp.core.network.ApiResponse
import com.peter.booksearchapp.feature.book.data.remote.BookRemoteDataSource
import com.peter.booksearchapp.feature.book.data.remote.model.BookSearchResponse
import javax.inject.Inject

class BookRepository @Inject internal constructor(
    private val remoteDataSource: BookRemoteDataSource,
) {
    suspend fun searchBooks(
        query: String,
        sort: String? = null,
        page: Int? = null,
        size: Int? = null,
    ): ApiResponse<BookSearchResponse> = remoteDataSource.searchBooks(query = query, sort = sort, page = page, size = size)
}
