package com.peter.booksearchapp.feature.book.presentation.model

import androidx.annotation.StringRes
import com.peter.booksearchapp.feature.book.presentation.R

/** 검색 탭 정렬. 카카오 도서 검색 API의 sort 파라미터(accuracy / latest)에 대응한다. */
enum class SearchSort(@StringRes val labelRes: Int) {
    ACCURACY(R.string.book_sort_accuracy),
    LATEST(R.string.book_sort_latest),
}

/** 즐겨찾기 탭 정렬. 로컬 데이터를 제목 기준으로 정렬한다. */
enum class FavoriteSort(@StringRes val labelRes: Int) {
    TITLE_ASC(R.string.book_sort_title_asc),
    TITLE_DESC(R.string.book_sort_title_desc),
}

/**
 * 즐겨찾기 탭 금액 필터. 판매 가격(할인가가 있으면 할인가) 기준이다.
 *
 * @property minPrice 이상(포함). null이면 하한 없음.
 * @property maxPrice 미만(미포함). null이면 상한 없음.
 */
enum class PriceFilter(
    @StringRes val labelRes: Int,
    val minPrice: Int?,
    val maxPrice: Int?,
) {
    ALL(R.string.book_price_filter_all, null, null),
    UNDER_10000(R.string.book_price_filter_under_10000, null, 10_000),
    FROM_10000_TO_20000(R.string.book_price_filter_10000_to_20000, 10_000, 20_000),
    FROM_20000_TO_30000(R.string.book_price_filter_20000_to_30000, 20_000, 30_000),
    OVER_30000(R.string.book_price_filter_over_30000, 30_000, null),
}
