package com.peter.booksearchapp.feature.book.presentation.model

import androidx.annotation.StringRes
import com.peter.booksearchapp.feature.book.data.local.model.FavoriteBookSort
import com.peter.booksearchapp.feature.book.domain.model.BookSearchSort
import com.peter.booksearchapp.feature.book.presentation.R

enum class SearchSort(
    @StringRes val labelRes: Int,
    val bookSearchSort: BookSearchSort,
) {
    ACCURACY(R.string.book_sort_accuracy, BookSearchSort.ACCURACY),
    LATEST(R.string.book_sort_latest, BookSearchSort.LATEST),
}

enum class FavoriteSort(
    @StringRes val labelRes: Int,
    val favoriteBookSort: FavoriteBookSort,
) {
    TITLE_ASC(R.string.book_sort_title_asc, FavoriteBookSort.TITLE_ASC),
    TITLE_DESC(R.string.book_sort_title_desc, FavoriteBookSort.TITLE_DESC),
}

enum class PriceFilter(
    @StringRes val labelRes: Int,
    val minPrice: Int?,
    val maxPrice: Int?,
) {
    ALL(R.string.book_price_filter_all, null, null),
    UNDER_10000(R.string.book_price_filter_under_10000, null, 10_000),
    FROM_10000_TO_20000(R.string.book_price_filter_10000_to_20000, 10_000, 20_000),
    FROM_20000_TO_30000(R.string.book_price_filter_20000_to_30000, 20_000, 30_000),
    OVER_30000(R.string.book_price_filter_over_30000, 30_000, null),
}
