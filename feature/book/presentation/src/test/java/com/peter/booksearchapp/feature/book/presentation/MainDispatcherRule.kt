package com.peter.booksearchapp.feature.book.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * 테스트 동안 Dispatchers.Main을 테스트 디스패처로 바꿉니다. viewModelScope가 Main에서 돌기 때문에 필요합니다.
 * runTest는 Main이 테스트 디스패처면 같은 가상 시계를 쓰므로, debounce 같은 시간도 advanceTimeBy로 넘길 수 있습니다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
