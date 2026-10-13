package com.peter.booksearchapp

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.defaultPopTransitionSpec
import androidx.navigation3.ui.defaultTransitionSpec
import com.peter.booksearchapp.feature.book.presentation.navigation.BookDetailNavKey
import com.peter.booksearchapp.feature.book.presentation.navigation.bookEntries
import com.peter.booksearchapp.navigation.TopLevelDestination
import com.peter.booksearchapp.navigation.rememberBookSearchNavigationState

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BookSearchApp(modifier: Modifier = Modifier) {
    val navigationState = rememberBookSearchNavigationState()
    val entryProvider = entryProvider {
        bookEntries(
            onBookClick = { book ->
                navigationState.navigate(BookDetailNavKey(book), replaceTop = navigationState.topKey is BookDetailNavKey)
            },
            onBackClick = navigationState::goBack,
        )
    }

    val paneDirective = calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth(currentWindowAdaptiveInfo())
    val isTwoPane = paneDirective.maxHorizontalPartitions > 1
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = paneDirective)

    BackHandler(enabled = navigationState.shouldBackToStartTab) {
        navigationState.goBack()
    }

    val layoutType = if (navigationState.isAtTopLevel || isTwoPane) {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    } else {
        NavigationSuiteType.None
    }

    NavigationSuiteScaffold(
        modifier = modifier,
        layoutType = layoutType,
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                item(
                    selected = destination == navigationState.currentTab,
                    onClick = { navigationState.selectTab(destination) },
                    icon = { Icon(painter = painterResource(destination.iconRes), contentDescription = null) },
                    label = { Text(text = stringResource(destination.labelRes)) },
                )
            }
        },
    ) {
        NavDisplay(
            entries = navigationState.rememberEntries(entryProvider),
            sceneStrategies = listOf(listDetailStrategy),
            transitionSpec = withoutTabSwitchAnimation(defaultTransitionSpec()),
            popTransitionSpec = withoutTabSwitchAnimation(defaultPopTransitionSpec()),
            onBack = navigationState::goBack,
        )
    }
}

/**
 * 탭을 바꿀 때는 애니메이션 없이 바로 바꾸고, 같은 탭 안에서 화면을 오갈 때(목록 ↔ 상세)는 [transition]을 씁니다.
 * 전환 전후 화면의 첫 화면(탭의 루트)이 다르면 탭을 바꾼 것으로 봅니다.
 */
private fun <T : Any> withoutTabSwitchAnimation(
    transition: AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform,
): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    if (initialState.rootContentKey != targetState.rootContentKey) {
        EnterTransition.None togetherWith ExitTransition.None
    } else {
        transition()
    }
}

/** 화면이 속한 탭의 첫 화면을 구분하는 키 */
private val Scene<*>.rootContentKey: Any
    get() = (previousEntries.firstOrNull() ?: entries.first()).contentKey
