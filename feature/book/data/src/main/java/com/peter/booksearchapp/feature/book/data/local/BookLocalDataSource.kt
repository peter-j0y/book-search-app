package com.peter.booksearchapp.feature.book.data.local

import com.peter.booksearchapp.core.database.BookSearchDatabase
import com.peter.booksearchapp.core.database.dao.FavoriteBookDao
import com.peter.booksearchapp.core.database.entity.FavoriteBookEntity
import com.peter.booksearchapp.feature.book.data.local.model.FavoriteBookCursor
import com.peter.booksearchapp.feature.book.data.local.model.FavoriteBookSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class BookLocalDataSource @Inject constructor(
    private val database: BookSearchDatabase,
    private val favoriteBookDao: FavoriteBookDao,
) {
    suspend fun getFavoriteBooks(
        sort: FavoriteBookSort,
        after: FavoriteBookCursor?,
        query: String?,
        minPrice: Int?,
        maxPrice: Int?,
        size: Int,
    ): List<FavoriteBookEntity> {
        val escapedQuery = query?.takeIf { it.isNotBlank() }?.escapeLikePattern()
        return when (sort) {
            FavoriteBookSort.TITLE_ASC -> favoriteBookDao.getFavoriteBooksByTitleAsc(
                afterTitle = after?.title,
                afterId = after?.id,
                query = escapedQuery,
                minPrice = minPrice,
                maxPrice = maxPrice,
                limit = size,
            )
            FavoriteBookSort.TITLE_DESC -> favoriteBookDao.getFavoriteBooksByTitleDesc(
                afterTitle = after?.title,
                afterId = after?.id,
                query = escapedQuery,
                minPrice = minPrice,
                maxPrice = maxPrice,
                limit = size,
            )
        }
    }

    fun observeFavoriteIsbns(isbns: List<String>): Flow<List<String>> = favoriteBookDao.observeFavoriteIsbns(isbns)

    fun observeFavoriteTableChanges(): Flow<Unit> =
        database.invalidationTracker.createFlow(FavoriteBookEntity.TABLE_NAME, emitInitialState = true).map { }

    suspend fun addFavoriteBook(book: FavoriteBookEntity) = favoriteBookDao.insert(book)

    suspend fun removeFavoriteBook(isbn: String) = favoriteBookDao.deleteByIsbn(isbn)

    /** 검색어에 든 %, _가 LIKE 와일드카드로 해석되지 않도록 DAO의 ESCAPE '\'에 맞춰 이스케이프합니다. */
    private fun String.escapeLikePattern(): String =
        replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
}
