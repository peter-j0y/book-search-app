package com.peter.booksearchapp.feature.book.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peter.booksearchapp.core.network.ApiResponse
import com.peter.booksearchapp.feature.book.domain.model.Book
import com.peter.booksearchapp.feature.book.domain.usecase.ObserveFavoriteIsbnsUseCase
import com.peter.booksearchapp.feature.book.domain.usecase.SearchBooksUseCase
import com.peter.booksearchapp.feature.book.domain.usecase.SetFavoriteUseCase
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.model.SearchSort
import com.peter.booksearchapp.feature.book.presentation.model.UiState
import com.peter.booksearchapp.feature.book.presentation.model.toUiModel
import com.peter.booksearchapp.feature.book.presentation.paging.PageLoader
import com.peter.booksearchapp.feature.book.presentation.paging.PageResult
import com.peter.booksearchapp.feature.book.presentation.paging.Paginator
import com.peter.booksearchapp.feature.book.presentation.paging.RefreshState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchBooks: SearchBooksUseCase,
    private val observeFavoriteIsbns: ObserveFavoriteIsbnsUseCase,
    private val setFavorite: SetFavoriteUseCase,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(SearchSort.ACCURACY)
    private val paginator = Paginator<Int, Book>(viewModelScope, SearchBooksUseCase.PAGE_SIZE, itemKey = Book::isbn)
    private var lastRequest: SearchRequest? = null

    /**
     * 화면에 있는 책들의 즐겨찾기 여부만 구독해 다른 화면에서 바꾼 즐겨찾기도 바로 반영합니다.
     * 다음 페이지 로딩 상태만 바뀔 때는 다시 구독하지 않도록, 책 목록(ISBN)이 바뀔 때만 구독을 새로 맺습니다.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val favoriteIsbns: Flow<FavoriteIsbns> = paginator.state
        .map { state -> state.items.map { it.isbn } }
        .distinctUntilChanged()
        .flatMapLatest { isbns -> observeFavoriteIsbns(isbns).map { FavoriteIsbns(isbns = isbns, favorites = it) } }

    private val searchResult: Flow<UiState<SearchResult>> = combineTransform(paginator.state, favoriteIsbns) { state, favoriteIsbns ->
        when (state.refreshState) {
            RefreshState.Loaded -> {
                if (favoriteIsbns.isbns != state.items.map { it.isbn }) return@combineTransform
                emit(
                    UiState.Success(
                        SearchResult(
                            books = state.items.map { it.toUiModel(isFavorite = it.isbn in favoriteIsbns.favorites) },
                            append = state.appendState,
                        ),
                    ),
                )
            }
            RefreshState.NotLoaded -> emit(UiState.Idle)
            RefreshState.Loading -> emit(UiState.Loading)
            RefreshState.Error -> emit(UiState.Error)
        }
    }

    /**
     * 입력이 멈추면 검색합니다. 글자를 칠 때마다 요청하지 않도록 잠시 기다립니다.
     * [uiState]에 함께 묶어 화면이 구독하는 동안만 동작하게 합니다.
     */
    private val searchOnQueryChange: Flow<Unit> = query
        .debounce(SEARCH_DEBOUNCE_MILLIS.milliseconds)
        .onEach { search(SearchRequest(it.trim(), sort.value), retryIfFailed = false) }
        .map { Unit }
        .onStart { emit(Unit) }

    val uiState: StateFlow<SearchUiState> = combine(query, sort, searchResult, searchOnQueryChange) { query, sort, result, _ ->
        SearchUiState(query = query, sort = sort, result = result)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun onQueryChange(query: String) {
        this.query.value = query
    }

    fun onSearch() {
        search(SearchRequest(query.value.trim(), sort.value))
    }

    fun onKeywordClick(keyword: String) {
        query.value = keyword
        search(SearchRequest(keyword, sort.value))
    }

    /**
     * 정렬을 바꾸고 다시 검색합니다.
     * 검색어를 지운 상태라면 화면에 남아 있는 결과의 검색어로 다시 검색해, 정렬 표시와 결과가 어긋나지 않게 합니다.
     */
    fun onSortChange(sort: SearchSort) {
        this.sort.value = sort
        val query = query.value.trim().ifEmpty { lastRequest?.query.orEmpty() }
        search(SearchRequest(query, sort))
    }

    /** 현재 검색 결과의 다음 페이지를 불러와 목록 뒤에 붙입니다. 같은 책이 다시 오면 ISBN 기준으로 걸러냅니다([Paginator] 참고). */
    fun onLoadMore() {
        paginator.loadMore()
    }

    /** 실패한 요청을 다시 합니다. 첫 페이지가 실패했으면 같은 조건으로 처음부터, 다음 페이지가 실패했으면 그 페이지를 다시 요청합니다. */
    fun onRetry() {
        paginator.retry()
    }

    fun onFavoriteClick(book: BookUiModel) {
        val target = findBook(book.id) ?: return
        viewModelScope.launch { setFavorite(target, isFavorite = !book.isFavorite) }
    }

    fun findBook(id: String): Book? = paginator.state.value.items.firstOrNull { it.isbn == id }

    /**
     * 같은 조건으로 이미 요청했다면 다시 요청하지 않습니다.
     * 단, 직전 검색이 실패했고 [retryIfFailed]가 true면(키보드 검색 키 · 추천 검색어 · 다시 시도처럼 사용자가 직접 한 동작) 다시 요청합니다.
     * 입력 debounce로 자동 검색할 때는 false로 불러, 실패한 검색을 사용자 모르게 다시 요청하지 않게 합니다.
     * 새 검색을 시작하면 진행 중인 요청은 취소합니다.
     * 검색어를 지워도 이전 결과는 그대로 두고, 새 검색어를 입력하면 그때 결과를 바꿉니다.
     */
    private fun search(request: SearchRequest, retryIfFailed: Boolean = true) {
        if (request.query.isEmpty()) return
        val canRetry = retryIfFailed && paginator.state.value.refreshState == RefreshState.Error
        if (request == lastRequest && !canRetry) return
        lastRequest = request
        paginator.refresh(loader = searchLoader(request))
    }

    /**
     * [request] 조건으로 검색 결과를 페이지 번호 단위로 불러옵니다. 첫 페이지는 1이고, 마지막 페이지가 아니면 다음 번호를 다음 키로 넘깁니다.
     * 카카오 API는 같은 책을 판매처별로 따로 내려주기도 해서(ISBN은 같고 출판사 · 가격만 다름) 같은 책이 다시 올 수 있는데,
     * [Paginator]가 ISBN 기준으로 걸러냅니다.
     */
    private fun searchLoader(request: SearchRequest) = PageLoader<Int, Book> { page, size ->
        val currentPage = page ?: FIRST_PAGE
        when (val response = searchBooks(request.query, request.sort.bookSearchSort, currentPage, size)) {
            is ApiResponse.Success -> PageResult.Success(
                items = response.data.books,
                nextKey = if (response.data.isEnd) null else currentPage + 1,
            )
            is ApiResponse.Failure -> PageResult.Error
        }
    }

    private data class FavoriteIsbns(
        val isbns: List<String>,
        val favorites: Set<String>,
    )

    private data class SearchRequest(
        val query: String,
        val sort: SearchSort,
    )


    private companion object {
        const val FIRST_PAGE = 1
        const val SEARCH_DEBOUNCE_MILLIS = 500L
    }
}
