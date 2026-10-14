package com.peter.booksearchapp.feature.book.presentation.paging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 목록을 페이지 단위로 불러와 쌓습니다. 검색 결과(페이지 번호)와 즐겨찾기(커서)처럼
 * 다음 페이지를 가리키는 키만 다른 목록에서 함께 씁니다.
 *
 * - 다음 페이지에 이미 있는 항목이 다시 오면 [itemKey] 기준으로 걸러냅니다.
 * - 처음부터 다시 불러오면 진행 중이던 요청(첫 페이지 · 다음 페이지)은 취소하고 그 결과는 버립니다.
 * - 모든 함수는 [scope]의 스레드(ViewModel이면 메인)에서 불러야 합니다.
 * - 로딩 상태는 코루틴을 띄우기 전에 바로 바꿉니다. 그래서 디스패처와 상관없이 연달아 불러도 요청이 한 번만 나갑니다.
 */
class Paginator<Key : Any, Item>(
    private val scope: CoroutineScope,
    private val pageSize: Int,
    private val itemKey: (Item) -> Any,
) {
    private val mutableState = MutableStateFlow(PagingState<Item>())
    val state: StateFlow<PagingState<Item>> = mutableState.asStateFlow()

    private var loader: PageLoader<Key, Item>? = null
    private var nextKey: Key? = null
    private var lastRefreshSize = pageSize
    private var refreshJob: Job? = null
    private var appendJob: Job? = null

    /**
     * 첫 페이지부터 다시 불러옵니다.
     *
     * @param loader 이번 목록을 불러오는 방법. 다음 페이지와 다시 시도에도 같은 방법을 씁니다.
     * @param loadSize 첫 요청에서 불러올 개수. 지금까지 불러온 만큼 한 번에 다시 불러올 때 씁니다.
     * @param keepItems true면 불러오는 동안 이미 불러온 목록을 그대로 보여주고, false면 목록을 비우고 로딩 상태로 바꿉니다.
     */
    fun refresh(loader: PageLoader<Key, Item>, loadSize: Int = pageSize, keepItems: Boolean = false) {
        refreshJob?.cancel()
        appendJob?.cancel()
        this.loader = loader
        lastRefreshSize = loadSize

        mutableState.update { current ->
            if (keepItems && current.refreshState == RefreshState.Loaded) current else PagingState(refreshState = RefreshState.Loading)
        }
        refreshJob = scope.launch {
            val result = loader.load(key = null, size = loadSize)
            ensureActive()
            mutableState.value = when (result) {
                is PageResult.Success -> {
                    nextKey = result.nextKey
                    PagingState(
                        items = result.items.distinctBy(itemKey),
                        refreshState = RefreshState.Loaded,
                        appendState = if (result.nextKey == null) AppendState.End else AppendState.Idle,
                    )
                }
                PageResult.Error -> PagingState(refreshState = RefreshState.Error)
            }
        }
    }

    /** 다음 페이지를 불러와 뒤에 붙입니다. 첫 페이지를 불러오는 중이거나, 다음 페이지를 불러오는 중이거나, 마지막 페이지면 무시합니다. */
    fun loadMore() {
        val loader = loader ?: return
        val key = nextKey ?: return
        val current = mutableState.value
        if (refreshJob?.isActive == true || appendJob?.isActive == true) return
        if (current.refreshState != RefreshState.Loaded || current.appendState.canLoadMore.not()) return

        mutableState.update { it.copy(appendState = AppendState.Loading) }
        appendJob = scope.launch {
            val result = loader.load(key = key, size = pageSize)
            ensureActive()
            when (result) {
                is PageResult.Success -> {
                    nextKey = result.nextKey
                    mutableState.update {
                        it.copy(
                            items = (it.items + result.items).distinctBy(itemKey),
                            appendState = if (result.nextKey == null) AppendState.End else AppendState.Idle,
                        )
                    }
                }
                PageResult.Error -> mutableState.update { it.copy(appendState = AppendState.Error) }
            }
        }
    }

    /** 실패한 요청을 다시 합니다. 첫 페이지가 실패했으면 처음부터, 다음 페이지가 실패했으면 그 페이지를 다시 불러옵니다. */
    fun retry() {
        val current = mutableState.value
        when {
            current.refreshState == RefreshState.Error -> loader?.let { refresh(it, loadSize = lastRefreshSize) }
            current.appendState == AppendState.Error -> loadMore()
        }
    }
}
