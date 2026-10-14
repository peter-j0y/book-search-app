package com.peter.booksearchapp.feature.book.presentation.component

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

/**
 * 스크롤할 때마다 목록 끝 근처인지 확인하고, 끝 근처에 새로 도착했을 때만 [onLoadMore]를 한 번 부릅니다.
 */
@Composable
internal fun LazyGridState.LoadMoreEffect(
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    bufferRows: Int = DefaultBufferRows,
) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    LaunchedEffect(this, canLoadMore, bufferRows) {
        if (canLoadMore.not()) return@LaunchedEffect
        snapshotFlow { itemCountIfNearEnd(bufferRows) }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { currentOnLoadMore() }
    }
}

/**
 * 지금 목록 끝 근처인지 확인합니다. 끝 근처면 전체 항목 수를, 아니면 null을 돌려줍니다.
 *
 * "화면에 보이는 마지막 항목 번호가 (전체 개수 − [bufferRows]줄 분량) 이상이면 끝 근처"로 봅니다.
 * 한 줄에 놓이는 개수를 곱하므로, 열이 많은 넓은 화면에서도 휴대폰과 같은 거리(줄 수)에서 미리 불러옵니다.
 * - 휴대폰(한 줄에 1권), 전체 20권: 20 − 5×1 = 15번 이상이 보이면 끝 근처
 * - 태블릿(한 줄에 3권), 전체 20권: 20 − 5×3 = 5번 이상이 보이면 끝 근처
 *
 * 아직 아무 항목도 그려지지 않았으면 판단하지 않고 null을 돌려줍니다.
 */
private fun LazyGridState.itemCountIfNearEnd(bufferRows: Int): Int? {
    val totalItems = layoutInfo.totalItemsCount
    val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return null
    val itemsPerRow = layoutInfo.maxSpan.coerceAtLeast(1)
    return totalItems.takeIf { lastVisibleIndex >= totalItems - bufferRows * itemsPerRow }
}

/** 끝에서 몇 줄 전부터 미리 불러올지. 휴대폰(한 줄에 1권)에서 끝에서 5권 전에 불러오도록 맞춘 값입니다. */
private const val DefaultBufferRows = 5
