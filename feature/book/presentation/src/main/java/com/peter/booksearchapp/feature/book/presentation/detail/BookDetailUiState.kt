package com.peter.booksearchapp.feature.book.presentation.detail

import androidx.compose.runtime.Immutable
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel

/** 도서 상세 화면 상태. */
@Immutable
sealed interface BookDetailUiState {
    data object Loading : BookDetailUiState

    data class Success(val book: BookUiModel) : BookDetailUiState
}
