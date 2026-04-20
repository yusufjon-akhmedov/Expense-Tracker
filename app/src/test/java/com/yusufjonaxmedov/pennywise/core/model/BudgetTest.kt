package com.yusufjonaxmedov.pennywise.core.model

import com.yusufjonaxmedov.pennywise.data.repository.buildBudget
import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetTest {
    @Test
    fun `buildBudget reports on track when below threshold`() {
        val budget = buildBudget(
            id = 1,
            monthKey = "2026-04",
            category = null,
            limitMinor = 10_000L,
            spentMinor = 4_000L,
            rolloverEnabled = false,
            warnThresholdPercent = 80,
        )

        assertEquals(BudgetStatus.ON_TRACK, budget.status)
        assertEquals(6_000L, budget.remainingMinor)
    }

    @Test
    fun `buildBudget reports over limit when spent exceeds cap`() {
        val budget = buildBudget(
            id = 1,
            monthKey = "2026-04",
            category = null,
            limitMinor = 10_000L,
            spentMinor = 12_500L,
            rolloverEnabled = false,
            warnThresholdPercent = 80,
        )

        assertEquals(BudgetStatus.OVER_LIMIT, budget.status)
    }
}
