package com.peter.booksearchapp.feature.book.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookSearchResponse(
    @SerialName("meta") val meta: MetaDto?,
    @SerialName("documents") val documents: List<BookDocumentDto>?,
)

@Serializable
data class MetaDto(
    @SerialName("total_count") val totalCount: Int?,
    @SerialName("pageable_count") val pageableCount: Int?,
    @SerialName("is_end") val isEnd: Boolean?,
)

@Serializable
data class BookDocumentDto(
    @SerialName("title") val title: String?,
    @SerialName("contents") val contents: String?,
    @SerialName("url") val url: String?,
    @SerialName("isbn") val isbn: String?,
    @SerialName("datetime") val datetime: String?,
    @SerialName("authors") val authors: List<String>?,
    @SerialName("publisher") val publisher: String?,
    @SerialName("translators") val translators: List<String>?,
    @SerialName("price") val price: Int?,
    @SerialName("sale_price") val salePrice: Int?,
    @SerialName("thumbnail") val thumbnail: String?,
    @SerialName("status") val status: String?,
)
