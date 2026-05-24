package com.example.pbd.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pbd.data.local.AppDatabase
import com.example.pbd.data.model.NotificationEntity
import com.example.pbd.data.model.TransactionType
import com.example.pbd.data.notification.NotificationHelper
import com.example.pbd.data.notification.NotificationPreferences
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.UUID

/**
 * Runs every Sunday at 9 AM via [PeriodicWorkRequest].
 *
 * Fetches last 7 days of transactions from Room, computes income & expense totals,
 * fires a "Weekly Financial Report" system notification, and persists it to
 * the in-app Notification Center.
 */
class WeeklyReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = NotificationPreferences(applicationContext)
        if (!prefs.notificationsEnabled) return Result.success()

        return try {
            val db = AppDatabase.getDatabase(applicationContext)

            val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
            val allTx = db.transactionDao().getAllTransactions().first()

            val weeklyTx = allTx.filter { it.timestamp >= sevenDaysAgo }
            val weeklyIncome   = weeklyTx.filter { it.type == TransactionType.INCOME }.sumOf { it.baseAmountLKR }
            val weeklyExpenses = weeklyTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.baseAmountLKR }

            NotificationHelper.notifyWeeklyReport(applicationContext, weeklyIncome, weeklyExpenses)

            val saved = weeklyIncome - weeklyExpenses
            val savedLabel = if (saved >= 0)
                "Saved LKR %,.0f".format(saved)
            else
                "Over budget by LKR %,.0f".format(-saved)

            db.notificationDao().insert(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    title = "📅 Your Weekly Financial Report",
                    message = "Income: LKR %,.0f | Expenses: LKR %,.0f | %s".format(weeklyIncome, weeklyExpenses, savedLabel),
                    type = "WEEKLY_REPORT"
                )
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
