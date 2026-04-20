package com.yusufjonaxmedov.pennywise.core.model

import java.time.LocalDate

data class TransactionDraft(
    val id: Long? = null,
    val amountMinor: Long,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val note: String,
    val payee: String,
    val tags: List<String>,
    val transactionDate: LocalDate,
    val origin: TransactionOrigin = TransactionOrigin.MANUAL,
    val recurringTemplateId: Long? = null,
)
