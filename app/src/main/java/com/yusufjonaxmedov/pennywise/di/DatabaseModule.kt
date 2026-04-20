package com.yusufjonaxmedov.pennywise.di

import android.content.Context
import androidx.room.Room
import com.yusufjonaxmedov.pennywise.data.database.AppDatabase
import com.yusufjonaxmedov.pennywise.data.database.dao.AccountDao
import com.yusufjonaxmedov.pennywise.data.database.dao.BudgetDao
import com.yusufjonaxmedov.pennywise.data.database.dao.CategoryDao
import com.yusufjonaxmedov.pennywise.data.database.dao.RecurringTemplateDao
import com.yusufjonaxmedov.pennywise.data.database.dao.SavedFilterDao
import com.yusufjonaxmedov.pennywise.data.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideAccountDao(database: AppDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideBudgetDao(database: AppDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideRecurringTemplateDao(database: AppDatabase): RecurringTemplateDao = database.recurringTemplateDao()

    @Provides
    fun provideSavedFilterDao(database: AppDatabase): SavedFilterDao = database.savedFilterDao()

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao = database.transactionDao()
}
