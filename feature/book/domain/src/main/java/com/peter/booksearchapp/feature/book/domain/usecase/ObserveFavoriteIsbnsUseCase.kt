package com.peter.booksearchapp.feature.book.domain.usecase

import com.peter.booksearchapp.feature.book.data.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ObserveFavoriteIsbnsUseCase @Inject constructor(
    private val bookRepository: BookRepository,
) {
    operator fun invoke(isbns: List<String>): Flow<Set<String>> {
        if (isbns.isEmpty()) return flowOf(emptySet())
        val flows = isbns.distinct().chunked(MAX_QUERY_ARGS).map { bookRepository.observeFavoriteIsbns(it) }
        return combine(flows) { results -> results.flatMap { it }.toSet() }
    }

    private companion object {
        /** SQLite 3.32.0 미만은 쿼리 인자 999개가 기본 한도이기 때문에 이 개수씩 나눠서 요청합니다. (https://www.sqlite.org/limits.html) */
        const val MAX_QUERY_ARGS = 500
    }
}
