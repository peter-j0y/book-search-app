package com.peter.booksearchapp.feature.book.presentation.paging

/**
 * [Paginator]가 관리하는 목록 상태.
 *
 * @param items 지금까지 불러온 항목. 다음 페이지를 불러오면 뒤에 붙습니다.
 * @param refreshState 첫 페이지(처음부터 다시 불러오기) 상태
 * @param appendState 다음 페이지 상태
 */
data class PagingState<Item>(
    val items: List<Item> = emptyList(),
    val refreshState: RefreshState = RefreshState.NotLoaded,
    val appendState: AppendState = AppendState.Idle,
)

enum class RefreshState {
    NotLoaded,
    Loading,
    Loaded,
    Error,
}

enum class AppendState {
    Idle,
    Loading,
    Error,
    End;

    /** 다음 페이지를 요청할 수 있는 상태. 실패한 상태도 다시 시도할 수 있어서 포함합니다. */
    val canLoadMore: Boolean
        get() = this == Idle || this == Error
}

/** 한 페이지를 불러온 결과 */
sealed interface PageResult<out Key : Any, out Item> {
    /** @param nextKey 다음 페이지를 가리키는 키. 마지막 페이지면 null입니다. */
    data class Success<Key : Any, Item>(val items: List<Item>, val nextKey: Key?) : PageResult<Key, Item>

    data object Error : PageResult<Nothing, Nothing>
}

/**
 * 한 페이지를 불러오는 방법. 검색어 · 조건처럼 요청마다 달라지는 값은 이 함수가 갖고 있습니다.
 *
 * @param key 불러올 페이지의 키. 첫 페이지면 null입니다.
 * @param size 불러올 개수
 */
fun interface PageLoader<Key : Any, Item> {
    suspend fun load(key: Key?, size: Int): PageResult<Key, Item>
}
