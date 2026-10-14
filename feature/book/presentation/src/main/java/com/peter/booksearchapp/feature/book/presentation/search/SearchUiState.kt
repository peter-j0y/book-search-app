package com.peter.booksearchapp.feature.book.presentation.search

import androidx.compose.runtime.Immutable
import com.peter.booksearchapp.feature.book.presentation.model.UiState
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.model.SearchSort
import com.peter.booksearchapp.feature.book.presentation.paging.AppendState

@Immutable
data class SearchUiState(
    val query: String = "",
    val sort: SearchSort = SearchSort.ACCURACY,
    val result: UiState<SearchResult> = UiState.Idle,
)

@Immutable
data class SearchResult(
    val books: List<BookUiModel>,
    val append: AppendState = AppendState.Idle,
)
