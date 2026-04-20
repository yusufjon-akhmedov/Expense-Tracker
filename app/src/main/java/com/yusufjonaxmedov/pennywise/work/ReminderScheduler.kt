package com.yusufjonaxmedov.pennywise.work

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    private val workManager: WorkManager,
) {
    fun schedule(hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        val nextRun = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0).let {
            if (it.isAfter(now)) it else it.plusDays(1)
        }
        val initialDelay = Duration.between(now, nextRun)
        val request = PeriodicWorkRequestBuilder<ExpenseReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay)
            .addTag(WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    private companion object {
        const val WORK_NAME = "daily_expense_reminder"
    }
}
