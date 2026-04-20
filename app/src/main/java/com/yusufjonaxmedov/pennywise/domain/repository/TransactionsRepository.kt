package com.yusufjonaxmedov.pennywise.domain.repository

import com.yusufjonaxmedov.pennywise.core.model.DashboardSummary
import com.yusufjonaxmedov.pennywise.core.model.SavedFilter
import com.yusufjonaxmedov.pennywise.core.model.Transaction
import com.yusufjonaxmedov.pennywise.core.model.TransactionDraft
import com.yusufjonaxmedov.pennywise.core.model.TransactionFilter
import kotlinx.coroutines.flow.Flow

interface TransactionsRepository {
    fun observeDashboard(monthKey: String): Flow<DashboardSummary>
    fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>>
    fun observeSavedFilters(): Flow<List<SavedFilter>>
    suspend fun getTransaction(transactionId: Long): Transaction?
    suspend fun upsertTransaction(draft: TransactionDraft): Long
    suspend fun duplicateTransaction(transactionId: Long): Long
    suspend fun deleteTransaction(transactionId: Long)
    suspend fun saveFilter(name: String, filter: TransactionFilter, pinned: Boolean = true): Long
    suspend fun deleteSavedFilter(savedFilterId: Long)
    suspend fun exportTransactions(filter: TransactionFilter = TransactionFilter()): List<Transaction>
}
