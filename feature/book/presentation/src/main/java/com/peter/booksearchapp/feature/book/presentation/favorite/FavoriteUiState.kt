package com.peter.booksearchapp.feature.book.presentation.favorite

import androidx.compose.runtime.Immutable
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.model.FavoriteSort
import com.peter.booksearchapp.feature.book.presentation.model.PriceFilter

/** 즐겨찾기 탭 화면 상태. 검색·정렬·필터는 로컬에 저장된 즐겨찾기 안에서 적용한다. */
@Immutable
data class FavoriteUiState(
    val query: String = "",
    val sort: FavoriteSort = FavoriteSort.TITLE_ASC,
    val priceFilter: PriceFilter = PriceFilter.ALL,
    val result: FavoriteResult = FavoriteResult.Loading,
)

@Immutable
sealed interface FavoriteResult {
    /** 로컬 저장소에서 읽는 중. */
    data object Loading : FavoriteResult

    /** 즐겨찾기한 도서가 하나도 없음. */
    data object Empty : FavoriteResult

    /** 즐겨찾기는 있지만 검색어·필터 조건에 맞는 도서가 없음. */
    data object NoMatch : FavoriteResult

    data class Success(val books: List<BookUiModel>) : FavoriteResult
}
