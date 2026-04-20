package com.yusufjonaxmedov.pennywise.domain.repository

import com.yusufjonaxmedov.pennywise.core.model.Category
import com.yusufjonaxmedov.pennywise.core.model.CategoryDraft
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoriesRepository {
    fun observeCategories(type: TransactionType? = null): Flow<List<Category>>
    suspend fun getCategory(categoryId: Long): Category?
    suspend fun upsertCategory(draft: CategoryDraft): Long
    suspend fun deleteCategory(categoryId: Long, replacementCategoryId: Long? = null)
}
