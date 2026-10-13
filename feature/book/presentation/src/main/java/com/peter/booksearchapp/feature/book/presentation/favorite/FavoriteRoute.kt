package com.peter.booksearchapp.feature.book.presentation.favorite

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.model.UiState

// TODO: FavoriteViewModel을 연결하면 상태와 이벤트를 ViewModel에서 받습니다.
@Composable
internal fun FavoriteRoute(
    onBookClick: (BookUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    FavoriteScreen(
        uiState = FavoriteUiState(result = UiState.Success(emptyList())),
        onQueryChange = {},
        onSortChange = {},
        onPriceFilterChange = {},
        onBookClick = onBookClick,
        onFavoriteClick = {},
        modifier = modifier,
    )
}
