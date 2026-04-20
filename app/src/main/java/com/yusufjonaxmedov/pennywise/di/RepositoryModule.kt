package com.yusufjonaxmedov.pennywise.di

import com.yusufjonaxmedov.pennywise.data.repository.AccountsRepositoryImpl
import com.yusufjonaxmedov.pennywise.data.repository.BudgetsRepositoryImpl
import com.yusufjonaxmedov.pennywise.data.repository.CategoriesRepositoryImpl
import com.yusufjonaxmedov.pennywise.data.repository.PreferencesRepositoryImpl
import com.yusufjonaxmedov.pennywise.data.repository.RecurringRepositoryImpl
import com.yusufjonaxmedov.pennywise.data.repository.ReportsRepositoryImpl
import com.yusufjonaxmedov.pennywise.data.repository.TransactionsRepositoryImpl
import com.yusufjonaxmedov.pennywise.domain.repository.AccountsRepository
import com.yusufjonaxmedov.pennywise.domain.repository.BudgetsRepository
import com.yusufjonaxmedov.pennywise.domain.repository.CategoriesRepository
import com.yusufjonaxmedov.pennywise.domain.repository.PreferencesRepository
import com.yusufjonaxmedov.pennywise.domain.repository.RecurringRepository
import com.yusufjonaxmedov.pennywise.domain.repository.ReportsRepository
import com.yusufjonaxmedov.pennywise.domain.repository.TransactionsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAccountsRepository(impl: AccountsRepositoryImpl): AccountsRepository

    @Binds
    @Singleton
    abstract fun bindBudgetsRepository(impl: BudgetsRepositoryImpl): BudgetsRepository

    @Binds
    @Singleton
    abstract fun bindCategoriesRepository(impl: CategoriesRepositoryImpl): CategoriesRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindRecurringRepository(impl: RecurringRepositoryImpl): RecurringRepository

    @Binds
    @Singleton
    abstract fun bindReportsRepository(impl: ReportsRepositoryImpl): ReportsRepository

    @Binds
    @Singleton
    abstract fun bindTransactionsRepository(impl: TransactionsRepositoryImpl): TransactionsRepository
}
