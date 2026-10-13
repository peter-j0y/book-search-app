package com.peter.booksearchapp.feature.book.presentation.favorite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peter.booksearchapp.feature.book.domain.model.Book

@Composable
internal fun FavoriteRoute(
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoriteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoriteScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSortChange = viewModel::onSortChange,
        onPriceFilterChange = viewModel::onPriceFilterChange,
        onBookClick = { book -> viewModel.findBook(book.id)?.let(onBookClick) },
        onFavoriteClick = viewModel::onFavoriteClick,
        onLoadMore = viewModel::onLoadMore,
        modifier = modifier,
    )
}
