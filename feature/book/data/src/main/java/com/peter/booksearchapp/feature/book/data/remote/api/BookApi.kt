package com.peter.booksearchapp.feature.book.data.remote.api

import com.peter.booksearchapp.core.network.ApiResponse
import com.peter.booksearchapp.feature.book.data.remote.model.BookSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

internal interface BookApi {
    @GET("v3/search/book")
    suspend fun searchBooks(
        @Query("query") query: String,
        @Query("sort") sort: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
    ): ApiResponse<BookSearchResponse>
}
