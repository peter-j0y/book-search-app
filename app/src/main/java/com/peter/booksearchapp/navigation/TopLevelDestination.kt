package com.peter.booksearchapp.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.peter.booksearchapp.feature.book.presentation.R as BookR
import com.peter.booksearchapp.feature.book.presentation.navigation.FavoriteNavKey
import com.peter.booksearchapp.feature.book.presentation.navigation.SearchNavKey

enum class TopLevelDestination(
    val key: NavKey,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
) {
    SEARCH(SearchNavKey, BookR.drawable.ic_search, BookR.string.book_search_title),
    FAVORITE(FavoriteNavKey, BookR.drawable.ic_favorite, BookR.string.book_favorite_title),
}
