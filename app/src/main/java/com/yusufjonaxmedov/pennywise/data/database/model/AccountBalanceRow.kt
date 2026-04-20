package com.yusufjonaxmedov.pennywise.data.database.model

import com.yusufjonaxmedov.pennywise.core.model.AccountType

data class AccountBalanceRow(
    val id: Long,
    val name: String,
    val type: AccountType,
    val initialBalanceMinor: Long,
    val archived: Boolean,
    val transactionDeltaMinor: Long,
)
