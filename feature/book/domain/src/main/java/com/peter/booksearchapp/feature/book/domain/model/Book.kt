package com.peter.booksearchapp.feature.book.domain.model

data class Book(
    val isbn: String,
    val title: String,
    val contents: String,
    val url: String,
    val datetime: String,
    val authors: List<String>,
    val publisher: String,
    val translators: List<String>,
    val price: Int?,
    val salePrice: Int?,
    val thumbnail: String?,
    val status: String,
) {
    val sellingPrice: Int? get() = salePrice ?: price
}
