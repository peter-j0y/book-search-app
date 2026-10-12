package com.peter.booksearchapp.feature.book.presentation.search

import androidx.compose.runtime.Immutable
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.model.SearchSort

/** 검색 탭 화면 상태. ViewModel이 StateFlow 하나로 내보낸다. */
@Immutable
data class SearchUiState(
    val query: String = "",
    val sort: SearchSort = SearchSort.ACCURACY,
    val result: SearchResult = SearchResult.Idle,
)

/** 검색 결과 영역의 상태. */
@Immutable
sealed interface SearchResult {
    /** 아직 검색하지 않음. */
    data object Idle : SearchResult

    /** 첫 페이지를 불러오는 중. */
    data object Loading : SearchResult

    /** 검색했지만 결과가 없음. */
    data object Empty : SearchResult

    /** 첫 페이지를 불러오지 못함. */
    data object Error : SearchResult

    /**
     * 한 페이지 이상 불러옴.
     *
     * 다음 페이지 실패는 이미 보여준 결과를 지우지 않고 [append]로만 알린다.
     */
    data class Success(
        val books: List<BookUiModel>,
        val append: AppendState = AppendState.Idle,
    ) : SearchResult
}

/** 다음 페이지 불러오기 상태. */
enum class AppendState {
    /** 더 불러올 수 있음. */
    Idle,
    Loading,
    Error,

    /** 마지막 페이지까지 불러옴. */
    End,
}
