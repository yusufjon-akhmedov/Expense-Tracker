package com.yusufjonaxmedov.pennywise.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yusufjonaxmedov.pennywise.data.database.entity.SavedFilterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedFilterDao {
    @Query("SELECT * FROM saved_filters ORDER BY pinned DESC, createdAt DESC")
    fun observeAll(): Flow<List<SavedFilterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SavedFilterEntity): Long

    @Query("DELETE FROM saved_filters WHERE id = :id")
    suspend fun deleteById(id: Long)
}
