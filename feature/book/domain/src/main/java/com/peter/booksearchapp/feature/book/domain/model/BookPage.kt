package com.peter.booksearchapp.feature.book.domain.model

import com.peter.booksearchapp.feature.book.data.local.model.FavoriteBookCursor

data class BookSearchPage(
    val books: List<Book>,
    val isEnd: Boolean,
)

data class FavoriteBookPage(
    val books: List<Book>,
    val nextCursor: FavoriteBookCursor?,
)
