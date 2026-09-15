package com.example.savebetter.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.savebetter.MainActivity
import com.example.savebetter.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * WorkManager worker that triggers on the first day of the month (1st)
 * to remind users to review and set their monthly budget and savings goal.
 */
@HiltWorker
class MonthlyBudgetReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            showMonthlyBudgetNotification()
            // Reschedule next month's reminder
            BudgetReminderScheduler.scheduleMonthlyBudgetReminder(context)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showMonthlyBudgetNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val channelId = "budget_reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(R.string.budget_reminder_channel_name)
            val channelDesc = context.getString(R.string.budget_reminder_channel_desc)
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = channelDesc
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "monthly")
            putExtra("open_budget_dialog", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val currentMonth = YearMonth.now()
        val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val title = context.getString(R.string.monthly_reminder_notification_title)
        val content = context.getString(R.string.monthly_reminder_notification_desc, monthName)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 2003
        const val WORK_NAME = "monthly_budget_setup_reminder"
    }
}
