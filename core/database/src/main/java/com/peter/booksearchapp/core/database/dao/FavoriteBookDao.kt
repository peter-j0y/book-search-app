package com.peter.booksearchapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.peter.booksearchapp.core.database.entity.FavoriteBookEntity

@Dao
interface FavoriteBookDao {
    @Query("SELECT * FROM favorite_books ORDER BY id DESC LIMIT :limit OFFSET :offset")
    suspend fun getFavoriteBooks(limit: Int, offset: Int): List<FavoriteBookEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(book: FavoriteBookEntity)

    @Query("DELETE FROM favorite_books WHERE isbn = :isbn")
    suspend fun deleteByIsbn(isbn: String)
}
