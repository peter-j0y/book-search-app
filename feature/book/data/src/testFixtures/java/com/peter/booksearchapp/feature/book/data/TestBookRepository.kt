package com.peter.booksearchapp.feature.book.data

import com.peter.booksearchapp.core.database.FakeFavoriteBookDao
import com.peter.booksearchapp.feature.book.data.local.BookLocalDataSource
import com.peter.booksearchapp.feature.book.data.remote.BookRemoteDataSource
import com.peter.booksearchapp.feature.book.data.repository.BookRepository

/**
 * 실제 데이터소스와 [BookRepository]를 만들고, 맨 아래의 API · DAO만 가짜로 연결합니다.
 * 다른 모듈의 테스트에서도 internal 생성자를 건드리지 않고 Repository를 만들 수 있게 합니다.
 */
fun testBookRepository(
    api: FakeBookApi = FakeBookApi(),
    dao: FakeFavoriteBookDao = FakeFavoriteBookDao(),
): BookRepository = BookRepository(
    remoteDataSource = BookRemoteDataSource(api.asBookApi()),
    localDataSource = BookLocalDataSource(favoriteBookDao = dao, favoriteTableChanges = dao.tableChanges),
)
