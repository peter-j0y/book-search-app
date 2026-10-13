package com.peter.booksearchapp.feature.book.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.peter.booksearchapp.feature.book.presentation.model.UiState
import com.peter.booksearchapp.feature.book.presentation.R
import com.peter.booksearchapp.feature.book.presentation.component.BookList
import com.peter.booksearchapp.feature.book.presentation.component.BookListHeader
import com.peter.booksearchapp.feature.book.presentation.component.BookListScaffold
import com.peter.booksearchapp.feature.book.presentation.component.BookSearchBar
import com.peter.booksearchapp.feature.book.presentation.component.ErrorContent
import com.peter.booksearchapp.feature.book.presentation.component.LoadingContent
import com.peter.booksearchapp.feature.book.presentation.component.MessageContent
import com.peter.booksearchapp.feature.book.presentation.component.SortMenuButton
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.model.SearchSort
import com.peter.booksearchapp.feature.book.presentation.preview.PreviewBooks

@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSortChange: (SearchSort) -> Unit,
    onBookClick: (BookUiModel) -> Unit,
    onFavoriteClick: (BookUiModel) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BookListScaffold(
        title = stringResource(R.string.book_search_title),
        modifier = modifier,
        searchBar = {
            BookSearchBar(query = uiState.query, onQueryChange = onQueryChange, onSearch = onSearch)
        },
        header = {
            BookListHeader(label = stringResource(uiState.sort.labelRes)) {
                SortMenuButton(
                    options = SearchSort.entries,
                    selected = uiState.sort,
                    optionLabel = { stringResource(it.labelRes) },
                    onSelect = onSortChange,
                )
            }
        },
    ) {
        when (val result = uiState.result) {
            UiState.Idle -> MessageContent(stringResource(R.string.book_search_idle))
            UiState.Loading -> LoadingContent()
            UiState.Error -> ErrorContent(onRetry = onRetry)
            is UiState.Success -> if (result.data.books.isEmpty()) {
                MessageContent(stringResource(R.string.book_search_empty))
            } else {
                BookList(
                    books = result.data.books,
                    onBookClick = onBookClick,
                    onFavoriteClick = onFavoriteClick,
                    onLoadMore = onLoadMore.takeIf { result.data.append == AppendState.Idle },
                    footer = { appendFooter(result.data.append, onRetry) },
                )
            }
        }
    }
}

private fun LazyGridScope.appendFooter(append: AppendState, onRetry: () -> Unit) {
    when (append) {
        AppendState.Idle, AppendState.End -> Unit
        AppendState.Loading -> item(key = "append", span = { GridItemSpan(maxLineSpan) }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        AppendState.Error -> item(key = "append", span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.book_error_load_more),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = onRetry) {
                    Text(stringResource(R.string.book_retry))
                }
            }
        }
    }
}

@Composable
private fun SearchScreenPreview(uiState: SearchUiState) {
    MaterialTheme {
        SearchScreen(
            uiState = uiState,
            onQueryChange = {},
            onSearch = {},
            onSortChange = {},
            onBookClick = {},
            onFavoriteClick = {},
            onLoadMore = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenSuccessPreview() {
    SearchScreenPreview(
        SearchUiState(
            query = "코틀린",
            result = UiState.Success(SearchResult(books = PreviewBooks.books)),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenAppendLoadingPreview() {
    SearchScreenPreview(
        SearchUiState(
            query = "코틀린",
            sort = SearchSort.LATEST,
            result = UiState.Success(
                SearchResult(
                    books = PreviewBooks.books.take(2),
                    append = AppendState.Loading,
                ),
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenAppendErrorPreview() {
    SearchScreenPreview(
        SearchUiState(
            query = "코틀린",
            result = UiState.Success(
                SearchResult(
                    books = PreviewBooks.books.take(2),
                    append = AppendState.Error,
                ),
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenIdlePreview() {
    SearchScreenPreview(SearchUiState())
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenLoadingPreview() {
    SearchScreenPreview(SearchUiState(query = "코틀린", result = UiState.Loading))
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenEmptyPreview() {
    SearchScreenPreview(
        SearchUiState(
            query = "없는 책 제목",
            result = UiState.Success(SearchResult(books = emptyList())),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenErrorPreview() {
    SearchScreenPreview(SearchUiState(query = "코틀린", result = UiState.Error))
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun SearchScreenTabletPreview() {
    SearchScreenPreview(
        SearchUiState(
            query = "코틀린",
            result = UiState.Success(SearchResult(books = PreviewBooks.books)),
        ),
    )
}
