package com.yusufjonaxmedov.pennywise.data.database.model

import com.yusufjonaxmedov.pennywise.core.model.AccountType
import com.yusufjonaxmedov.pennywise.core.model.TransactionOrigin
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import java.time.Instant
import java.time.LocalDate

data class TransactionJoinedRow(
    val id: Long,
    val amountMinor: Long,
    val type: TransactionType,
    val note: String,
    val payee: String,
    val tagsJson: String,
    val transactionDate: LocalDate,
    val origin: TransactionOrigin,
    val recurringTemplateId: Long?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val categoryId: Long,
    val categoryName: String,
    val categoryEmoji: String,
    val categoryColorHex: String,
    val accountId: Long,
    val accountName: String,
    val accountType: AccountType,
)
