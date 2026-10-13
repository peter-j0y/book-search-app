package com.peter.booksearchapp.feature.book.domain.model

enum class BookSearchSort(internal val apiValue: String) {
    ACCURACY("accuracy"),
    LATEST("latest"),
}
