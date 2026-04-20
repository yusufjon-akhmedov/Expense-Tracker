package com.yusufjonaxmedov.pennywise.core.model

import java.time.LocalDate

data class RecurringTemplate(
    val id: Long,
    val name: String,
    val amountMinor: Long,
    val type: TransactionType,
    val category: Category,
    val accountId: Long,
    val accountName: String,
    val accountType: AccountType,
    val note: String,
    val payee: String,
    val tags: List<String>,
    val frequency: RecurringFrequency,
    val intervalValue: Int,
    val dayOfMonth: Int?,
    val dayOfWeekIso: Int?,
    val nextOccurrenceDate: LocalDate,
    val active: Boolean,
)
