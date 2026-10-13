package com.peter.booksearchapp.feature.book.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.peter.booksearchapp.feature.book.domain.model.Book
import kotlinx.serialization.Serializable

@Serializable
data object SearchNavKey : NavKey

@Serializable
data object FavoriteNavKey : NavKey

@Serializable
data class BookDetailNavKey(val book: Book) : NavKey
