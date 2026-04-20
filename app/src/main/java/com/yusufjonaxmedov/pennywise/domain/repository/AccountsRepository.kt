package com.yusufjonaxmedov.pennywise.domain.repository

import com.yusufjonaxmedov.pennywise.core.model.Account
import com.yusufjonaxmedov.pennywise.core.model.AccountDraft
import kotlinx.coroutines.flow.Flow

interface AccountsRepository {
    fun observeAccounts(): Flow<List<Account>>
    suspend fun getAccount(accountId: Long): Account?
    suspend fun upsertAccount(draft: AccountDraft): Long
    suspend fun deleteAccount(accountId: Long)
}
