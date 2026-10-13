package com.peter.booksearchapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.peter.booksearchapp.core.database.entity.FavoriteBookEntity
import kotlinx.coroutines.flow.Flow

private const val SELLING_PRICE = "CASE WHEN sale_price >= 0 THEN sale_price ELSE price END"

@Dao
interface FavoriteBookDao {
    /**
     * 즐겨찾기를 제목순으로 limit개씩 가져옵니다.
     * - afterTitle, afterId: 직전 페이지의 마지막 책. 이 책 다음부터 가져옵니다. 첫 페이지는 null.
     *   제목이 같은 책이 있을 수 있어서 id까지 함께 비교합니다.
     * - query, minPrice, maxPrice: null이면 그 조건은 적용하지 않습니다.
     * - 금액은 할인가가 있으면 할인가, 없으면 정가로 비교합니다. (minPrice 이상, maxPrice 미만)
     */
    @Query(
        """
        SELECT * FROM favorite_books
        WHERE (:afterTitle IS NULL OR title > :afterTitle OR (title = :afterTitle AND id > :afterId))
          AND (:query IS NULL OR title LIKE '%' || :query || '%' ESCAPE '\')
          AND (:minPrice IS NULL OR $SELLING_PRICE >= :minPrice)
          AND (:maxPrice IS NULL OR $SELLING_PRICE < :maxPrice)
        ORDER BY title ASC, id ASC
        LIMIT :limit
        """,
    )
    suspend fun getFavoriteBooksByTitleAsc(
        afterTitle: String?,
        afterId: Long?,
        query: String?,
        minPrice: Int?,
        maxPrice: Int?,
        limit: Int,
    ): List<FavoriteBookEntity>

    /** [getFavoriteBooksByTitleAsc]의 역순(제목 내림차순) 버전입니다. */
    @Query(
        """
        SELECT * FROM favorite_books
        WHERE (:afterTitle IS NULL OR title <:afterTitle OR (title = :afterTitle AND id < :afterId))
          AND (:query IS NULL OR title LIKE '%' || :query || '%' ESCAPE '\')
          AND (:minPrice IS NULL OR $SELLING_PRICE >= :minPrice)
          AND (:maxPrice IS NULL OR $SELLING_PRICE < :maxPrice)
        ORDER BY title DESC, id DESC
        LIMIT :limit
        """,
    )
    suspend fun getFavoriteBooksByTitleDesc(
        afterTitle: String?,
        afterId: Long?,
        query: String?,
        minPrice: Int?,
        maxPrice: Int?,
        limit: Int,
    ): List<FavoriteBookEntity>

    @Query("SELECT isbn FROM favorite_books WHERE isbn IN (:isbns)")
    fun observeFavoriteIsbns(isbns: List<String>): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(book: FavoriteBookEntity)

    @Query("DELETE FROM favorite_books WHERE isbn = :isbn")
    suspend fun deleteByIsbn(isbn: String)
}
