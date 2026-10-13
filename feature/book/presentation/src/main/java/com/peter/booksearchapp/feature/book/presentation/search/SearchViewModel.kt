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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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
    private val searchedBooks = MutableStateFlow<UiState<SearchedBooks>>(UiState.Idle)
    private var lastRequest: SearchRequest? = null
    private var searchJob: Job? = null

    /**
     * 화면에 있는 책들의 즐겨찾기 여부만 구독해 다른 화면에서 바꾼 즐겨찾기도 바로 반영합니다.
     * 다음 페이지 로딩 상태만 바뀔 때는 다시 구독하지 않도록, 책 목록(ISBN)이 바뀔 때만 구독을 새로 맺습니다.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val favoriteIsbns: Flow<FavoriteIsbns> = searchedBooks
        .map { state -> (state as? UiState.Success)?.data?.books?.map { it.isbn }.orEmpty() }
        .distinctUntilChanged()
        .flatMapLatest { isbns -> observeFavoriteIsbns(isbns).map { FavoriteIsbns(isbns = isbns, favorites = it) } }

    private val searchResult: Flow<UiState<SearchResult>> = combineTransform(searchedBooks, favoriteIsbns) { state, favoriteIsbns ->
        when (state) {
            is UiState.Success -> {
                if (favoriteIsbns.isbns != state.data.books.map { it.isbn }) return@combineTransform
                emit(
                    UiState.Success(
                        SearchResult(
                            books = state.data.books.map { it.toUiModel(isFavorite = it.isbn in favoriteIsbns.favorites) },
                            append = state.data.append,
                        ),
                    ),
                )
            }
            UiState.Idle -> emit(UiState.Idle)
            UiState.Loading -> emit(UiState.Loading)
            UiState.Error -> emit(UiState.Error)
        }
    }

    /**
     * 입력이 멈추면 검색합니다. 글자를 칠 때마다 요청하지 않도록 잠시 기다립니다.
     * [uiState]에 함께 묶어 화면이 구독하는 동안만 동작하게 합니다.
     */
    private val searchOnQueryChange: Flow<Unit> = query
        .debounce(SEARCH_DEBOUNCE_MILLIS.milliseconds)
        .onEach { search(SearchRequest(it.trim(), sort.value)) }
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

    /**
     * 현재 검색 결과의 다음 페이지를 불러와 목록 뒤에 붙입니다.
     * 검색 결과가 없거나, 이미 다음 페이지를 불러오는 중이거나, 마지막 페이지까지 불러왔다면 아무것도 하지 않습니다.
     * 직전 다음 페이지 요청이 실패한 상태라면 같은 페이지를 다시 요청합니다.
     * 카카오 API는 같은 책을 판매처별로 따로 내려주기도 해서(ISBN은 같고 출판사 · 가격만 다름), 앞 페이지에 있던 책이
     * 다음 페이지에 다시 올 수 있습니다. 목록이 ISBN을 키로 쓰기 때문에 ISBN 기준으로 중복을 걸러냅니다.
     */
    fun onLoadMore() {
        val current = (searchedBooks.value as? UiState.Success)?.data ?: return
        if (current.append.canLoadMore.not()) return

        searchJob = viewModelScope.launch {
            searchedBooks.value = UiState.Success(current.copy(append = AppendState.Loading))
            val nextPage = current.page + 1
            searchedBooks.value = when (val response = searchBooks(current.request.query, current.request.sort.bookSearchSort, nextPage)) {
                is ApiResponse.Success -> UiState.Success(
                    current.copy(
                        books = (current.books + response.data.books).distinctBy { it.isbn },
                        page = nextPage,
                        append = if (response.data.isEnd) AppendState.End else AppendState.Idle,
                    ),
                )
                is ApiResponse.Failure -> UiState.Success(current.copy(append = AppendState.Error))
            }
        }
    }

    fun onRetry() {
        when (val current = searchedBooks.value) {
            UiState.Error -> lastRequest?.let(::search)
            is UiState.Success -> if (current.data.append == AppendState.Error) onLoadMore()
            UiState.Idle, UiState.Loading -> Unit
        }
    }

    fun onFavoriteClick(book: BookUiModel) {
        val target = findBook(book.id) ?: return
        viewModelScope.launch { setFavorite(target, isFavorite = !book.isFavorite) }
    }

    fun findBook(id: String): Book? =
        (searchedBooks.value as? UiState.Success)?.data?.books?.firstOrNull { it.isbn == id }

    /**
     * 같은 조건으로 이미 요청했다면 실패한 경우가 아니면 다시 요청하지 않습니다.
     * 새 검색을 시작하면 진행 중인 요청은 취소합니다.
     * 검색어를 지워도 이전 결과는 그대로 두고, 새 검색어를 입력하면 그때 결과를 바꿉니다.
     */
    private fun search(request: SearchRequest) {
        if (request.query.isEmpty()) return
        if (request == lastRequest && searchedBooks.value != UiState.Error) return
        lastRequest = request
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            searchedBooks.value = UiState.Loading
            searchedBooks.value = when (val response = searchBooks(request.query, request.sort.bookSearchSort, FIRST_PAGE)) {
                is ApiResponse.Success -> UiState.Success(
                    SearchedBooks(
                        request = request,
                        books = response.data.books.distinctBy { it.isbn },
                        page = FIRST_PAGE,
                        append = if (response.data.isEnd) AppendState.End else AppendState.Idle,
                    ),
                )
                is ApiResponse.Failure -> UiState.Error
            }
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

    private data class SearchedBooks(
        val request: SearchRequest,
        val books: List<Book>,
        val page: Int,
        val append: AppendState,
    )

    private companion object {
        const val FIRST_PAGE = 1
        const val SEARCH_DEBOUNCE_MILLIS = 500L
    }
}
