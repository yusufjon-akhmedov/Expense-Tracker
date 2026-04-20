package com.yusufjonaxmedov.pennywise.data.repository

import com.yusufjonaxmedov.pennywise.core.model.Account
import com.yusufjonaxmedov.pennywise.core.model.Budget
import com.yusufjonaxmedov.pennywise.core.model.Category
import com.yusufjonaxmedov.pennywise.core.model.CategorySpend
import com.yusufjonaxmedov.pennywise.core.model.MonthlyTrendPoint
import com.yusufjonaxmedov.pennywise.core.model.RecurringTemplate
import com.yusufjonaxmedov.pennywise.core.model.SavedFilter
import com.yusufjonaxmedov.pennywise.core.model.Transaction
import com.yusufjonaxmedov.pennywise.core.model.TransactionFilter
import com.yusufjonaxmedov.pennywise.data.database.entity.AccountEntity
import com.yusufjonaxmedov.pennywise.data.database.entity.CategoryEntity
import com.yusufjonaxmedov.pennywise.data.database.entity.RecurringTemplateEntity
import com.yusufjonaxmedov.pennywise.data.database.entity.SavedFilterEntity
import com.yusufjonaxmedov.pennywise.data.database.model.AccountBalanceRow
import com.yusufjonaxmedov.pennywise.data.database.model.CategorySpendRow
import com.yusufjonaxmedov.pennywise.data.database.model.MonthlyTrendRow
import com.yusufjonaxmedov.pennywise.data.database.model.TransactionJoinedRow
import kotlinx.serialization.json.Json

internal fun CategoryEntity.toModel(): Category = Category(
    id = id,
    name = name,
    emoji = emoji,
    colorHex = colorHex,
    type = type,
    isDefault = isDefault,
)

internal fun AccountEntity.toModel(): Account = Account(
    id = id,
    name = name,
    type = type,
    initialBalanceMinor = initialBalanceMinor,
    currentBalanceMinor = initialBalanceMinor,
    archived = archived,
)

internal fun AccountBalanceRow.toModel(): Account = Account(
    id = id,
    name = name,
    type = type,
    initialBalanceMinor = initialBalanceMinor,
    currentBalanceMinor = initialBalanceMinor + transactionDeltaMinor,
    archived = archived,
)

internal fun TransactionJoinedRow.toModel(json: Json): Transaction = Transaction(
    id = id,
    amountMinor = amountMinor,
    type = type,
    category = Category(
        id = categoryId,
        name = categoryName,
        emoji = categoryEmoji,
        colorHex = categoryColorHex,
        type = type,
        isDefault = false,
    ),
    accountId = accountId,
    accountName = accountName,
    accountType = accountType,
    note = note,
    payee = payee,
    tags = json.decodeStringList(tagsJson),
    transactionDate = transactionDate,
    origin = origin,
    recurringTemplateId = recurringTemplateId,
)

internal fun CategorySpendRow.toModel(): CategorySpend = CategorySpend(
    category = Category(
        id = categoryId,
        name = categoryName,
        emoji = categoryEmoji,
        colorHex = categoryColorHex,
        type = com.yusufjonaxmedov.pennywise.core.model.TransactionType.EXPENSE,
        isDefault = false,
    ),
    amountMinor = spentMinor,
)

internal fun MonthlyTrendRow.toModel(): MonthlyTrendPoint = MonthlyTrendPoint(
    monthKey = monthKey,
    incomeMinor = incomeMinor,
    expenseMinor = expenseMinor,
)

internal fun SavedFilterEntity.toModel(json: Json): SavedFilter = SavedFilter(
    id = id,
    name = name,
    filter = TransactionFilter(
        searchQuery = searchQuery,
        categoryIds = json.decodeLongSet(categoryIdsJson),
        accountIds = json.decodeLongSet(accountIdsJson),
        types = json.decodeTypeSet(typesJson),
        dateRange = if (startDate != null && endDate != null) {
            com.yusufjonaxmedov.pennywise.core.model.DateRangeSerializable(
                start = startDate.toString(),
                endInclusive = endDate.toString(),
            )
        } else {
            null
        },
        sortOption = sortOption,
    ),
    pinned = pinned,
)

internal fun RecurringTemplateEntity.toModel(
    category: Category,
    account: Account,
    json: Json,
): RecurringTemplate = RecurringTemplate(
    id = id,
    name = name,
    amountMinor = amountMinor,
    type = type,
    category = category,
    accountId = account.id,
    accountName = account.name,
    accountType = account.type,
    note = note,
    payee = payee,
    tags = json.decodeStringList(tagsJson),
    frequency = frequency,
    intervalValue = intervalValue,
    dayOfMonth = dayOfMonth,
    dayOfWeekIso = dayOfWeekIso,
    nextOccurrenceDate = nextOccurrenceDate,
    active = active,
)

internal fun buildBudget(
    id: Long,
    monthKey: String,
    category: Category?,
    limitMinor: Long,
    spentMinor: Long,
    rolloverEnabled: Boolean,
    warnThresholdPercent: Int,
): Budget = Budget(
    id = id,
    monthKey = monthKey,
    category = category,
    limitMinor = limitMinor,
    spentMinor = spentMinor,
    remainingMinor = limitMinor - spentMinor,
    progress = if (limitMinor <= 0) 0f else spentMinor.toFloat() / limitMinor.toFloat(),
    rolloverEnabled = rolloverEnabled,
    warnThresholdPercent = warnThresholdPercent,
)
