package com.yusufjonaxmedov.pennywise.data.repository

import androidx.room.withTransaction
import com.yusufjonaxmedov.pennywise.core.common.ClockProvider
import com.yusufjonaxmedov.pennywise.core.model.RecurringFrequency
import com.yusufjonaxmedov.pennywise.core.model.RecurringTemplate
import com.yusufjonaxmedov.pennywise.core.model.RecurringTemplateDraft
import com.yusufjonaxmedov.pennywise.core.model.TransactionOrigin
import com.yusufjonaxmedov.pennywise.data.database.AppDatabase
import com.yusufjonaxmedov.pennywise.data.database.dao.AccountDao
import com.yusufjonaxmedov.pennywise.data.database.dao.CategoryDao
import com.yusufjonaxmedov.pennywise.data.database.dao.RecurringTemplateDao
import com.yusufjonaxmedov.pennywise.data.database.dao.TransactionDao
import com.yusufjonaxmedov.pennywise.data.database.entity.RecurringTemplateEntity
import com.yusufjonaxmedov.pennywise.data.database.entity.TransactionEntity as TransactionDbEntity
import com.yusufjonaxmedov.pennywise.domain.repository.RecurringRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val clockProvider: ClockProvider,
    private val database: AppDatabase,
    private val json: Json,
    private val recurringTemplateDao: RecurringTemplateDao,
    private val transactionDao: TransactionDao,
    ) : RecurringRepository {
    override fun observeTemplates(): Flow<List<RecurringTemplate>> = combine(
        recurringTemplateDao.observeAll(),
        categoryDao.observeAll(),
        accountDao.observeAccountBalances(),
    ) { templates, categories, accounts ->
        val categoryMap = categories.associateBy { it.id }
        val accountMap = accounts.associateBy { it.id }
        templates.mapNotNull { template ->
            val category = categoryMap[template.categoryId]?.toModel() ?: return@mapNotNull null
            val account = accountMap[template.accountId]?.toModel() ?: return@mapNotNull null
            template.toModel(category, account, json)
        }
    }

    override fun observeDueCount(): Flow<Int> = recurringTemplateDao.observeDueTemplateCount(clockProvider.currentDate())

    override suspend fun getTemplate(templateId: Long): RecurringTemplate? =
        observeTemplates().first().firstOrNull { it.id == templateId }

    override suspend fun upsertTemplate(draft: RecurringTemplateDraft): Long {
        val existing = if (draft.id != null) recurringTemplateDao.getById(draft.id) else null
        val entity = RecurringTemplateEntity(
            id = draft.id ?: 0,
            name = draft.name.trim(),
            amountMinor = draft.amountMinor,
            type = draft.type,
            categoryId = draft.categoryId,
            accountId = draft.accountId,
            note = draft.note.trim(),
            payee = draft.payee.trim(),
            tagsJson = json.encodeStringList(draft.tags),
            frequency = draft.frequency,
            intervalValue = draft.intervalValue,
            dayOfMonth = draft.dayOfMonth,
            dayOfWeekIso = draft.dayOfWeekIso,
            nextOccurrenceDate = draft.nextOccurrenceDate,
            active = draft.active,
            createdAt = existing?.createdAt ?: clockProvider.currentInstant(),
            updatedAt = clockProvider.currentInstant(),
        )
        return if (draft.id == null) {
            recurringTemplateDao.insert(entity)
        } else {
            recurringTemplateDao.update(entity)
            draft.id
        }
    }

    override suspend fun deleteTemplate(templateId: Long) {
        recurringTemplateDao.deleteById(templateId)
    }

    override suspend fun applyDueTemplates(): Int = database.withTransaction {
        val today = clockProvider.currentDate()
        val dueTemplates = recurringTemplateDao.getDueTemplates(today)
        var createdCount = 0

        dueTemplates.forEach { template ->
            var nextDate = template.nextOccurrenceDate
            while (!nextDate.isAfter(today) && template.active) {
                transactionDao.insert(
                    TransactionDbEntity(
                        amountMinor = template.amountMinor,
                        type = template.type,
                        categoryId = template.categoryId,
                        accountId = template.accountId,
                        note = template.note,
                        payee = template.payee,
                        tagsJson = template.tagsJson,
                        transactionDate = nextDate,
                        origin = TransactionOrigin.RECURRING,
                        recurringTemplateId = template.id,
                        createdAt = clockProvider.currentInstant(),
                        updatedAt = clockProvider.currentInstant(),
                    ),
                )
                createdCount += 1
                nextDate = advanceTemplateDate(template, nextDate)
            }

            recurringTemplateDao.update(
                template.copy(
                    nextOccurrenceDate = nextDate,
                    updatedAt = clockProvider.currentInstant(),
                ),
            )
        }

        createdCount
    }

    private fun advanceTemplateDate(
        template: RecurringTemplateEntity,
        currentDate: LocalDate,
    ): LocalDate = when (template.frequency) {
        RecurringFrequency.DAILY -> currentDate.plusDays(template.intervalValue.toLong())
        RecurringFrequency.WEEKLY -> currentDate.plusWeeks(template.intervalValue.toLong())
        RecurringFrequency.MONTHLY -> {
            val targetMonth = currentDate.plusMonths(template.intervalValue.toLong())
            val day = template.dayOfMonth ?: currentDate.dayOfMonth
            targetMonth.withDayOfMonth(day.coerceAtMost(targetMonth.lengthOfMonth()))
        }
    }
}
