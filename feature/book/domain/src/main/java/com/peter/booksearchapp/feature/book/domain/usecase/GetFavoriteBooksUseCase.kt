package com.peter.booksearchapp.feature.book.domain.usecase

import com.peter.booksearchapp.feature.book.data.local.model.FavoriteBookCursor
import com.peter.booksearchapp.feature.book.data.local.model.FavoriteBookSort
import com.peter.booksearchapp.feature.book.data.repository.BookRepository
import com.peter.booksearchapp.feature.book.domain.mapper.toFavoriteBookPage
import com.peter.booksearchapp.feature.book.domain.model.FavoriteBookPage
import javax.inject.Inject

class GetFavoriteBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository,
) {
    // 첫 페이지는 after에 null을 넘기고, 다음 페이지는 직전 결과의 nextCursor를 넘깁니다.
    suspend operator fun invoke(
        sort: FavoriteBookSort,
        after: FavoriteBookCursor?,
        query: String? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        size: Int = PAGE_SIZE,
    ): FavoriteBookPage = bookRepository.getFavoriteBooks(
        sort = sort,
        after = after,
        size = size,
        query = query?.trim()?.takeIf { it.isNotEmpty() },
        minPrice = minPrice,
        maxPrice = maxPrice,
    ).toFavoriteBookPage(requestedSize = size)

    companion object {
        const val PAGE_SIZE = 20
    }
}
