package com.yusufjonaxmedov.pennywise.core.model

import java.time.LocalDate

data class Transaction(
    val id: Long,
    val amountMinor: Long,
    val type: TransactionType,
    val category: Category,
    val accountId: Long,
    val accountName: String,
    val accountType: AccountType,
    val note: String,
    val payee: String,
    val tags: List<String>,
    val transactionDate: LocalDate,
    val origin: TransactionOrigin,
    val recurringTemplateId: Long?,
)
