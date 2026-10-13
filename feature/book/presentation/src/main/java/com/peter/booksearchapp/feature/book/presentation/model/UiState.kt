package com.peter.booksearchapp.feature.book.presentation.model

import androidx.compose.runtime.Immutable
@Immutable
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data object Error : UiState<Nothing>
}
