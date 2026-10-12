package com.peter.booksearchapp.feature.book.data.remote

import com.peter.booksearchapp.core.network.ApiResponse
import com.peter.booksearchapp.feature.book.data.remote.api.BookApi
import com.peter.booksearchapp.feature.book.data.remote.model.BookSearchResponse
import javax.inject.Inject

internal class BookRemoteDataSource @Inject constructor(
    private val bookApi: BookApi,
) {
    suspend fun searchBooks(
        query: String,
        sort: String? = null,
        page: Int? = null,
        size: Int? = null,
    ): ApiResponse<BookSearchResponse> = bookApi.searchBooks(query = query, sort = sort, page = page, size = size)
}
