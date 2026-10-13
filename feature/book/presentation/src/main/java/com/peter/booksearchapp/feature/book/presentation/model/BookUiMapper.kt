package com.peter.booksearchapp.feature.book.presentation.model

import com.peter.booksearchapp.feature.book.domain.model.Book

// datetime("2017-10-31T00:00:00.000+09:00")에서 날짜 부분("2017-10-31")의 길이
private const val DATE_LENGTH = 10

internal fun Book.toUiModel(isFavorite: Boolean): BookUiModel = BookUiModel(
    id = isbn,
    title = title,
    authors = authors,
    publisher = publisher,
    publishedDate = datetime.take(DATE_LENGTH).replace('-', '.'),
    isbn = isbn,
    price = price,
    salePrice = salePrice,
    thumbnailUrl = thumbnail,
    contents = contents,
    isFavorite = isFavorite,
)
