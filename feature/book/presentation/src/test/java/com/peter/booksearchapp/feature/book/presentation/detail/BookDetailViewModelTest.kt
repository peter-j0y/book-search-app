package com.peter.booksearchapp.feature.book.presentation.detail

import com.peter.booksearchapp.core.database.FakeFavoriteBookDao
import com.peter.booksearchapp.core.database.favoriteBookEntity
import com.peter.booksearchapp.feature.book.data.testBookRepository
import com.peter.booksearchapp.feature.book.domain.usecase.ObserveFavoriteIsbnsUseCase
import com.peter.booksearchapp.feature.book.domain.usecase.SetFavoriteUseCase
import com.peter.booksearchapp.feature.book.presentation.MainDispatcherRule
import com.peter.booksearchapp.feature.book.presentation.book
import com.peter.booksearchapp.feature.book.presentation.isbn
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.model.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dao = FakeFavoriteBookDao()
    private lateinit var viewModel: BookDetailViewModel

    @Before
    fun setUp() {
        val repository = testBookRepository(dao = dao)
        viewModel = BookDetailViewModel(
            book = book(isbn(1)),
            observeFavoriteIsbns = ObserveFavoriteIsbnsUseCase(repository),
            setFavorite = SetFavoriteUseCase(repository),
        )
    }

    @Test
    fun `다른 화면에서 바꾼 즐겨찾기 여부를 반영한다`() = runTest {
        collectUiState()
        advanceUntilIdle()
        assertFalse(detail().isFavorite)

        dao.insert(favoriteBookEntity(isbn(1)))
        advanceUntilIdle()

        assertTrue(detail().isFavorite)
    }

    @Test
    fun `하트를 누르면 즐겨찾기에 추가하고 다시 누르면 해제한다`() = runTest {
        collectUiState()
        advanceUntilIdle()

        viewModel.onFavoriteClick(detail())
        advanceUntilIdle()
        assertEquals(listOf(isbn(1)), dao.favoriteBooks.map { it.isbn })

        viewModel.onFavoriteClick(detail())
        advanceUntilIdle()
        assertTrue(dao.favoriteBooks.isEmpty())
    }

    /** uiState는 구독하는 동안만 동작하므로 화면처럼 구독해 둡니다. */
    private fun TestScope.collectUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    private fun detail(): BookUiModel = (viewModel.uiState.value as UiState.Success).data
}
