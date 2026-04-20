package com.yusufjonaxmedov.pennywise.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.yusufjonaxmedov.pennywise.data.database.entity.TransactionEntity
import com.yusufjonaxmedov.pennywise.data.database.model.CategorySpendRow
import com.yusufjonaxmedov.pennywise.data.database.model.MonthSnapshotRow
import com.yusufjonaxmedov.pennywise.data.database.model.MonthlyTrendRow
import com.yusufjonaxmedov.pennywise.data.database.model.TransactionJoinedRow
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @RawQuery(
        observedEntities = [
            TransactionEntity::class,
            com.yusufjonaxmedov.pennywise.data.database.entity.CategoryEntity::class,
            com.yusufjonaxmedov.pennywise.data.database.entity.AccountEntity::class,
        ],
    )
    fun observeFiltered(query: SupportSQLiteQuery): Flow<List<TransactionJoinedRow>>

    @RawQuery(
        observedEntities = [
            TransactionEntity::class,
            com.yusufjonaxmedov.pennywise.data.database.entity.CategoryEntity::class,
            com.yusufjonaxmedov.pennywise.data.database.entity.AccountEntity::class,
        ],
    )
    suspend fun listFiltered(query: SupportSQLiteQuery): List<TransactionJoinedRow>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Query(
        """
        SELECT 
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountMinor ELSE 0 END), 0) AS incomeMinor,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountMinor ELSE 0 END), 0) AS expenseMinor
        FROM transactions
        WHERE transactionDate BETWEEN :startDate AND :endDate
        """,
    )
    fun observeMonthSnapshot(startDate: String, endDate: String): Flow<MonthSnapshotRow>

    @Query(
        """
        SELECT
            c.id AS categoryId,
            c.name AS categoryName,
            c.emoji AS categoryEmoji,
            c.colorHex AS categoryColorHex,
            COALESCE(SUM(t.amountMinor), 0) AS spentMinor
        FROM transactions t
        INNER JOIN categories c ON c.id = t.categoryId
        WHERE t.type = 'EXPENSE'
          AND t.transactionDate BETWEEN :startDate AND :endDate
        GROUP BY c.id
        ORDER BY spentMinor DESC
        LIMIT :limit
        """,
    )
    fun observeTopCategorySpend(
        startDate: String,
        endDate: String,
        limit: Int,
    ): Flow<List<CategorySpendRow>>

    @Query(
        """
        SELECT
            substr(transactionDate, 1, 7) AS monthKey,
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountMinor ELSE 0 END), 0) AS incomeMinor,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountMinor ELSE 0 END), 0) AS expenseMinor
        FROM transactions
        WHERE transactionDate BETWEEN :startDate AND :endDate
        GROUP BY substr(transactionDate, 1, 7)
        ORDER BY monthKey ASC
        """,
    )
    fun observeMonthlyTrend(startDate: String, endDate: String): Flow<List<MonthlyTrendRow>>

    @Query(
        """
        SELECT
            c.id AS categoryId,
            c.name AS categoryName,
            c.emoji AS categoryEmoji,
            c.colorHex AS categoryColorHex,
            COALESCE(SUM(t.amountMinor), 0) AS spentMinor
        FROM categories c
        LEFT JOIN transactions t
            ON t.categoryId = c.id
            AND t.type = 'EXPENSE'
            AND t.transactionDate BETWEEN :startDate AND :endDate
        WHERE c.type = 'EXPENSE'
        GROUP BY c.id
        ORDER BY spentMinor DESC, c.name COLLATE NOCASE ASC
        """,
    )
    fun observeExpenseSpendByCategory(startDate: String, endDate: String): Flow<List<CategorySpendRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionEntity): Long

    @Update
    suspend fun update(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
