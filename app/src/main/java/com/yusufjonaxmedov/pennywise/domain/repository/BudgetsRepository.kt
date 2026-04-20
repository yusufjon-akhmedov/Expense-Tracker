package com.yusufjonaxmedov.pennywise.domain.repository

import com.yusufjonaxmedov.pennywise.core.model.Budget
import com.yusufjonaxmedov.pennywise.core.model.BudgetDraft
import kotlinx.coroutines.flow.Flow

interface BudgetsRepository {
    fun observeBudgets(monthKey: String): Flow<List<Budget>>
    suspend fun getBudget(budgetId: Long): Budget?
    suspend fun upsertBudget(draft: BudgetDraft): Long
    suspend fun deleteBudget(budgetId: Long)
}
