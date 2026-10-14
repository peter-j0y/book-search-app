package com.peter.booksearchapp.feature.book.data.local.model

import com.peter.booksearchapp.core.database.entity.FavoriteBookEntity

data class FavoriteBookCursor(
    val title: String,
    val isbn: String,
)

fun FavoriteBookEntity.toCursor(): FavoriteBookCursor = FavoriteBookCursor(title = title, isbn = isbn)
