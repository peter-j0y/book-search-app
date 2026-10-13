package com.peter.booksearchapp.feature.book.domain.usecase

import com.peter.booksearchapp.feature.book.data.repository.BookRepository
import com.peter.booksearchapp.feature.book.domain.mapper.toFavoriteBookEntity
import com.peter.booksearchapp.feature.book.domain.model.Book
import javax.inject.Inject

class SetFavoriteUseCase @Inject constructor(
    private val bookRepository: BookRepository,
) {
    suspend operator fun invoke(book: Book, isFavorite: Boolean) {
        if (isFavorite) {
            bookRepository.addFavoriteBook(book.toFavoriteBookEntity())
        } else {
            bookRepository.removeFavoriteBook(book.isbn)
        }
    }
}
