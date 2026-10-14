package com.peter.booksearchapp.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = FavoriteBookEntity.TABLE_NAME,
    indices = [Index(value = ["title", "isbn"])],
)
data class FavoriteBookEntity(
    @PrimaryKey
    @ColumnInfo(name = "isbn") val isbn: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "contents") val contents: String?,
    @ColumnInfo(name = "url") val url: String?,
    @ColumnInfo(name = "datetime") val datetime: String?,
    @ColumnInfo(name = "authors") val authors: List<String>?,
    @ColumnInfo(name = "publisher") val publisher: String?,
    @ColumnInfo(name = "translators") val translators: List<String>?,
    @ColumnInfo(name = "price") val price: Int?,
    @ColumnInfo(name = "sale_price") val salePrice: Int?,
    @ColumnInfo(name = "thumbnail") val thumbnail: String?,
    @ColumnInfo(name = "status") val status: String?,
) {
    companion object {
        const val TABLE_NAME = "favorite_books"
    }
}
