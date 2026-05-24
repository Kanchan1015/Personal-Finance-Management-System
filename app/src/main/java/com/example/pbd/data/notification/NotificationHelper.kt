package com.example.pbd.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.pbd.MainActivity
import com.example.pbd.R

/**
 * Central helper for the entire notification system.
 *
 * Responsibilities:
 * 1. Create all notification channels (call [createChannels] once in Application.onCreate).
 * 2. Expose typed [notify] overloads for each notification category.
 * 3. Handle Android 13+ POST_NOTIFICATIONS permission guard.
 */
object NotificationHelper {

    // ── Channel IDs ────────────────────────────────────────────────────────────
    const val CHANNEL_BUDGET_ALERTS   = "channel_budget_alerts"
    const val CHANNEL_TRANSACTIONS    = "channel_transactions"
    const val CHANNEL_GOALS           = "channel_goals"
    const val CHANNEL_SUMMARIES       = "channel_summaries"

    // ── Notification IDs ───────────────────────────────────────────────────────
    // Fixed IDs collapse identical notifications (e.g. only one budget alert at a time).
    private const val ID_BUDGET_ALERT       = 1001
    private const val ID_LARGE_TRANSACTION  = 1002
    private const val ID_RECURRING_AUTO     = 1003
    private const val ID_DAILY_SUMMARY      = 1004
    private const val ID_WEEKLY_REPORT      = 1005
    // Goal notifications use dynamic IDs per goal to allow multiple concurrently.

    // ── Channel creation ───────────────────────────────────────────────────────

    /**
     * Must be called once in [Application.onCreate] before any notification is posted.
     * Safe to call repeatedly — Android is idempotent with existing channels.
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)

        val channels = listOf(
            NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                "Budget & Spending Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when you approach or exceed your monthly budget."
            },
            NotificationChannel(
                CHANNEL_TRANSACTIONS,
                "Transaction Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for large transactions and auto-logged recurring expenses."
            },
            NotificationChannel(
                CHANNEL_GOALS,
                "Goal Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Milestone celebrations and deadline reminders for your savings goals."
            },
            NotificationChannel(
                CHANNEL_SUMMARIES,
                "Daily & Weekly Summaries",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Scheduled spending summaries and weekly financial reports."
            }
        )

        channels.forEach { manager.createNotificationChannel(it) }
    }

    // ── Permission guard ───────────────────────────────────────────────────────

    private fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    // ── Pending intent (opens MainActivity) ───────────────────────────────────

    private fun mainPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // ── Core notify ───────────────────────────────────────────────────────────

    private fun notify(
        context: Context,
        id: Int,
        channelId: String,
        title: String,
        message: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ) {
        if (!hasPermission(context)) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(mainPendingIntent(context))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }

    // ── Typed notification methods ─────────────────────────────────────────────

    fun notifyBudgetAlert(context: Context, spentPercent: Int, spentAmount: Double, budgetLimit: Double) {
        val title = if (spentPercent >= 100) "🚨 Budget Exceeded!" else "⚠️ Budget Warning"
        val message = if (spentPercent >= 100)
            "You've exceeded your monthly budget of LKR %,.0f. Spent: LKR %,.0f".format(budgetLimit, spentAmount)
        else
            "You've used $spentPercent%% of your LKR %,.0f monthly budget (LKR %,.0f spent).".format(budgetLimit, spentAmount)

        notify(context, ID_BUDGET_ALERT, CHANNEL_BUDGET_ALERTS, title, message, NotificationCompat.PRIORITY_HIGH)
    }

    fun notifyLargeTransaction(context: Context, amount: Double, category: String) {
        notify(
            context,
            ID_LARGE_TRANSACTION,
            CHANNEL_TRANSACTIONS,
            "💸 Large Expense Logged",
            "LKR %,.0f was logged under %s. Tap to review.".format(amount, category),
            NotificationCompat.PRIORITY_DEFAULT
        )
    }

    fun notifyRecurringAutoLogged(context: Context, label: String, amount: Double) {
        notify(
            context,
            ID_RECURRING_AUTO,
            CHANNEL_TRANSACTIONS,
            "🔄 Recurring Expense Auto-Logged",
            "$label (LKR %,.0f) was automatically recorded for you.".format(amount)
        )
    }

    fun notifyGoalMilestone(context: Context, goalTitle: String, percent: Int, goalId: String) {
        val emoji = when {
            percent >= 100 -> "🏆"
            percent >= 75  -> "🎯"
            percent >= 50  -> "🌟"
            else           -> "🚀"
        }
        val message = if (percent >= 100)
            "Congratulations! You've fully funded your '$goalTitle' goal!"
        else
            "You're $percent%% of the way to your '$goalTitle' goal. Keep it up!"

        notify(
            context,
            // Use a unique ID per goal so multiple goals don't collapse each other
            (goalId.hashCode() and 0x0FFF) + 2000,
            CHANNEL_GOALS,
            "$emoji Goal Milestone Reached!",
            message
        )
    }

    fun notifyGoalDeadline(context: Context, goalTitle: String, daysLeft: Long, goalId: String) {
        notify(
            context,
            (goalId.hashCode() and 0x0FFF) + 3000,
            CHANNEL_GOALS,
            "⏰ Goal Deadline Approaching",
            "Your '$goalTitle' goal deadline is in $daysLeft day${if (daysLeft == 1L) "" else "s"}. You still have time!"
        )
    }

    fun notifyDailySummary(context: Context, totalSpent: Double, txCount: Int) {
        notify(
            context,
            ID_DAILY_SUMMARY,
            CHANNEL_SUMMARIES,
            "📊 Today's Spending Summary",
            "You made $txCount transaction${if (txCount == 1) "" else "s"} totalling LKR %,.0f today.".format(totalSpent)
        )
    }

    fun notifyWeeklyReport(context: Context, weeklyIncome: Double, weeklyExpenses: Double) {
        val saved = weeklyIncome - weeklyExpenses
        val savedLabel = if (saved >= 0) "Saved LKR %,.0f".format(saved) else "Over budget by LKR %,.0f".format(-saved)
        notify(
            context,
            ID_WEEKLY_REPORT,
            CHANNEL_SUMMARIES,
            "📅 Your Weekly Financial Report",
            "Income: LKR %,.0f | Expenses: LKR %,.0f | %s".format(weeklyIncome, weeklyExpenses, savedLabel),
            NotificationCompat.PRIORITY_LOW
        )
    }
}
