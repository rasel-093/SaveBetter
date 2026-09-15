package com.example.savebetter.core.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

/**
 * Utility for scheduling automated weekly and monthly budget setup reminders using WorkManager.
 */
object BudgetReminderScheduler {

    /**
     * Schedules a one-time reminder targeting the first day of the week (Monday at 09:00 AM).
     * If currently before 09:00 AM on Monday, targets today at 09:00 AM.
     * Otherwise targets next Monday at 09:00 AM.
     */
    fun scheduleWeeklyBudgetReminder(context: Context) {
        val now = LocalDateTime.now()
        var targetDateTime = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .withHour(9)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        if (now.isAfter(targetDateTime) || now.isEqual(targetDateTime)) {
            targetDateTime = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(9)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
        }

        val initialDelaySeconds = Duration.between(now, targetDateTime).seconds.coerceAtLeast(60L)

        val workRequest = OneTimeWorkRequestBuilder<WeeklyBudgetReminderWorker>()
            .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
            .addTag(WeeklyBudgetReminderWorker.WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WeeklyBudgetReminderWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Schedules a one-time reminder targeting the first day of the month (1st at 09:00 AM).
     * If currently before 09:00 AM on the 1st of the current month, targets today at 09:00 AM.
     * Otherwise targets the 1st of next month at 09:00 AM.
     */
    fun scheduleMonthlyBudgetReminder(context: Context) {
        val now = LocalDateTime.now()
        var targetDateTime = now.withDayOfMonth(1)
            .withHour(9)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        if (now.isAfter(targetDateTime) || now.isEqual(targetDateTime)) {
            val nextYearMonth = YearMonth.of(now.year, now.month).plusMonths(1)
            targetDateTime = nextYearMonth.atDay(1)
                .atTime(9, 0, 0, 0)
        }

        val initialDelaySeconds = Duration.between(now, targetDateTime).seconds.coerceAtLeast(60L)

        val workRequest = OneTimeWorkRequestBuilder<MonthlyBudgetReminderWorker>()
            .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
            .addTag(MonthlyBudgetReminderWorker.WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            MonthlyBudgetReminderWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Cancels any pending weekly budget reminder work.
     */
    fun cancelWeeklyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WeeklyBudgetReminderWorker.WORK_NAME)
    }

    /**
     * Cancels any pending monthly budget reminder work.
     */
    fun cancelMonthlyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(MonthlyBudgetReminderWorker.WORK_NAME)
    }
}
