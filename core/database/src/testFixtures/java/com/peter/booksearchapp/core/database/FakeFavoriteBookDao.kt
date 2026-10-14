package com.peter.booksearchapp.core.database

import com.peter.booksearchapp.core.database.dao.FavoriteBookDao
import com.peter.booksearchapp.core.database.entity.FavoriteBookEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * 메모리 목록으로 동작하는 [FavoriteBookDao] 가짜. 다른 모듈의 ViewModel · Repository 테스트에서 씁니다.
 *
 * 검색어 · 금액 필터 · 제목 정렬 · 커서 페이징을 단순하게 흉내 낼 뿐, 실제 SQL과 똑같이 동작한다고 보장하지 않습니다.
 * 그래서 이 가짜로 SQL 자체가 맞는지는 테스트하지 않습니다.
 */
class FakeFavoriteBookDao : FavoriteBookDao {
    private val books = MutableStateFlow<List<FavoriteBookEntity>>(emptyList())

    /** 지금 저장된 즐겨찾기 */
    val favoriteBooks: List<FavoriteBookEntity>
        get() = books.value

    /** 즐겨찾기가 바뀔 때마다 신호를 보냅니다. 실제 [FavoriteTableChanges] Flow처럼 구독하면 바로 한 번 보냅니다. */
    val tableChanges: Flow<Unit> = books.map { }

    override suspend fun getFavoriteBooksByTitleAsc(
        afterTitle: String?,
        afterIsbn: String?,
        query: String?,
        minPrice: Int?,
        maxPrice: Int?,
        limit: Int,
    ): List<FavoriteBookEntity> = findBooks(afterTitle, afterIsbn, query, minPrice, maxPrice, limit, descending = false)

    override suspend fun getFavoriteBooksByTitleDesc(
        afterTitle: String?,
        afterIsbn: String?,
        query: String?,
        minPrice: Int?,
        maxPrice: Int?,
        limit: Int,
    ): List<FavoriteBookEntity> = findBooks(afterTitle, afterIsbn, query, minPrice, maxPrice, limit, descending = true)

    override fun observeFavoriteIsbns(isbns: List<String>): Flow<List<String>> =
        books.map { list -> list.map { it.isbn }.filter { it in isbns } }

    override suspend fun insert(book: FavoriteBookEntity) {
        books.update { list -> if (list.any { it.isbn == book.isbn }) list else list + book }
    }

    override suspend fun deleteByIsbn(isbn: String) {
        books.update { list -> list.filterNot { it.isbn == isbn } }
    }

    private fun findBooks(
        afterTitle: String?,
        afterIsbn: String?,
        query: String?,
        minPrice: Int?,
        maxPrice: Int?,
        limit: Int,
        descending: Boolean,
    ): List<FavoriteBookEntity> {
        val byTitle = compareBy<FavoriteBookEntity>({ it.title }, { it.isbn })
        val keyword = query?.unescapeLikePattern()
        return books.value
            .asSequence()
            .filter { afterTitle == null || it.isAfter(afterTitle, afterIsbn.orEmpty(), descending) }
            .filter { keyword == null || it.title.contains(keyword, ignoreCase = true) }
            .filter { minPrice == null || it.sellingPrice?.let { price -> price >= minPrice } == true }
            .filter { maxPrice == null || it.sellingPrice?.let { price -> price < maxPrice } == true }
            .sortedWith(if (descending) byTitle.reversed() else byTitle)
            .take(limit)
            .toList()
    }

    private fun FavoriteBookEntity.isAfter(afterTitle: String, afterIsbn: String, descending: Boolean): Boolean =
        if (descending) {
            title < afterTitle || (title == afterTitle && isbn < afterIsbn)
        } else {
            title > afterTitle || (title == afterTitle && isbn > afterIsbn)
        }

    /** DAO 쿼리와 같이 할인가가 있으면 할인가, 없으면 정가로 비교합니다. */
    private val FavoriteBookEntity.sellingPrice: Int?
        get() = salePrice?.takeIf { it >= 0 } ?: price

    /** 데이터소스가 LIKE용으로 이스케이프한 검색어(\%, \_, \\)를 원래 글자로 되돌립니다. */
    private fun String.unescapeLikePattern(): String = replace(Regex("""\\(.)"""), "$1")
}

/** 테스트용 즐겨찾기 엔티티. 필요한 값만 바꿔서 씁니다. */
fun favoriteBookEntity(
    isbn: String,
    title: String = "책 $isbn",
    price: Int? = 10_000,
    salePrice: Int? = null,
): FavoriteBookEntity = FavoriteBookEntity(
    title = title,
    contents = "",
    url = "",
    isbn = isbn,
    datetime = "2024-01-01T00:00:00.000+09:00",
    authors = listOf("저자"),
    publisher = "출판사",
    translators = emptyList(),
    price = price,
    salePrice = salePrice,
    thumbnail = null,
    status = "정상판매",
)
