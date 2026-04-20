package com.yusufjonaxmedov.pennywise.core.model

import java.time.LocalDate

data class CategoryDraft(
    val id: Long? = null,
    val name: String,
    val emoji: String,
    val colorHex: String,
    val type: TransactionType,
)

data class AccountDraft(
    val id: Long? = null,
    val name: String,
    val type: AccountType,
    val initialBalanceMinor: Long,
    val archived: Boolean = false,
)

data class BudgetDraft(
    val id: Long? = null,
    val monthKey: String,
    val categoryId: Long = 0,
    val limitMinor: Long,
    val rolloverEnabled: Boolean = false,
    val warnThresholdPercent: Int = 80,
)

data class RecurringTemplateDraft(
    val id: Long? = null,
    val name: String,
    val amountMinor: Long,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val note: String,
    val payee: String,
    val tags: List<String>,
    val frequency: RecurringFrequency,
    val intervalValue: Int,
    val dayOfMonth: Int? = null,
    val dayOfWeekIso: Int? = null,
    val nextOccurrenceDate: LocalDate,
    val active: Boolean = true,
)
