package com.peter.booksearchapp.feature.book.presentation.favorite

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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

/**
 * 즐겨찾기 탭 화면. 상태를 받아 그리기만 하고, 사용자 동작은 콜백으로 올린다.
 *
 * 로컬 데이터 안에서 검색하므로 키보드의 검색 버튼 없이 입력할 때마다 결과가 바뀐다.
 */
@Composable
fun FavoriteScreen(
    uiState: FavoriteUiState,
    onQueryChange: (String) -> Unit,
    onSortChange: (FavoriteSort) -> Unit,
    onPriceFilterChange: (PriceFilter) -> Unit,
    onBookClick: (BookUiModel) -> Unit,
    onFavoriteClick: (BookUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 시트가 열려 있는지는 화면 안에서만 쓰는 상태라 ViewModel로 올리지 않는다.
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
    ) {
        when (val result = uiState.result) {
            FavoriteResult.Loading -> LoadingContent()
            FavoriteResult.Empty -> MessageContent(stringResource(R.string.book_favorite_empty))
            FavoriteResult.NoMatch -> MessageContent(stringResource(R.string.book_favorite_no_match))
            is FavoriteResult.Success -> BookList(
                books = result.books,
                onBookClick = onBookClick,
                onFavoriteClick = onFavoriteClick,
            )
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

/** "오름차순(제목)" 또는 필터가 걸려 있으면 "오름차순(제목) · 1만원 미만". */
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
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoriteScreenSuccessPreview() {
    FavoriteScreenPreview(
        FavoriteUiState(
            result = FavoriteResult.Success(PreviewBooks.books.map { it.copy(isFavorite = true) }),
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
            result = FavoriteResult.Success(listOf(PreviewBooks.book)),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun FavoriteScreenEmptyPreview() {
    FavoriteScreenPreview(FavoriteUiState(result = FavoriteResult.Empty))
}

@Preview(showBackground = true)
@Composable
private fun FavoriteScreenNoMatchPreview() {
    FavoriteScreenPreview(
        FavoriteUiState(
            query = "없는 책",
            priceFilter = PriceFilter.UNDER_10000,
            result = FavoriteResult.NoMatch,
        ),
    )
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun FavoriteScreenTabletPreview() {
    FavoriteScreenPreview(
        FavoriteUiState(
            result = FavoriteResult.Success(PreviewBooks.books.map { it.copy(isFavorite = true) }),
        ),
    )
}
