package com.peter.booksearchapp.feature.book.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.model.UiState

// TODO: BookDetailViewModel을 연결하면 즐겨찾기 상태를 저장소에서 구독합니다.
@Composable
internal fun BookDetailRoute(
    book: BookUiModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BookDetailScreen(
        uiState = UiState.Success(book),
        onBackClick = onBackClick,
        onFavoriteClick = {},
        modifier = modifier,
    )
}
