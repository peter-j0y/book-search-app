package com.peter.booksearchapp.feature.book.presentation

import com.peter.booksearchapp.feature.book.domain.model.Book

/** 형식에 맞는 ISBN13. 번호만 바꿔 서로 다른 책을 만듭니다. */
fun isbn(number: Int): String = "978" + number.toString().padStart(10, '0')

/** 테스트용 도서 */
fun book(isbn: String, title: String = "책 $isbn"): Book = Book(
    isbn = isbn,
    title = title,
    contents = "",
    url = "",
    datetime = "2024-01-01T00:00:00.000+09:00",
    authors = listOf("저자"),
    publisher = "출판사",
    translators = emptyList(),
    price = 10_000,
    salePrice = null,
    thumbnail = null,
    status = "정상판매",
)
