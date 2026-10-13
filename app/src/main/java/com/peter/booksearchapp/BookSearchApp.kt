package com.peter.booksearchapp

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.peter.booksearchapp.feature.book.presentation.navigation.BookDetailNavKey
import com.peter.booksearchapp.feature.book.presentation.navigation.bookEntries
import com.peter.booksearchapp.navigation.TopLevelDestination
import com.peter.booksearchapp.navigation.rememberBookSearchNavigationState

@Composable
fun BookSearchApp(modifier: Modifier = Modifier) {
    val navigationState = rememberBookSearchNavigationState()
    val entryProvider = entryProvider {
        bookEntries(
            onBookClick = { navigationState.navigate(BookDetailNavKey(it)) },
            onBackClick = navigationState::goBack,
        )
    }

    val layoutType = if (navigationState.isAtTopLevel) {
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
            onBack = navigationState::goBack,
        )
    }
}
