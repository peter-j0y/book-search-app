package com.peter.booksearchapp.feature.book.presentation.favorite

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.peter.booksearchapp.feature.book.presentation.model.UiState
import com.peter.booksearchapp.feature.book.presentation.R
import com.peter.booksearchapp.feature.book.presentation.component.BookList
import com.peter.booksearchapp.feature.book.presentation.component.BookListHeader
import com.peter.booksearchapp.feature.book.presentation.component.BookListScaffold
import com.peter.booksearchapp.feature.book.presentation.component.BookSearchBar
import com.peter.booksearchapp.feature.book.presentation.component.HeaderOptionButton
import com.peter.booksearchapp.feature.book.presentation.component.LoadingContent
import com.peter.booksearchapp.feature.book.presentation.component.MessageContent
import com.peter.booksearchapp.feature.book.presentation.component.SortMenuButton
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.model.FavoriteSort
import com.peter.booksearchapp.feature.book.presentation.model.PriceFilter
import com.peter.booksearchapp.feature.book.presentation.preview.PreviewBooks

@Composable
fun FavoriteScreen(
    uiState: FavoriteUiState,
    onQueryChange: (String) -> Unit,
    onSortChange: (FavoriteSort) -> Unit,
    onPriceFilterChange: (PriceFilter) -> Unit,
    onBookClick: (BookUiModel) -> Unit,
    onFavoriteClick: (BookUiModel) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPriceFilter by rememberSaveable { mutableStateOf(false) }

    BookListScaffold(
        title = stringResource(R.string.book_favorite_title),
        modifier = modifier,
        searchBar = {
            BookSearchBar(query = uiState.query, onQueryChange = onQueryChange)
        },
        header = {
            BookListHeader(label = headerLabel(uiState.sort, uiState.priceFilter)) {
                HeaderOptionButton(
                    text = stringResource(R.string.book_filter),
                    iconRes = R.drawable.ic_filter,
                    onClick = { showPriceFilter = true },
                )
                SortMenuButton(
                    options = FavoriteSort.entries,
                    selected = uiState.sort,
                    optionLabel = { stringResource(it.labelRes) },
                    onSelect = onSortChange,
                )
            }
        },
    ) { contentPadding ->
        val paddingModifier = Modifier.padding(contentPadding)
        when (val result = uiState.result) {
            UiState.Idle, UiState.Loading -> LoadingContent(paddingModifier)
            UiState.Error -> MessageContent(stringResource(R.string.book_favorite_error), paddingModifier)
            is UiState.Success -> when {
                result.data.isNotEmpty() -> BookList(
                    contentPadding = contentPadding,
                    books = result.data,
                    onBookClick = onBookClick,
                    onFavoriteClick = onFavoriteClick,
                    canLoadMore = true,
                    onLoadMore = onLoadMore,
                )
                uiState.hasCondition -> MessageContent(stringResource(R.string.book_favorite_no_match), paddingModifier)
                else -> MessageContent(stringResource(R.string.book_favorite_empty), paddingModifier)
            }
        }
    }

    if (showPriceFilter) {
        PriceFilterSheet(
            selected = uiState.priceFilter,
            onSelect = onPriceFilterChange,
            onDismiss = { showPriceFilter = false },
        )
    }
}

@Composable
private fun headerLabel(sort: FavoriteSort, priceFilter: PriceFilter): String {
    val sortLabel = stringResource(sort.labelRes)
    return if (priceFilter == PriceFilter.ALL) {
        sortLabel
    } else {
        "$sortLabel · ${stringResource(priceFilter.labelRes)}"
    }
}

@Composable
private fun FavoriteScreenPreview(uiState: FavoriteUiState) {
    MaterialTheme {
        FavoriteScreen(
            uiState = uiState,
            onQueryChange = {},
            onSortChange = {},
            onPriceFilterChange = {},
            onBookClick = {},
            onFavoriteClick = {},
            onLoadMore = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoriteScreenSuccessPreview() {
    FavoriteScreenPreview(
        FavoriteUiState(
            result = UiState.Success(PreviewBooks.books.map { it.copy(isFavorite = true) }),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun FavoriteScreenFilteredPreview() {
    FavoriteScreenPreview(
        FavoriteUiState(
            query = "코틀린",
            sort = FavoriteSort.TITLE_DESC,
            priceFilter = PriceFilter.OVER_30000,
            result = UiState.Success(listOf(PreviewBooks.book)),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun FavoriteScreenEmptyPreview() {
    FavoriteScreenPreview(FavoriteUiState(result = UiState.Success(emptyList())))
}

@Preview(showBackground = true)
@Composable
private fun FavoriteScreenNoMatchPreview() {
    FavoriteScreenPreview(
        FavoriteUiState(
            query = "없는 책",
            priceFilter = PriceFilter.UNDER_10000,
            result = UiState.Success(emptyList()),
        ),
    )
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun FavoriteScreenTabletPreview() {
    FavoriteScreenPreview(
        FavoriteUiState(
            result = UiState.Success(PreviewBooks.books.map { it.copy(isFavorite = true) }),
        ),
    )
}
