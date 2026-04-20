package com.yusufjonaxmedov.pennywise.domain.repository

import com.yusufjonaxmedov.pennywise.core.model.RecurringTemplate
import com.yusufjonaxmedov.pennywise.core.model.RecurringTemplateDraft
import kotlinx.coroutines.flow.Flow

interface RecurringRepository {
    fun observeTemplates(): Flow<List<RecurringTemplate>>
    fun observeDueCount(): Flow<Int>
    suspend fun getTemplate(templateId: Long): RecurringTemplate?
    suspend fun upsertTemplate(draft: RecurringTemplateDraft): Long
    suspend fun deleteTemplate(templateId: Long)
    suspend fun applyDueTemplates(): Int
}
