package com.peter.booksearchapp.feature.book.presentation.paging

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaginatorTest {
    @Test
    fun `첫 페이지를 불러오고 다음 페이지를 붙이며 이미 있는 항목은 걸러낸다`() = runTest {
        val loader = FakeLoader(pages = mapOf(null to page(1, 2, 3, next = 2), 2 to page(3, 4)))
        val paginator = paginator()

        paginator.refresh(loader)
        advanceUntilIdle()
        assertEquals(PagingState(items = listOf(1, 2, 3), refreshState = RefreshState.Loaded, appendState = AppendState.Idle), paginator.state.value)

        paginator.loadMore()
        advanceUntilIdle()
        assertEquals(listOf(1, 2, 3, 4), paginator.state.value.items)
        assertEquals(AppendState.End, paginator.state.value.appendState)
    }

    @Test
    fun `불러오는 중이거나 마지막 페이지면 다음 페이지를 요청하지 않는다`() = runTest {
        val loader = FakeLoader(pages = mapOf(null to page(1, next = 2), 2 to page(2)))
        val paginator = paginator()
        paginator.refresh(loader)
        advanceUntilIdle()

        paginator.loadMore()
        paginator.loadMore()
        advanceUntilIdle()
        paginator.loadMore()
        advanceUntilIdle()

        assertEquals(listOf(null, 2), loader.requestedKeys)
    }

    @Test
    fun `첫 페이지가 실패하면 다시 시도로 같은 개수를 처음부터 다시 요청한다`() = runTest {
        val loader = FakeLoader(pages = mapOf(null to page(1)), failingKeys = mutableSetOf(null))
        val paginator = paginator()
        paginator.refresh(loader, loadSize = 40)
        advanceUntilIdle()
        assertEquals(RefreshState.Error, paginator.state.value.refreshState)

        loader.failingKeys.clear()
        paginator.retry()
        advanceUntilIdle()

        assertEquals(listOf(40, 40), loader.requestedSizes)
        assertEquals(listOf(1), paginator.state.value.items)
    }

    @Test
    fun `다음 페이지가 실패해도 불러온 목록은 유지하고 다시 시도로 그 페이지를 다시 요청한다`() = runTest {
        val loader = FakeLoader(pages = mapOf(null to page(1, next = 2), 2 to page(2)), failingKeys = mutableSetOf(2))
        val paginator = paginator()
        paginator.refresh(loader)
        advanceUntilIdle()

        paginator.loadMore()
        advanceUntilIdle()
        assertEquals(listOf(1), paginator.state.value.items)
        assertEquals(AppendState.Error, paginator.state.value.appendState)

        loader.failingKeys.clear()
        paginator.retry()
        advanceUntilIdle()

        assertEquals(listOf(null, 2, 2), loader.requestedKeys)
        assertEquals(listOf(1, 2), paginator.state.value.items)
    }

    @Test
    fun `처음부터 다시 불러오면 진행 중이던 다음 페이지 결과는 버린다`() = runTest {
        val slowSecondPage = CompletableDeferred<PageResult<Int, Int>>()
        val firstLoader = FakeLoader(pages = mapOf(null to page(1, next = 2)), waits = mapOf(2 to slowSecondPage))
        val paginator = paginator()
        paginator.refresh(firstLoader)
        advanceUntilIdle()
        paginator.loadMore()
        runCurrent()

        paginator.refresh(FakeLoader(pages = mapOf(null to page(10))))
        slowSecondPage.complete(page(2))
        advanceUntilIdle()

        assertEquals(listOf(10), paginator.state.value.items)
    }

    @Test
    fun `keepItems면 다시 불러오는 동안 기존 목록을 보여주고, 아니면 목록을 비우고 로딩 상태로 바꾼다`() = runTest {
        val waiting = CompletableDeferred<PageResult<Int, Int>>()
        val paginator = paginator()
        paginator.refresh(FakeLoader(pages = mapOf(null to page(1))))
        advanceUntilIdle()

        paginator.refresh(FakeLoader(waits = mapOf(null to waiting)), keepItems = true)
        runCurrent()
        assertEquals(PagingState(items = listOf(1), refreshState = RefreshState.Loaded, appendState = AppendState.End), paginator.state.value)

        paginator.refresh(FakeLoader(waits = mapOf(null to waiting)), keepItems = false)
        runCurrent()
        assertEquals(PagingState<Int>(refreshState = RefreshState.Loading), paginator.state.value)
    }

    /**
     * 테스트와 같은 가상 시계로 도는 범위에서 [Paginator]를 만듭니다. advanceUntilIdle은 backgroundScope의 작업을 기다리지 않아서 따로 만듭니다.
     * 테스트에 속하지 않은 범위라, 끝까지 기다리는 요청이 남아 있어도 테스트가 끝납니다.
     */
    private fun TestScope.paginator() = Paginator<Int, Int>(
        scope = CoroutineScope(StandardTestDispatcher(testScheduler) + Job()),
        pageSize = PAGE_SIZE,
        itemKey = { it },
    )

    private fun page(vararg items: Int, next: Int? = null): PageResult<Int, Int> = PageResult.Success(items.toList(), next)

    /** 키별로 정해 둔 페이지를 돌려주고, 받은 요청(키 · 개수)을 기록합니다. [waits]에 있는 키는 결과가 올 때까지 기다립니다. */
    private class FakeLoader(
        private val pages: Map<Int?, PageResult<Int, Int>> = emptyMap(),
        val failingKeys: MutableSet<Int?> = mutableSetOf(),
        private val waits: Map<Int?, CompletableDeferred<PageResult<Int, Int>>> = emptyMap(),
    ) : PageLoader<Int, Int> {
        val requestedKeys = mutableListOf<Int?>()
        val requestedSizes = mutableListOf<Int>()

        override suspend fun load(key: Int?, size: Int): PageResult<Int, Int> {
            requestedKeys += key
            requestedSizes += size
            waits[key]?.let { return it.await() }
            return if (key in failingKeys) PageResult.Error else pages.getValue(key)
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
