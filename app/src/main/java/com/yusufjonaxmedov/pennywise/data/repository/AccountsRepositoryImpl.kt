package com.yusufjonaxmedov.pennywise.data.repository

import com.yusufjonaxmedov.pennywise.core.common.ClockProvider
import com.yusufjonaxmedov.pennywise.core.model.Account
import com.yusufjonaxmedov.pennywise.core.model.AccountDraft
import com.yusufjonaxmedov.pennywise.data.database.dao.AccountDao
import com.yusufjonaxmedov.pennywise.data.database.entity.AccountEntity
import com.yusufjonaxmedov.pennywise.domain.repository.AccountsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountsRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val clockProvider: ClockProvider,
) : AccountsRepository {
    override fun observeAccounts(): Flow<List<Account>> =
        accountDao.observeAccountBalances().map { rows -> rows.map { it.toModel() } }

    override suspend fun getAccount(accountId: Long): Account? =
        observeAccounts().first().firstOrNull { it.id == accountId }

    override suspend fun upsertAccount(draft: AccountDraft): Long {
        val existing = if (draft.id != null) accountDao.getById(draft.id) else null
        val entity = AccountEntity(
            id = draft.id ?: 0,
            name = draft.name.trim(),
            type = draft.type,
            initialBalanceMinor = draft.initialBalanceMinor,
            archived = draft.archived,
            createdAt = existing?.createdAt ?: clockProvider.currentInstant(),
        )
        return if (draft.id == null) {
            accountDao.insert(entity)
        } else {
            accountDao.update(entity)
            draft.id
        }
    }

    override suspend fun deleteAccount(accountId: Long) {
        check(accountDao.countTransactions(accountId) == 0) {
            "Archive accounts that have transactions instead of deleting them."
        }
        accountDao.deleteById(accountId)
    }
}
