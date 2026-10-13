package com.peter.booksearchapp.feature.book.presentation.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peter.booksearchapp.feature.book.domain.model.Book

@Composable
internal fun SearchRoute(
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::onSearch,
        onKeywordClick = viewModel::onKeywordClick,
        onSortChange = viewModel::onSortChange,
        onBookClick = { book -> viewModel.findBook(book.id)?.let(onBookClick) },
        onFavoriteClick = viewModel::onFavoriteClick,
        onLoadMore = viewModel::onLoadMore,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}
