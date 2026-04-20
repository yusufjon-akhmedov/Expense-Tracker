package com.yusufjonaxmedov.pennywise.core.model

data class Budget(
    val id: Long,
    val monthKey: String,
    val category: Category?,
    val limitMinor: Long,
    val spentMinor: Long,
    val remainingMinor: Long,
    val progress: Float,
    val rolloverEnabled: Boolean,
    val warnThresholdPercent: Int,
) {
    val status: BudgetStatus
        get() = when {
            spentMinor > limitMinor -> BudgetStatus.OVER_LIMIT
            limitMinor > 0 && progress >= warnThresholdPercent / 100f -> BudgetStatus.NEARING_LIMIT
            else -> BudgetStatus.ON_TRACK
        }
}

enum class BudgetStatus {
    ON_TRACK,
    NEARING_LIMIT,
    OVER_LIMIT,
}
