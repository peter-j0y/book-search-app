package com.peter.booksearchapp.feature.book.domain.mapper

import com.peter.booksearchapp.core.database.entity.FavoriteBookEntity
import com.peter.booksearchapp.feature.book.data.local.model.toCursor
import com.peter.booksearchapp.feature.book.data.remote.model.BookDocumentDto
import com.peter.booksearchapp.feature.book.data.remote.model.BookSearchResponse
import com.peter.booksearchapp.feature.book.domain.model.Book
import com.peter.booksearchapp.feature.book.domain.model.BookSearchPage
import com.peter.booksearchapp.feature.book.domain.model.FavoriteBookPage

// 카카오 API는 50페이지까지만 결과를 줍니다. 그 뒤 페이지도 200으로 응답하므로 직접 막습니다.
internal const val MAX_SEARCH_PAGE = 50

// 결과가 0건이어도 is_end=false로 오는 경우가 있기 때문에 is_end와 documents.size, 요청한 페이지를 모두 고려해 마지막 페이지 여부를 판단합니다.
internal fun BookSearchResponse.toBookSearchPage(page: Int): BookSearchPage {
    val documents = documents.orEmpty()
    return BookSearchPage(
        books = documents.mapNotNull { it.toBookOrNull() },
        isEnd = meta?.isEnd == true || documents.isEmpty() || page >= MAX_SEARCH_PAGE,
    )
}

internal fun BookDocumentDto.toBookOrNull(): Book? = toBookOrNull(
    isbn = isbn,
    title = title,
    contents = contents,
    url = url,
    datetime = datetime,
    authors = authors,
    publisher = publisher,
    translators = translators,
    price = price,
    salePrice = salePrice,
    thumbnail = thumbnail,
    status = status,
)

// 요청한 개수보다 적게 오면 마지막 페이지입니다.
internal fun List<FavoriteBookEntity>.toFavoriteBookPage(requestedSize: Int): FavoriteBookPage =
    FavoriteBookPage(
        books = mapNotNull { it.toBookOrNull() },
        nextCursor = if (size < requestedSize) null else last().toCursor(),
    )

internal fun FavoriteBookEntity.toBookOrNull(): Book? = toBookOrNull(
    isbn = isbn,
    title = title,
    contents = contents,
    url = url,
    datetime = datetime,
    authors = authors,
    publisher = publisher,
    translators = translators,
    price = price,
    salePrice = salePrice,
    thumbnail = thumbnail,
    status = status,
)

internal fun Book.toFavoriteBookEntity(): FavoriteBookEntity = FavoriteBookEntity(
    title = title,
    contents = contents,
    url = url,
    isbn = isbn,
    datetime = datetime,
    authors = authors,
    publisher = publisher,
    translators = translators,
    price = price,
    salePrice = salePrice,
    thumbnail = thumbnail,
    status = status,
)

private fun toBookOrNull(
    isbn: String?,
    title: String?,
    contents: String?,
    url: String?,
    datetime: String?,
    authors: List<String>?,
    publisher: String?,
    translators: List<String>?,
    price: Int?,
    salePrice: Int?,
    thumbnail: String?,
    status: String?,
): Book? {
    val isbn13 = isbn?.let(::normalizeIsbn13)
    if (isbn13 == null || title.isNullOrBlank()) return null
    return Book(
        isbn = isbn13,
        title = title,
        contents = contents.orEmpty(),
        url = url.orEmpty(),
        datetime = datetime.orEmpty(),
        authors = authors.orEmpty(),
        publisher = publisher.orEmpty(),
        translators = translators.orEmpty(),
        price = price?.takeIf { it >= 0 },
        salePrice = salePrice?.takeIf { it >= 0 },
        thumbnail = thumbnail?.takeIf { it.isNotBlank() },
        status = status.orEmpty(),
    )
}

// 카카오 API는 isbn에 "ISBN10 ISBN13"을 공백으로 이어 주고, 둘 중 하나만 줄 때도 있습니다.
// 어떤 조합으로 와도 같은 책이 같은 키를 갖도록 ISBN13 하나로 맞춥니다. 형식에 맞는 값이 없으면 null입니다.
internal fun normalizeIsbn13(raw: String): String? {
    val candidates = raw.split(' ').map { it.replace("-", "") }
    return candidates.firstOrNull { it.length == 13 && it.all(Char::isDigit) }
        ?: candidates.firstOrNull(::isIsbn10)?.let(::isbn10ToIsbn13)
}

private fun isIsbn10(value: String): Boolean =
    value.length == 10 && value.take(9).all(Char::isDigit) && (value.last().isDigit() || value.last() in "Xx")

// ISBN10은 앞에 978을 붙이고 체크 숫자(마지막 자리)를 ISBN13 방식으로 다시 계산하면 ISBN13이 됩니다.
private fun isbn10ToIsbn13(isbn10: String): String {
    val body = "978" + isbn10.take(9)
    val sum = body.mapIndexed { index, c -> c.digitToInt() * if (index % 2 == 0) 1 else 3 }.sum()
    return body + (10 - sum % 10) % 10
}
