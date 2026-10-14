package com.peter.booksearchapp.feature.book.presentation.search

import com.peter.booksearchapp.core.database.FakeFavoriteBookDao
import com.peter.booksearchapp.core.database.favoriteBookEntity
import com.peter.booksearchapp.feature.book.data.FakeBookApi
import com.peter.booksearchapp.feature.book.data.bookDocument
import com.peter.booksearchapp.feature.book.data.networkErrorResponse
import com.peter.booksearchapp.feature.book.data.searchResponse
import com.peter.booksearchapp.feature.book.data.testBookRepository
import com.peter.booksearchapp.feature.book.domain.usecase.ObserveFavoriteIsbnsUseCase
import com.peter.booksearchapp.feature.book.domain.usecase.SearchBooksUseCase
import com.peter.booksearchapp.feature.book.domain.usecase.SetFavoriteUseCase
import com.peter.booksearchapp.feature.book.presentation.MainDispatcherRule
import com.peter.booksearchapp.feature.book.presentation.isbn
import com.peter.booksearchapp.feature.book.presentation.model.SearchSort
import com.peter.booksearchapp.feature.book.presentation.model.UiState
import com.peter.booksearchapp.feature.book.presentation.paging.AppendState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeBookApi()
    private val dao = FakeFavoriteBookDao()
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        val repository = testBookRepository(api, dao)
        viewModel = SearchViewModel(
            searchBooks = SearchBooksUseCase(repository),
            observeFavoriteIsbns = ObserveFavoriteIsbnsUseCase(repository),
            setFavorite = SetFavoriteUseCase(repository),
        )
    }

    @Test
    fun `검색어 입력이 멈춘 뒤에 검색한다`() = runTest {
        collectUiState()
        api.respond = { searchResponse(listOf(bookDocument(isbn(1))), isEnd = true) }

        viewModel.onQueryChange("코틀린")
        advanceTimeBy(499)
        runCurrent()
        assertTrue(api.requests.isEmpty())

        advanceUntilIdle()
        assertEquals(listOf("코틀린"), api.requests.map { it.query })
        assertEquals(listOf(isbn(1)), bookIds())
    }

    @Test
    fun `같은 조건으로는 다시 요청하지 않는다`() = runTest {
        collectUiState()
        searchFor("코틀린")

        viewModel.onSearch()
        advanceUntilIdle()

        assertEquals(1, api.requests.size)
    }

    @Test
    fun `검색에 실패하면 다시 시도로 같은 조건을 다시 요청한다`() = runTest {
        collectUiState()
        api.respond = { networkErrorResponse() }
        searchFor("코틀린")
        assertEquals(UiState.Error, viewModel.uiState.value.result)

        api.respond = { searchResponse(listOf(bookDocument(isbn(1))), isEnd = true) }
        viewModel.onRetry()
        advanceUntilIdle()

        assertEquals(2, api.requests.size)
        assertEquals(listOf(isbn(1)), bookIds())
    }

    @Test
    fun `검색이 실패해도 입력 debounce로는 같은 조건을 다시 요청하지 않는다`() = runTest {
        collectUiState()
        api.respond = { networkErrorResponse() }

        viewModel.onKeywordClick("코틀린")
        advanceUntilIdle()

        assertEquals(1, api.requests.size)
        assertEquals(UiState.Error, viewModel.uiState.value.result)
    }

    @Test
    fun `새 검색을 시작하면 진행 중이던 검색 결과는 버린다`() = runTest {
        collectUiState()
        api.respond = { request ->
            if (request.query == "느린 검색") delay(1_000)
            searchResponse(listOf(bookDocument(isbn(if (request.query == "느린 검색") 1 else 2))), isEnd = true)
        }

        viewModel.onKeywordClick("느린 검색")
        runCurrent()
        viewModel.onKeywordClick("빠른 검색")
        advanceUntilIdle()

        assertEquals(listOf(isbn(2)), bookIds())
    }

    @Test
    fun `다음 페이지를 붙이고 이미 있는 ISBN은 걸러낸다`() = runTest {
        collectUiState()
        api.respond = { request ->
            when (request.page) {
                1 -> searchResponse((1..3).map { bookDocument(isbn(it)) }, isEnd = false)
                else -> searchResponse(listOf(bookDocument(isbn(3)), bookDocument(isbn(4))), isEnd = true)
            }
        }
        searchFor("코틀린")

        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals((1..4).map(::isbn), bookIds())
        assertEquals(AppendState.End, successResult().append)
    }

    @Test
    fun `마지막 페이지까지 불러왔으면 다음 페이지를 요청하지 않는다`() = runTest {
        collectUiState()
        api.respond = { searchResponse(listOf(bookDocument(isbn(1))), isEnd = true) }
        searchFor("코틀린")

        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(1, api.requests.size)
    }

    @Test
    fun `다음 페이지 요청이 실패해도 불러온 목록은 유지하고 다시 시도할 수 있다`() = runTest {
        collectUiState()
        api.respond = { request ->
            if (request.page == 1) searchResponse(listOf(bookDocument(isbn(1))), isEnd = false) else networkErrorResponse()
        }
        searchFor("코틀린")

        viewModel.onLoadMore()
        advanceUntilIdle()
        assertEquals(listOf(isbn(1)), bookIds())
        assertEquals(AppendState.Error, successResult().append)

        api.respond = { searchResponse(listOf(bookDocument(isbn(2))), isEnd = true) }
        viewModel.onRetry()
        advanceUntilIdle()

        assertEquals(listOf(2, 2), api.requests.drop(1).map { it.page })
        assertEquals(listOf(isbn(1), isbn(2)), bookIds())
    }

    @Test
    fun `검색어를 지워도 이전 검색 결과를 유지한다`() = runTest {
        collectUiState()
        api.respond = { searchResponse(listOf(bookDocument(isbn(1))), isEnd = true) }
        searchFor("코틀린")

        viewModel.onQueryChange("")
        advanceUntilIdle()

        assertEquals(1, api.requests.size)
        assertEquals(listOf(isbn(1)), bookIds())
    }

    @Test
    fun `검색어를 지운 상태에서 정렬을 바꾸면 이전 검색어로 다시 검색한다`() = runTest {
        collectUiState()
        searchFor("코틀린")
        viewModel.onQueryChange("")
        advanceUntilIdle()

        viewModel.onSortChange(SearchSort.LATEST)
        advanceUntilIdle()

        val lastRequest = api.requests.last()
        assertEquals("코틀린", lastRequest.query)
        assertEquals("latest", lastRequest.sort)
    }

    @Test
    fun `다른 화면에서 바꾼 즐겨찾기가 검색 결과에 반영된다`() = runTest {
        collectUiState()
        api.respond = { searchResponse(listOf(bookDocument(isbn(1)), bookDocument(isbn(2))), isEnd = true) }
        searchFor("코틀린")

        dao.insert(favoriteBookEntity(isbn(2)))
        advanceUntilIdle()

        assertEquals(listOf(false, true), successResult().books.map { it.isFavorite })
    }

    @Test
    fun `하트를 누르면 즐겨찾기에 추가한다`() = runTest {
        collectUiState()
        api.respond = { searchResponse(listOf(bookDocument(isbn(1))), isEnd = true) }
        searchFor("코틀린")

        viewModel.onFavoriteClick(successResult().books.single())
        advanceUntilIdle()

        assertEquals(listOf(isbn(1)), dao.favoriteBooks.map { it.isbn })
        assertTrue(successResult().books.single().isFavorite)
    }

    /** uiState는 구독하는 동안만 동작하므로 화면처럼 구독해 둡니다. */
    private fun TestScope.collectUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    private fun TestScope.searchFor(query: String) {
        viewModel.onQueryChange(query)
        viewModel.onSearch()
        advanceUntilIdle()
    }

    private fun successResult(): SearchResult = (viewModel.uiState.value.result as UiState.Success).data

    private fun bookIds(): List<String> = successResult().books.map { it.id }
}
