package com.example.pbd.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pbd.data.local.AppDatabase
import com.example.pbd.data.model.NotificationEntity
import com.example.pbd.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

/**
 * BroadcastReceiver fired by AlarmManager every day at the user's configured time (default 10 PM).
 *
 * Queries Room for today's total spending and fires a "Daily Spending Summary" notification.
 * Also persists the notification to the in-app Notification Center.
 */
class DailyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = NotificationPreferences(context)
        if (!prefs.notificationsEnabled) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val dao = db.transactionDao()

                // Calculate midnight of today (start of day)
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfDay = cal.timeInMillis

                val allTx = dao.getAllTransactions().first()
                val todayExpenses = allTx.filter {
                    it.type == TransactionType.EXPENSE && it.timestamp >= startOfDay
                }

                val totalSpent = todayExpenses.sumOf { it.baseAmountLKR }
                val txCount = todayExpenses.size

                if (txCount > 0) {
                    NotificationHelper.notifyDailySummary(context, totalSpent, txCount)

                    // Persist to Notification Center
                    db.notificationDao().insert(
                        NotificationEntity(
                            id = UUID.randomUUID().toString(),
                            title = "📊 Today's Spending Summary",
                            message = "You made $txCount transaction${if (txCount == 1) "" else "s"} totalling LKR %,.0f today.".format(totalSpent),
                            type = "DAILY_SUMMARY"
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Re-schedule for the same time tomorrow
            scheduleNextAlarm(context)
        }
    }

    companion object {
        const val ACTION_DAILY_REMINDER = "com.example.pbd.ACTION_DAILY_REMINDER"

        /**
         * Schedule (or re-schedule) the daily alarm for the user's preferred time.
         * Call this from Application.onCreate and from BootReceiver.
         */
        fun scheduleNextAlarm(context: Context) {
            val prefs = NotificationPreferences(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, prefs.dailySummaryHour)
                set(Calendar.MINUTE, prefs.dailySummaryMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // If we're already past that time today, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val intent = Intent(context, DailyReminderReceiver::class.java).apply {
                action = ACTION_DAILY_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Use setExactAndAllowWhileIdle for reliable delivery in Doze mode
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                // SCHEDULE_EXACT_ALARM not granted on Android 12+; fall back to inexact
                alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            }
        }
    }
}
