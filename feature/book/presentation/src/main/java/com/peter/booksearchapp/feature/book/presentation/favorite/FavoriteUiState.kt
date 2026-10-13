package com.peter.booksearchapp.feature.book.presentation.favorite

import androidx.compose.runtime.Immutable
import com.peter.booksearchapp.feature.book.presentation.model.UiState
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.model.FavoriteSort
import com.peter.booksearchapp.feature.book.presentation.model.PriceFilter

@Immutable
data class FavoriteUiState(
    val query: String = "",
    val sort: FavoriteSort = FavoriteSort.TITLE_ASC,
    val priceFilter: PriceFilter = PriceFilter.ALL,
    val result: UiState<List<BookUiModel>> = UiState.Loading,
) {
    val hasCondition: Boolean
        get() = query.isNotBlank() || priceFilter != PriceFilter.ALL
}
