package com.yusufjonaxmedov.pennywise.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yusufjonaxmedov.pennywise.core.model.AccountType
import com.yusufjonaxmedov.pennywise.core.model.TransactionOrigin
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.data.database.entity.AccountEntity
import com.yusufjonaxmedov.pennywise.data.database.entity.CategoryEntity
import com.yusufjonaxmedov.pennywise.data.database.entity.TransactionEntity
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun accountBalanceIncludesIncomeAndExpenseTransactions() = runBlocking {
        val categoryId = database.categoryDao().insert(
            CategoryEntity(
                name = "Coffee",
                emoji = "☕",
                colorHex = "#155EEF",
                type = TransactionType.EXPENSE,
                createdAt = Instant.now(),
            ),
        )
        val incomeCategoryId = database.categoryDao().insert(
            CategoryEntity(
                name = "Salary",
                emoji = "💼",
                colorHex = "#117A65",
                type = TransactionType.INCOME,
                createdAt = Instant.now(),
            ),
        )
        val accountId = database.accountDao().insert(
            AccountEntity(
                name = "Cash",
                type = AccountType.CASH,
                initialBalanceMinor = 1_000L,
                createdAt = Instant.now(),
            ),
        )

        database.transactionDao().insert(
            TransactionEntity(
                amountMinor = 300L,
                type = TransactionType.EXPENSE,
                categoryId = categoryId,
                accountId = accountId,
                note = "Coffee",
                payee = "",
                tagsJson = "[]",
                transactionDate = LocalDate.of(2026, 4, 20),
                origin = TransactionOrigin.MANUAL,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            ),
        )
        database.transactionDao().insert(
            TransactionEntity(
                amountMinor = 2_000L,
                type = TransactionType.INCOME,
                categoryId = incomeCategoryId,
                accountId = accountId,
                note = "Salary",
                payee = "",
                tagsJson = "[]",
                transactionDate = LocalDate.of(2026, 4, 20),
                origin = TransactionOrigin.MANUAL,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            ),
        )

        val balance = database.accountDao().observeAccountBalances().first().first()
        assertEquals(2_700L, balance.initialBalanceMinor + balance.transactionDeltaMinor)
    }
}
