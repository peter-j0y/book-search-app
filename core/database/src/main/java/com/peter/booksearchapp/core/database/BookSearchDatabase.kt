package com.peter.booksearchapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.peter.booksearchapp.core.database.converter.StringListConverter
import com.peter.booksearchapp.core.database.dao.FavoriteBookDao
import com.peter.booksearchapp.core.database.entity.FavoriteBookEntity

@Database(
    entities = [FavoriteBookEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(StringListConverter::class)
abstract class BookSearchDatabase : RoomDatabase() {
    abstract fun favoriteBookDao(): FavoriteBookDao
}
