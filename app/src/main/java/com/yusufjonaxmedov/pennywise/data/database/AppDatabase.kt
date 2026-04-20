package com.yusufjonaxmedov.pennywise.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yusufjonaxmedov.pennywise.data.database.converter.RoomConverters
import com.yusufjonaxmedov.pennywise.data.database.dao.AccountDao
import com.yusufjonaxmedov.pennywise.data.database.dao.BudgetDao
import com.yusufjonaxmedov.pennywise.data.database.dao.CategoryDao
import com.yusufjonaxmedov.pennywise.data.database.dao.RecurringTemplateDao
import com.yusufjonaxmedov.pennywise.data.database.dao.SavedFilterDao
import com.yusufjonaxmedov.pennywise.data.database.dao.TransactionDao
import com.yusufjonaxmedov.pennywise.data.database.entity.AccountEntity
import com.yusufjonaxmedov.pennywise.data.database.entity.BudgetEntity
import com.yusufjonaxmedov.pennywise.data.database.entity.CategoryEntity
import com.yusufjonaxmedov.pennywise.data.database.entity.RecurringTemplateEntity
import com.yusufjonaxmedov.pennywise.data.database.entity.SavedFilterEntity
import com.yusufjonaxmedov.pennywise.data.database.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        BudgetEntity::class,
        CategoryEntity::class,
        RecurringTemplateEntity::class,
        SavedFilterEntity::class,
        TransactionEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recurringTemplateDao(): RecurringTemplateDao
    abstract fun savedFilterDao(): SavedFilterDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "pennywise.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN payee TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE transactions ADD COLUMN tagsJson TEXT NOT NULL DEFAULT '[]'")
                database.execSQL("ALTER TABLE transactions ADD COLUMN origin TEXT NOT NULL DEFAULT 'MANUAL'")
                database.execSQL("ALTER TABLE transactions ADD COLUMN recurringTemplateId INTEGER")
                database.execSQL("ALTER TABLE budgets ADD COLUMN warnThresholdPercent INTEGER NOT NULL DEFAULT 80")
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS saved_filters (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        searchQuery TEXT NOT NULL,
                        categoryIdsJson TEXT NOT NULL,
                        accountIdsJson TEXT NOT NULL,
                        typesJson TEXT NOT NULL,
                        startDate TEXT,
                        endDate TEXT,
                        sortOption TEXT NOT NULL,
                        pinned INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_saved_filters_pinned ON saved_filters(pinned)")
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recurring_templates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amountMinor INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        categoryId INTEGER NOT NULL,
                        accountId INTEGER NOT NULL,
                        note TEXT NOT NULL,
                        payee TEXT NOT NULL,
                        tagsJson TEXT NOT NULL,
                        frequency TEXT NOT NULL,
                        intervalValue INTEGER NOT NULL,
                        dayOfMonth INTEGER,
                        dayOfWeekIso INTEGER,
                        nextOccurrenceDate TEXT NOT NULL,
                        active INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE RESTRICT DEFERRABLE INITIALLY DEFERRED,
                        FOREIGN KEY(accountId) REFERENCES accounts(id) ON DELETE RESTRICT DEFERRABLE INITIALLY DEFERRED
                    )
                    """.trimIndent(),
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_templates_categoryId ON recurring_templates(categoryId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_templates_accountId ON recurring_templates(accountId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_templates_nextOccurrenceDate ON recurring_templates(nextOccurrenceDate)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_recurringTemplateId ON transactions(recurringTemplateId)")
            }
        }
    }
}
