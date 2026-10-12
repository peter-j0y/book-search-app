package com.peter.booksearchapp.feature.book.presentation.model

import androidx.compose.runtime.Immutable

/**
 * 화면에 그릴 도서 한 권의 정보.
 *
 * 리스트 카드와 상세 화면이 같은 모델을 쓴다. API 응답의 null 처리와 값 변환
 * (예: 할인가 -1 → null, 날짜 포맷)은 이 모델을 만드는 쪽에서 끝내고,
 * Composable은 받은 값을 그리기만 한다.
 *
 * @property id 도서를 구분하는 키. 즐겨찾기 저장과 LazyList key에 쓴다.
 * @property salePrice 할인가가 없으면 null.
 */
@Immutable
data class BookUiModel(
    val id: String,
    val title: String,
    val authors: List<String>,
    val publisher: String,
    val publishedDate: String,
    val isbn: String,
    val price: Int?,
    val salePrice: Int?,
    val thumbnailUrl: String?,
    val contents: String,
    val isFavorite: Boolean,
)
