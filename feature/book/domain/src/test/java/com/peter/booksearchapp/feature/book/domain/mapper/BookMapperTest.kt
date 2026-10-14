package com.peter.booksearchapp.feature.book.domain.mapper

import com.peter.booksearchapp.core.database.entity.FavoriteBookEntity
import com.peter.booksearchapp.feature.book.data.local.model.FavoriteBookCursor
import com.peter.booksearchapp.feature.book.data.remote.model.BookDocumentDto
import com.peter.booksearchapp.feature.book.data.remote.model.BookSearchResponse
import com.peter.booksearchapp.feature.book.data.remote.model.MetaDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookMapperTest {
    @Test
    fun `is_end가 false여도 결과가 0건이면 마지막 페이지다`() {
        val page = response(isEnd = false, documents = emptyList()).toBookSearchPage(page = 1)

        assertTrue(page.isEnd)
    }

    @Test
    fun `50페이지면 is_end가 false여도 마지막 페이지다`() {
        val page = response(isEnd = false, documents = listOf(document())).toBookSearchPage(page = MAX_SEARCH_PAGE)

        assertTrue(page.isEnd)
    }

    @Test
    fun `결과가 있고 is_end가 false면 다음 페이지가 있다`() {
        val page = response(isEnd = false, documents = listOf(document())).toBookSearchPage(page = 1)

        assertFalse(page.isEnd)
    }

    @Test
    fun `음수 가격과 빈 썸네일은 null로 바꾼다`() {
        val dto = document(price = -1, salePrice = -1, thumbnail = "")

        val book = response(documents = listOf(dto)).toBookSearchPage(page = 1).books.single()

        assertNull(book.price)
        assertNull(book.salePrice)
        assertNull(book.thumbnail)
    }

    @Test
    fun `ISBN이나 제목이 없는 책은 제외한다`() {
        val documents = listOf(document(isbn = null), document(isbn = " "), document(title = null), document())

        val books = response(documents = documents).toBookSearchPage(page = 1).books

        assertEquals(1, books.size)
    }

    @Test
    fun `ISBN10과 ISBN13이 같이 오면 ISBN13을 쓴다`() {
        assertEquals("9788937460449", normalizeIsbn13("8937460440 9788937460449"))
    }

    @Test
    fun `ISBN13만 오면 앞뒤 공백을 지우고 그대로 쓴다`() {
        assertEquals("9788937460449", normalizeIsbn13(" 9788937460449"))
    }

    @Test
    fun `ISBN10만 오면 ISBN13으로 바꾼다`() {
        assertEquals("9788937460449", normalizeIsbn13("8937460440 "))
    }

    @Test
    fun `체크 숫자가 X인 ISBN10도 ISBN13으로 바꾼다`() {
        assertEquals("9780804429573", normalizeIsbn13("080442957X"))
    }

    @Test
    fun `ISBN 형식에 맞는 값이 없으면 null이다`() {
        assertNull(normalizeIsbn13("12345 abc"))
    }

    @Test
    fun `할인가가 없으면 판매 가격은 정가다`() {
        val book = response(documents = listOf(document(price = 15000, salePrice = -1))).toBookSearchPage(page = 1).books.single()

        assertEquals(15000, book.sellingPrice)
    }

    @Test
    fun `즐겨찾기를 요청한 개수만큼 받으면 마지막 책이 다음 커서가 된다`() {
        val entities = listOf(entity(isbn = "9788937460441", title = "A"), entity(isbn = "9788937460442", title = "B"))

        val page = entities.toFavoriteBookPage(requestedSize = 2)

        assertEquals(FavoriteBookCursor(title = "B", isbn = "9788937460442"), page.nextCursor)
    }

    @Test
    fun `즐겨찾기를 요청한 개수보다 적게 받으면 다음 커서가 없다`() {
        val page = listOf(entity(isbn = "9788937460441", title = "A")).toFavoriteBookPage(requestedSize = 2)

        assertNull(page.nextCursor)
    }

    @Test
    fun `저장했다가 다시 읽어도 같은 책이다`() {
        val book = response(documents = listOf(document(salePrice = 9000))).toBookSearchPage(page = 1).books.single()

        assertEquals(book, book.toFavoriteBookEntity().toBookOrNull())
    }

    private fun response(isEnd: Boolean = false, documents: List<BookDocumentDto>) = BookSearchResponse(
        meta = MetaDto(totalCount = documents.size, pageableCount = documents.size, isEnd = isEnd),
        documents = documents,
    )

    private fun document(
        isbn: String? = "8937460440 9788937460449",
        title: String? = "데미안",
        price: Int? = 10000,
        salePrice: Int? = 9000,
        thumbnail: String? = "https://example.com/cover.jpg",
    ) = BookDocumentDto(
        title = title,
        contents = "소개",
        url = "https://example.com/book",
        isbn = isbn,
        datetime = "2000-12-20T00:00:00.000+09:00",
        authors = listOf("헤르만 헤세"),
        publisher = "민음사",
        translators = listOf("전영애"),
        price = price,
        salePrice = salePrice,
        thumbnail = thumbnail,
        status = "정상판매",
    )

    private fun entity(isbn: String, title: String) = FavoriteBookEntity(
        isbn = isbn,
        title = title,
        contents = null,
        url = null,
        datetime = null,
        authors = null,
        publisher = null,
        translators = null,
        price = null,
        salePrice = null,
        thumbnail = null,
        status = null,
    )
}
