package com.yusufjonaxmedov.pennywise.core.model

data class Account(
    val id: Long,
    val name: String,
    val type: AccountType,
    val initialBalanceMinor: Long,
    val currentBalanceMinor: Long,
    val archived: Boolean,
)
