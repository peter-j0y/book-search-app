package com.peter.booksearchapp.feature.book.data

import com.peter.booksearchapp.core.network.ApiResponse
import com.peter.booksearchapp.feature.book.data.remote.api.BookApi
import com.peter.booksearchapp.feature.book.data.remote.model.BookDocumentDto
import com.peter.booksearchapp.feature.book.data.remote.model.BookSearchResponse
import com.peter.booksearchapp.feature.book.data.remote.model.MetaDto
import java.io.IOException

/**
 * 카카오 도서 검색 API 가짜. 받은 요청을 기록하고, 테스트가 [respond]로 정한 응답을 돌려줍니다.
 *
 * [BookApi]가 internal이라 이 클래스가 직접 구현하지 않고, [testBookRepository]에서 연결합니다.
 */
class FakeBookApi {
    private val receivedRequests = mutableListOf<SearchRequest>()

    /** 지금까지 받은 검색 요청 */
    val requests: List<SearchRequest>
        get() = receivedRequests.toList()

    /** 요청을 받아 돌려줄 응답을 정합니다. 기본값은 결과가 없는 마지막 페이지입니다. */
    var respond: suspend (SearchRequest) -> ApiResponse<BookSearchResponse> = { searchResponse(emptyList(), isEnd = true) }

    data class SearchRequest(
        val query: String,
        val sort: String?,
        val page: Int?,
        val size: Int?,
    )

    internal fun asBookApi(): BookApi = object : BookApi {
        override suspend fun searchBooks(query: String, sort: String?, page: Int?, size: Int?): ApiResponse<BookSearchResponse> {
            val request = SearchRequest(query, sort, page, size)
            receivedRequests += request
            return respond(request)
        }
    }
}

/** 검색 성공 응답 */
fun searchResponse(documents: List<BookDocumentDto>, isEnd: Boolean): ApiResponse<BookSearchResponse> =
    ApiResponse.Success(
        BookSearchResponse(
            meta = MetaDto(totalCount = documents.size, pageableCount = documents.size, isEnd = isEnd),
            documents = documents,
        ),
    )

/** 네트워크 연결 실패 응답 */
fun networkErrorResponse(): ApiResponse<Nothing> = ApiResponse.Failure.NetworkError(IOException("가짜 네트워크 오류"))

/** 테스트용 검색 결과 문서. 필요한 값만 바꿔서 씁니다. */
fun bookDocument(
    isbn: String,
    title: String = "책 $isbn",
    price: Int? = 10_000,
    salePrice: Int? = null,
): BookDocumentDto = BookDocumentDto(
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
