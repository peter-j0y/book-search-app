package com.peter.booksearchapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

/**
 * 탭마다 백스택을 따로 두어, 탭을 오가도 각 탭에서 쌓은 화면(예: 검색 결과에서 연 상세)이 유지되도록 합니다.
 */
@Stable
class BookSearchNavigationState(
    val startDestination: TopLevelDestination,
    currentTab: MutableState<TopLevelDestination>,
    private val backStacks: Map<TopLevelDestination, NavBackStack<NavKey>>,
) {
    var currentTab by currentTab
        private set

    private val currentBackStack: NavBackStack<NavKey>
        get() = backStacks.getValue(currentTab)

    /** 현재 탭의 첫 화면을 보고 있는지 여부. 상세 화면처럼 탭 위에 쌓인 화면에서는 탭 바를 숨깁니다. */
    val isAtTopLevel: Boolean
        get() = currentBackStack.size == 1

    /** 현재 탭 맨 위에 있는 화면 */
    val topKey: NavKey
        get() = currentBackStack.last()

    /** 시작 탭이 아닌 탭의 첫 화면에서는 뒤로 가기를 누르면 앱을 끄지 않고 시작 탭으로 돌아갑니다. */
    val shouldBackToStartTab: Boolean
        get() = currentTab != startDestination && isAtTopLevel

    fun selectTab(tab: TopLevelDestination) {
        if (tab == currentTab) {
            backStacks.getValue(tab).run { subList(1, size).clear() }
        } else {
            currentTab = tab
        }
    }

    /** [replaceTop]이 true면 맨 위 화면을 [key]로 바꾸고, 아니면 위에 쌓습니다. */
    fun navigate(key: NavKey, replaceTop: Boolean = false) {
        if (replaceTop) currentBackStack.removeLastOrNull()
        currentBackStack.add(key)
    }

    fun goBack() {
        if (currentBackStack.size > 1) {
            currentBackStack.removeLastOrNull()
        } else if (currentTab != startDestination) {
            currentTab = startDestination
        }
    }

    /**
     * 현재 탭의 화면만 넘깁니다. 다른 탭의 화면까지 이어 붙이면 리스트-상세 화면이 다른 탭에서 연 상세를 함께 묶어 보여줄 수 있습니다.
     * 화면을 넘기지 않는 탭도 상태는 유지되도록 모든 탭의 항목을 기억해 둡니다.
     */
    @Composable
    fun rememberEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
        val entriesByTab = backStacks.mapValues { (_, backStack) ->
            rememberDecoratedNavEntries(
                backStack = backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider,
            )
        }
        return entriesByTab.getValue(currentTab)
    }
}

@Composable
fun rememberBookSearchNavigationState(
    startDestination: TopLevelDestination = TopLevelDestination.SEARCH,
): BookSearchNavigationState {
    val currentTab = rememberSaveable { mutableStateOf(startDestination) }
    val backStacks = TopLevelDestination.entries.associateWith { rememberNavBackStack(it.key) }
    return remember(startDestination, currentTab, backStacks) {
        BookSearchNavigationState(startDestination, currentTab, backStacks)
    }
}
