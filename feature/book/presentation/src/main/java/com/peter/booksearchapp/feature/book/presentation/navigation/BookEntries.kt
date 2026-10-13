package com.peter.booksearchapp.feature.book.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.peter.booksearchapp.feature.book.presentation.detail.BookDetailRoute
import com.peter.booksearchapp.feature.book.presentation.favorite.FavoriteRoute
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.search.SearchRoute

fun EntryProviderScope<NavKey>.bookEntries(
    onBookClick: (BookUiModel) -> Unit,
    onBackClick: () -> Unit,
) {
    entry<SearchNavKey> {
        SearchRoute(onBookClick = onBookClick)
    }
    entry<FavoriteNavKey> {
        FavoriteRoute(onBookClick = onBookClick)
    }
    entry<BookDetailNavKey> { key ->
        BookDetailRoute(book = key.book, onBackClick = onBackClick)
    }
}
