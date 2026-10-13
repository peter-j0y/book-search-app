package com.peter.booksearchapp.feature.book.presentation.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.peter.booksearchapp.feature.book.domain.model.Book
import com.peter.booksearchapp.feature.book.presentation.detail.BookDetailPlaceholder
import com.peter.booksearchapp.feature.book.presentation.detail.BookDetailRoute
import com.peter.booksearchapp.feature.book.presentation.favorite.FavoriteRoute
import com.peter.booksearchapp.feature.book.presentation.search.SearchRoute

/**
 * 검색 · 즐겨찾기 목록은 리스트 칸, 상세는 상세 칸으로 표시합니다.
 * 넓은 화면에서는 두 칸이 나란히 보이고, 좁은 화면에서는 지금 화면 하나만 보입니다.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.bookEntries(
    onBookClick: (Book) -> Unit,
    onBackClick: () -> Unit,
) {
    val listPane = ListDetailSceneStrategy.listPane(detailPlaceholder = { BookDetailPlaceholder() })
    entry<SearchNavKey>(metadata = listPane) {
        SearchRoute(onBookClick = onBookClick)
    }
    entry<FavoriteNavKey>(metadata = listPane) {
        FavoriteRoute(onBookClick = onBookClick)
    }
    entry<BookDetailNavKey>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
        BookDetailRoute(book = key.book, onBackClick = onBackClick)
    }
}
