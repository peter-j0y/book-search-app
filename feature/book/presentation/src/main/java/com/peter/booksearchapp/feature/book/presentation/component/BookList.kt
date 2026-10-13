package com.peter.booksearchapp.feature.book.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.preview.PreviewBooks
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * 도서 카드 목록. 화면 너비에 따라 열 개수가 바뀝니다.
 *
 * @param contentPadding 시스템 바 등을 피하기 위한 여백. 목록의 기본 여백에 더해집니다.
 */
@Composable
internal fun BookList(
    books: List<BookUiModel>,
    onBookClick: (BookUiModel) -> Unit,
    onFavoriteClick: (BookUiModel) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    state: LazyGridState = rememberLazyGridState(),
    onLoadMore: (() -> Unit)? = null,
    footer: (LazyGridScope.() -> Unit)? = null,
) {
    if (onLoadMore != null) {
        val currentOnLoadMore by rememberUpdatedState(onLoadMore)
        LaunchedEffect(state) {
            snapshotFlow {
                val layoutInfo = state.layoutInfo
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                layoutInfo.totalItemsCount > 0 &&
                    lastVisibleIndex >= layoutInfo.totalItemsCount - LoadMoreThreshold
            }
                .distinctUntilChanged()
                .filter { it }
                .collect { currentOnLoadMore() }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = BookCardMinWidth),
        modifier = modifier.fillMaxSize(),
        state = state,
        contentPadding = contentPadding.plus(ListPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = books, key = { it.id }) { book ->
            BookCard(
                book = book,
                onClick = { onBookClick(book) },
                onFavoriteClick = { onFavoriteClick(book) },
                modifier = Modifier.animateItem(),
            )
        }
        footer?.invoke(this)
    }
}

private val BookCardMinWidth = 340.dp
private val ListPadding = 12.dp

@Composable
private fun PaddingValues.plus(padding: Dp): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + padding,
        top = calculateTopPadding() + padding,
        end = calculateEndPadding(layoutDirection) + padding,
        bottom = calculateBottomPadding() + padding,
    )
}
private const val LoadMoreThreshold = 5

@Preview(showBackground = true, backgroundColor = 0xFFE6E6E6)
@Composable
private fun BookListPreview() {
    MaterialTheme {
        BookList(books = PreviewBooks.books, onBookClick = {}, onFavoriteClick = {})
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFE6E6E6,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun BookListTabletPreview() {
    MaterialTheme {
        BookList(books = PreviewBooks.books, onBookClick = {}, onFavoriteClick = {})
    }
}
