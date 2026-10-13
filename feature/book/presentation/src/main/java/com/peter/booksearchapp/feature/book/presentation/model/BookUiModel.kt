package com.peter.booksearchapp.feature.book.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class BookUiModel(
    val id: String,
    val title: String,
    val authors: List<String>,
    val publisher: String,
    val publishedDate: String,
    val isbn: String,
    val price: Int?,
    val salePrice: Int?,
    val thumbnailUrl: String?,
    val contents: String,
    val isFavorite: Boolean,
)
