package com.peter.booksearchapp.feature.book.data.local.model

import com.peter.booksearchapp.core.database.entity.FavoriteBookEntity

data class FavoriteBookCursor(
    val title: String,
    val id: Long,
)

fun FavoriteBookEntity.toCursor(): FavoriteBookCursor = FavoriteBookCursor(title = title, id = id)
