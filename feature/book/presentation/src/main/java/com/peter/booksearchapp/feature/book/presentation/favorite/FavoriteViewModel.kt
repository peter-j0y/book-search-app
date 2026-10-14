package com.peter.booksearchapp.feature.book.presentation.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peter.booksearchapp.feature.book.data.local.model.FavoriteBookCursor
import com.peter.booksearchapp.feature.book.data.repository.BookRepository
import com.peter.booksearchapp.feature.book.domain.model.Book
import com.peter.booksearchapp.feature.book.domain.usecase.GetFavoriteBooksUseCase
import com.peter.booksearchapp.feature.book.domain.usecase.SetFavoriteUseCase
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.model.FavoriteSort
import com.peter.booksearchapp.feature.book.presentation.model.PriceFilter
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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val getFavoriteBooks: GetFavoriteBooksUseCase,
    private val setFavorite: SetFavoriteUseCase,
    bookRepository: BookRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(FavoriteSort.TITLE_ASC)
    private val priceFilter = MutableStateFlow(PriceFilter.ALL)
    private val paginator = Paginator<FavoriteBookCursor, Book>(viewModelScope, GetFavoriteBooksUseCase.PAGE_SIZE, itemKey = Book::isbn)
    private var loadedCondition: FavoriteCondition? = null

    /**
     * 즐겨찾기 목록을 다시 불러오는 흐름입니다. 아래 두 경우에 다시 불러옵니다.
     * - 검색어 · 정렬 · 금액 필터를 바꿨을 때
     * - 어느 화면에서든 즐겨찾기를 추가하거나 해제했을 때 (즐겨찾기 테이블이 바뀌면 알림이 옵니다)
     *
     * 즐겨찾기 화면이 보이는 동안만 동작하도록 [uiState]에 묶었습니다.
     * 다른 탭에 있다가 돌아오면 바로 한 번 다시 불러와서 그 사이 바뀐 내용을 반영합니다.
     */
    private val reloadOnChange: Flow<Unit> = combine(
        query.debounce { if (it.isBlank()) 0L else QUERY_DEBOUNCE_MILLIS },
        sort,
        priceFilter,
    ) { query, sort, priceFilter -> FavoriteCondition(query.trim(), sort, priceFilter) }
        .distinctUntilChanged()
        .flatMapLatest { condition -> bookRepository.observeFavoriteTableChanges().map { condition } }
        .onEach { reload(it) }
        .map { Unit }
        .onStart { emit(Unit) }

    val uiState: StateFlow<FavoriteUiState> = combine(
        query,
        sort,
        priceFilter,
        paginator.state,
        reloadOnChange,
    ) { query, sort, priceFilter, favorites, _ ->
        FavoriteUiState(
            query = query,
            sort = sort,
            priceFilter = priceFilter,
            result = when (favorites.refreshState) {
                RefreshState.Loaded -> UiState.Success(favorites.items.map { it.toUiModel(isFavorite = true) })
                RefreshState.NotLoaded, RefreshState.Loading -> UiState.Loading
                RefreshState.Error -> UiState.Error
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoriteUiState())

    fun onQueryChange(query: String) {
        this.query.value = query
    }

    fun onSortChange(sort: FavoriteSort) {
        this.sort.value = sort
    }

    fun onPriceFilterChange(priceFilter: PriceFilter) {
        this.priceFilter.value = priceFilter
    }

    fun onLoadMore() {
        paginator.loadMore()
    }

    fun onFavoriteClick(book: BookUiModel) {
        val target = findBook(book.id) ?: return
        viewModelScope.launch { setFavorite(target, isFavorite = !book.isFavorite) }
    }

    fun findBook(id: String): Book? = paginator.state.value.items.firstOrNull { it.isbn == id }

    /**
     * 즐겨찾기 목록을 첫 페이지부터 다시 불러옵니다.
     * - 조건이 바뀌었을 때: 첫 페이지만 불러옵니다.
     * - 조건은 그대로이고 즐겨찾기만 바뀌었을 때: 지금까지 불러온 개수만큼 한 번에 다시 불러옵니다.
     *   첫 페이지만 불러오면 스크롤해서 보던 아래쪽 목록이 사라지기 때문입니다.
     *
     * 불러오는 동안에도 기존 목록을 그대로 보여주고, 다 불러오면 새 목록으로 통째로 바꿉니다.
     * 진행 중이던 다음 페이지 불러오기는 취소합니다([Paginator.refresh] 참고).
     */
    private fun reload(condition: FavoriteCondition) {
        val loadSize = if (condition == loadedCondition) {
            maxOf(paginator.state.value.items.size, GetFavoriteBooksUseCase.PAGE_SIZE)
        } else {
            GetFavoriteBooksUseCase.PAGE_SIZE
        }
        loadedCondition = condition
        paginator.refresh(loader = favoriteLoader(condition), loadSize = loadSize, keepItems = true)
    }

    /** [condition]에 맞는 즐겨찾기를 커서 단위로 불러옵니다. 첫 페이지는 커서 없이, 다음 페이지는 직전 페이지의 마지막 책 다음부터 불러옵니다. */
    private fun favoriteLoader(condition: FavoriteCondition) = PageLoader<FavoriteBookCursor, Book> { cursor, size ->
        val page = getFavoriteBooks(
            sort = condition.sort.favoriteBookSort,
            after = cursor,
            query = condition.query,
            minPrice = condition.priceFilter.minPrice,
            maxPrice = condition.priceFilter.maxPrice,
            size = size,
        )
        PageResult.Success(items = page.books, nextKey = page.nextCursor)
    }

    private data class FavoriteCondition(
        val query: String,
        val sort: FavoriteSort,
        val priceFilter: PriceFilter,
    )

    private companion object {
        const val QUERY_DEBOUNCE_MILLIS = 300L
    }
}
