package com.yusufjonaxmedov.pennywise.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yusufjonaxmedov.pennywise.data.database.entity.AccountEntity
import com.yusufjonaxmedov.pennywise.data.database.model.AccountBalanceRow
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query(
        """
        SELECT 
            a.id AS id,
            a.name AS name,
            a.type AS type,
            a.initialBalanceMinor AS initialBalanceMinor,
            a.archived AS archived,
            COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amountMinor ELSE -t.amountMinor END), 0) AS transactionDeltaMinor
        FROM accounts a
        LEFT JOIN transactions t ON t.accountId = a.id
        GROUP BY a.id
        ORDER BY a.archived ASC, a.name COLLATE NOCASE ASC
        """,
    )
    fun observeAccountBalances(): Flow<List<AccountBalanceRow>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AccountEntity): Long

    @Update
    suspend fun update(entity: AccountEntity)

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun countTransactions(accountId: Long): Int

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
