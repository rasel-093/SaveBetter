package com.example.savebetter.core.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.concurrent.TimeUnit

/**
 * Utility for scheduling local month-end reconciliation reminders using WorkManager.
 */
object ReconciliationReminderScheduler {

    /**
     * Schedules a one-time reminder targeting the 27th of the month at 20:00 (8:00 PM).
     * If already past the 27th of the current month, targets the 27th of next month.
     */
    fun scheduleMonthEndReminder(context: Context) {
        val now = LocalDateTime.now()
        val currentYearMonth = YearMonth.of(now.year, now.month)
        val targetDay = 27.coerceAtMost(currentYearMonth.lengthOfMonth())

        var targetDateTime = currentYearMonth.atDay(targetDay).atTime(20, 0)
        if (now.isAfter(targetDateTime)) {
            val nextYearMonth = currentYearMonth.plusMonths(1)
            val nextTargetDay = 27.coerceAtMost(nextYearMonth.lengthOfMonth())
            targetDateTime = nextYearMonth.atDay(nextTargetDay).atTime(20, 0)
        }

        val initialDelaySeconds = Duration.between(now, targetDateTime).seconds.coerceAtLeast(60L)

        val workRequest = OneTimeWorkRequestBuilder<ReconciliationReminderWorker>()
            .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
            .addTag(ReconciliationReminderWorker.WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ReconciliationReminderWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Cancels any pending reconciliation reminder work.
     */
    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(ReconciliationReminderWorker.WORK_NAME)
    }
}
