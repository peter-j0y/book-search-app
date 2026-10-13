package com.peter.booksearchapp.feature.book.presentation.favorite

import com.peter.booksearchapp.core.database.FakeFavoriteBookDao
import com.peter.booksearchapp.core.database.favoriteBookEntity
import com.peter.booksearchapp.feature.book.data.testBookRepository
import com.peter.booksearchapp.feature.book.domain.usecase.GetFavoriteBooksUseCase
import com.peter.booksearchapp.feature.book.domain.usecase.SetFavoriteUseCase
import com.peter.booksearchapp.feature.book.presentation.MainDispatcherRule
import com.peter.booksearchapp.feature.book.presentation.isbn
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.model.FavoriteSort
import com.peter.booksearchapp.feature.book.presentation.model.PriceFilter
import com.peter.booksearchapp.feature.book.presentation.model.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dao = FakeFavoriteBookDao()
    private lateinit var viewModel: FavoriteViewModel

    @Before
    fun setUp() {
        val repository = testBookRepository(dao = dao)
        viewModel = FavoriteViewModel(
            getFavoriteBooks = GetFavoriteBooksUseCase(repository),
            setFavorite = SetFavoriteUseCase(repository),
            bookRepository = repository,
        )
    }

    @Test
    fun `즐겨찾기를 제목 오름차순으로 불러온다`() = runTest {
        addFavorites("다", "가", "나")

        collectUiState()
        advanceUntilIdle()

        assertEquals(listOf("가", "나", "다"), titles())
    }

    @Test
    fun `정렬 · 금액 필터 · 검색어를 바꾸면 그 조건으로 다시 불러온다`() = runTest {
        dao.insert(favoriteBookEntity(isbn(1), title = "코틀린 입문", price = 8_000))
        dao.insert(favoriteBookEntity(isbn(2), title = "자바 입문", price = 15_000))
        dao.insert(favoriteBookEntity(isbn(3), title = "코틀린 심화", price = 25_000))
        collectUiState()
        advanceUntilIdle()

        viewModel.onSortChange(FavoriteSort.TITLE_DESC)
        advanceUntilIdle()
        assertEquals(listOf("코틀린 입문", "코틀린 심화", "자바 입문"), titles())

        viewModel.onPriceFilterChange(PriceFilter.FROM_10000_TO_20000)
        advanceUntilIdle()
        assertEquals(listOf("자바 입문"), titles())

        viewModel.onPriceFilterChange(PriceFilter.ALL)
        viewModel.onQueryChange("코틀린")
        advanceUntilIdle()
        assertEquals(listOf("코틀린 입문", "코틀린 심화"), titles())
    }

    @Test
    fun `다른 화면에서 즐겨찾기를 추가하면 목록에 나타난다`() = runTest {
        addFavorites("가")
        collectUiState()
        advanceUntilIdle()

        dao.insert(favoriteBookEntity(isbn(100), title = "나"))
        advanceUntilIdle()

        assertEquals(listOf("가", "나"), titles())
    }

    @Test
    fun `스크롤하면 다음 페이지를 불러온다`() = runTest {
        addFavorites(*pagedTitles(25))
        collectUiState()
        advanceUntilIdle()
        assertEquals(GetFavoriteBooksUseCase.PAGE_SIZE, titles().size)

        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(pagedTitles(25).toList(), titles())
    }

    @Test
    fun `즐겨찾기가 바뀌면 지금까지 불러온 개수만큼 다시 불러온다`() = runTest {
        addFavorites(*pagedTitles(25))
        collectUiState()
        advanceUntilIdle()
        viewModel.onLoadMore()
        advanceUntilIdle()

        dao.insert(favoriteBookEntity(isbn(100), title = "책 00"))
        advanceUntilIdle()

        assertEquals(listOf("책 00") + pagedTitles(24), titles())
    }

    @Test
    fun `하트를 끄면 목록에서 사라진다`() = runTest {
        addFavorites("가", "나")
        collectUiState()
        advanceUntilIdle()

        viewModel.onFavoriteClick(favorites().first())
        advanceUntilIdle()

        assertEquals(listOf("나"), titles())
    }

    private suspend fun addFavorites(vararg titles: String) {
        titles.forEachIndexed { index, title -> dao.insert(favoriteBookEntity(isbn(index + 1), title = title)) }
    }

    /** "책 01", "책 02" … 처럼 제목순과 번호순이 같은 제목들 */
    private fun pagedTitles(count: Int): Array<String> = Array(count) { "책 %02d".format(it + 1) }

    /** uiState는 구독하는 동안만 동작하므로 화면처럼 구독해 둡니다. */
    private fun TestScope.collectUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    private fun favorites(): List<BookUiModel> = (viewModel.uiState.value.result as UiState.Success).data

    private fun titles(): List<String> = favorites().map { it.title }
}
