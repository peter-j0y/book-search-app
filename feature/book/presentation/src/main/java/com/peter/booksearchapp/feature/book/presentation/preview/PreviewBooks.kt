package com.peter.booksearchapp.feature.book.presentation.preview

import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel

/** Preview 전용 샘플 데이터. 실제 화면 코드에서는 쓰지 않는다. */
internal object PreviewBooks {
    val book = BookUiModel(
        id = "9788966262281",
        title = "코틀린 인 액션",
        authors = listOf("드미트리 제메로프", "스베트라나 이사코바"),
        publisher = "에이콘출판",
        publishedDate = "2017.10.31",
        isbn = "8966262287 9788966262281",
        price = 36_000,
        salePrice = 32_400,
        thumbnailUrl = null,
        contents = "코틀린이 무엇이며, 코틀린을 사용하면 어떤 장점이 있는지 설명하고, " +
            "코틀린의 기본 문법부터 고급 기능까지 다룬다. 자바 개발자가 코틀린을 빠르게 익힐 수 " +
            "있도록 자바와 비교하며 설명하고, 실제 프로젝트에 코틀린을 도입하는 방법도 소개한다.",
        isFavorite = true,
    )

    val longTitleBook = book.copy(
        id = "9791162241295",
        title = "이펙티브 코틀린: 안드로이드 개발자를 위한 코틀린 프로그래밍 모범 사례와 실전 가이드",
        authors = listOf("마르친 모스칼라"),
        publisher = "인사이트",
        publishedDate = "2022.01.05",
        isbn = "9791162241295",
        price = 28_000,
        salePrice = null,
        isFavorite = false,
    )

    val noInfoBook = book.copy(
        id = "unknown",
        title = "정보가 비어 있는 도서",
        authors = emptyList(),
        publisher = "",
        publishedDate = "",
        isbn = "",
        price = null,
        salePrice = null,
        contents = "",
        isFavorite = false,
    )

    val books: List<BookUiModel> = List(10) { index ->
        val base = if (index % 2 == 0) book else longTitleBook
        base.copy(id = "${base.id}-$index", isFavorite = index % 3 == 0)
    }
}
