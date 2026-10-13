package com.peter.booksearchapp

import androidx.activity.compose.BackHandler
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
import androidx.navigation3.ui.NavDisplay
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
            onBack = navigationState::goBack,
        )
    }
}
