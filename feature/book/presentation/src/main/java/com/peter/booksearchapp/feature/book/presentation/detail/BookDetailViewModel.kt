package com.peter.booksearchapp.feature.book.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peter.booksearchapp.feature.book.domain.model.Book
import com.peter.booksearchapp.feature.book.domain.usecase.ObserveFavoriteIsbnsUseCase
import com.peter.booksearchapp.feature.book.domain.usecase.SetFavoriteUseCase
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.model.UiState
import com.peter.booksearchapp.feature.book.presentation.model.toUiModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = BookDetailViewModel.Factory::class)
class BookDetailViewModel @AssistedInject constructor(
    @Assisted private val book: Book,
    observeFavoriteIsbns: ObserveFavoriteIsbnsUseCase,
    private val setFavorite: SetFavoriteUseCase,
) : ViewModel() {
    val uiState: StateFlow<UiState<BookUiModel>> = observeFavoriteIsbns(listOf(book.isbn))
        .map { favoriteIsbns -> UiState.Success(book.toUiModel(isFavorite = book.isbn in favoriteIsbns)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun onFavoriteClick(book: BookUiModel) {
        viewModelScope.launch { setFavorite(this@BookDetailViewModel.book, isFavorite = !book.isFavorite) }
    }

    @AssistedFactory
    interface Factory {
        fun create(book: Book): BookDetailViewModel
    }
}
