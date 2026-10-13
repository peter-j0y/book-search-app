package com.peter.booksearchapp.core.database

import javax.inject.Qualifier

/**
 * 즐겨찾기 테이블이 바뀔 때마다(추가 · 삭제) 신호를 보내는 `Flow<Unit>`을 가리킵니다. 구독을 시작하면 바로 한 번 보냅니다.
 *
 * 데이터소스가 RoomDatabase를 직접 받지 않고 이 Flow만 받도록 해서, 단위 테스트에서 가짜 Flow로 바꿀 수 있게 합니다.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FavoriteTableChanges
