package com.peter.booksearchapp.feature.book.domain.usecase

import com.peter.booksearchapp.core.network.ApiResponse
import com.peter.booksearchapp.core.network.map
import com.peter.booksearchapp.feature.book.data.repository.BookRepository
import com.peter.booksearchapp.feature.book.domain.mapper.toBookSearchPage
import com.peter.booksearchapp.feature.book.domain.model.BookSearchPage
import com.peter.booksearchapp.feature.book.domain.model.BookSearchSort
import javax.inject.Inject

class SearchBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository,
) {
    suspend operator fun invoke(
        query: String,
        sort: BookSearchSort,
        page: Int,
        size: Int = PAGE_SIZE,
    ): ApiResponse<BookSearchPage> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return ApiResponse.Success(BookSearchPage(books = emptyList(), isEnd = true))

        return bookRepository.searchBooks(
            query = trimmedQuery,
            sort = sort.apiValue,
            page = page,
            size = size,
        ).map { it.toBookSearchPage(page) }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
