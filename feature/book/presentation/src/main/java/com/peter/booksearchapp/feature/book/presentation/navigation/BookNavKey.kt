package com.peter.booksearchapp.feature.book.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import kotlinx.serialization.Serializable

@Serializable
data object SearchNavKey : NavKey

@Serializable
data object FavoriteNavKey : NavKey

@Serializable
data class BookDetailNavKey(val book: BookUiModel) : NavKey
