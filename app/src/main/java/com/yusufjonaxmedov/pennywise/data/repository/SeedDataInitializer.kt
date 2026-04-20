package com.yusufjonaxmedov.pennywise.data.repository

import com.yusufjonaxmedov.pennywise.core.common.ClockProvider
import com.yusufjonaxmedov.pennywise.core.model.AccountType
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.data.database.dao.AccountDao
import com.yusufjonaxmedov.pennywise.data.database.dao.CategoryDao
import com.yusufjonaxmedov.pennywise.data.database.entity.AccountEntity
import com.yusufjonaxmedov.pennywise.data.database.entity.CategoryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedDataInitializer @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val clockProvider: ClockProvider,
) {
    suspend fun seedIfNeeded() {
        if (categoryDao.countAll() == 0) {
            defaultCategories().forEach { categoryDao.insert(it) }
        }
        if (accountDao.countAll() == 0) {
            defaultAccounts().forEach { accountDao.insert(it) }
        }
    }

    private fun defaultAccounts(): List<AccountEntity> = listOf(
        AccountEntity(name = "Cash", type = AccountType.CASH, initialBalanceMinor = 0, createdAt = clockProvider.currentInstant()),
        AccountEntity(name = "Main Card", type = AccountType.CARD, initialBalanceMinor = 0, createdAt = clockProvider.currentInstant()),
        AccountEntity(name = "Bank Account", type = AccountType.BANK, initialBalanceMinor = 0, createdAt = clockProvider.currentInstant()),
        AccountEntity(name = "Savings", type = AccountType.SAVINGS, initialBalanceMinor = 0, createdAt = clockProvider.currentInstant()),
    )

    private fun defaultCategories(): List<CategoryEntity> = listOf(
        CategoryEntity(name = "Groceries", emoji = "🛒", colorHex = "#2F855A", type = TransactionType.EXPENSE, isDefault = true, createdAt = clockProvider.currentInstant()),
        CategoryEntity(name = "Transport", emoji = "🚕", colorHex = "#1A73E8", type = TransactionType.EXPENSE, isDefault = true, createdAt = clockProvider.currentInstant()),
        CategoryEntity(name = "Dining", emoji = "🍽️", colorHex = "#D97706", type = TransactionType.EXPENSE, isDefault = true, createdAt = clockProvider.currentInstant()),
        CategoryEntity(name = "Bills", emoji = "💡", colorHex = "#7C3AED", type = TransactionType.EXPENSE, isDefault = true, createdAt = clockProvider.currentInstant()),
        CategoryEntity(name = "Shopping", emoji = "🛍️", colorHex = "#E11D48", type = TransactionType.EXPENSE, isDefault = true, createdAt = clockProvider.currentInstant()),
        CategoryEntity(name = "Health", emoji = "💊", colorHex = "#0891B2", type = TransactionType.EXPENSE, isDefault = true, createdAt = clockProvider.currentInstant()),
        CategoryEntity(name = "Salary", emoji = "💼", colorHex = "#15803D", type = TransactionType.INCOME, isDefault = true, createdAt = clockProvider.currentInstant()),
        CategoryEntity(name = "Freelance", emoji = "🧑‍💻", colorHex = "#2563EB", type = TransactionType.INCOME, isDefault = true, createdAt = clockProvider.currentInstant()),
        CategoryEntity(name = "Investment", emoji = "📈", colorHex = "#7C2D12", type = TransactionType.INCOME, isDefault = true, createdAt = clockProvider.currentInstant()),
        CategoryEntity(name = "Gift", emoji = "🎁", colorHex = "#9333EA", type = TransactionType.INCOME, isDefault = true, createdAt = clockProvider.currentInstant()),
    )
}
