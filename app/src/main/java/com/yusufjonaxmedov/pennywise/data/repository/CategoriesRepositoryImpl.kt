package com.yusufjonaxmedov.pennywise.data.repository

import androidx.room.withTransaction
import com.yusufjonaxmedov.pennywise.core.common.ClockProvider
import com.yusufjonaxmedov.pennywise.core.model.Category
import com.yusufjonaxmedov.pennywise.core.model.CategoryDraft
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.data.database.AppDatabase
import com.yusufjonaxmedov.pennywise.data.database.dao.CategoryDao
import com.yusufjonaxmedov.pennywise.data.database.entity.CategoryEntity
import com.yusufjonaxmedov.pennywise.domain.repository.CategoriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoriesRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val categoryDao: CategoryDao,
    private val clockProvider: ClockProvider,
) : CategoriesRepository {
    override fun observeCategories(type: TransactionType?): Flow<List<Category>> =
        (type?.let(categoryDao::observeByType) ?: categoryDao.observeAll())
            .map { items -> items.map(CategoryEntity::toModel) }

    override suspend fun getCategory(categoryId: Long): Category? = categoryDao.getById(categoryId)?.toModel()

    override suspend fun upsertCategory(draft: CategoryDraft): Long {
        val existing = if (draft.id != null) categoryDao.getById(draft.id) else null
        val entity = CategoryEntity(
            id = draft.id ?: 0,
            name = draft.name.trim(),
            emoji = draft.emoji.trim(),
            colorHex = draft.colorHex,
            type = draft.type,
            isDefault = existing?.isDefault ?: false,
            createdAt = existing?.createdAt ?: clockProvider.currentInstant(),
        )
        return if (draft.id == null) {
            categoryDao.insert(entity)
        } else {
            categoryDao.update(entity)
            draft.id
        }
    }

    override suspend fun deleteCategory(categoryId: Long, replacementCategoryId: Long?) {
        database.withTransaction {
            val usageCount = categoryDao.countTransactions(categoryId)
            if (usageCount > 0 && replacementCategoryId == null) {
                throw IllegalStateException("Choose a replacement category before deleting one that is in use.")
            }
            if (usageCount > 0 && replacementCategoryId != null) {
                categoryDao.reassignTransactions(categoryId, replacementCategoryId)
            }
            categoryDao.deleteById(categoryId)
        }
    }
}
