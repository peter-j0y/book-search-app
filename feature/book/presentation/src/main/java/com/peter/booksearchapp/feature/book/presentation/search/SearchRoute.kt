package com.peter.booksearchapp.feature.book.presentation.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel

// TODO: SearchViewModel을 연결하면 상태와 이벤트를 ViewModel에서 받습니다.
@Composable
internal fun SearchRoute(
    onBookClick: (BookUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchScreen(
        uiState = SearchUiState(),
        onQueryChange = {},
        onSearch = {},
        onSortChange = {},
        onBookClick = onBookClick,
        onFavoriteClick = {},
        onLoadMore = {},
        onRetry = {},
        modifier = modifier,
    )
}
