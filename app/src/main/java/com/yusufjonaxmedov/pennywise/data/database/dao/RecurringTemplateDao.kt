package com.yusufjonaxmedov.pennywise.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yusufjonaxmedov.pennywise.data.database.entity.RecurringTemplateEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface RecurringTemplateDao {
    @Query("SELECT * FROM recurring_templates ORDER BY active DESC, nextOccurrenceDate ASC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<RecurringTemplateEntity>>

    @Query("SELECT * FROM recurring_templates WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RecurringTemplateEntity?

    @Query("SELECT * FROM recurring_templates WHERE active = 1 AND nextOccurrenceDate <= :currentDate")
    suspend fun getDueTemplates(currentDate: LocalDate): List<RecurringTemplateEntity>

    @Query("SELECT COUNT(*) FROM recurring_templates WHERE active = 1 AND nextOccurrenceDate <= :currentDate")
    fun observeDueTemplateCount(currentDate: LocalDate): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecurringTemplateEntity): Long

    @Update
    suspend fun update(entity: RecurringTemplateEntity)

    @Query("DELETE FROM recurring_templates WHERE id = :id")
    suspend fun deleteById(id: Long)
}
