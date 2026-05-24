package com.example.pbd.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pbd.data.local.AppDatabase
import com.example.pbd.data.model.NotificationEntity
import com.example.pbd.data.notification.NotificationHelper
import com.example.pbd.data.notification.NotificationPreferences
import com.example.pbd.data.repository.FinanceRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class RecurringExpenseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: FinanceRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            val processedExpenses = repository.checkAndProcessRecurringExpenses()

            // Fire a notification for each auto-logged recurring expense
            val prefs = NotificationPreferences(applicationContext)
            if (prefs.notificationsEnabled && processedExpenses.isNotEmpty()) {
                val db = AppDatabase.getDatabase(applicationContext)

                processedExpenses.forEach { (label, amount) ->
                    NotificationHelper.notifyRecurringAutoLogged(applicationContext, label, amount)

                    db.notificationDao().insert(
                        NotificationEntity(
                            id = UUID.randomUUID().toString(),
                            title = "🔄 Recurring Expense Auto-Logged",
                            message = "$label (LKR %,.0f) was automatically recorded for you.".format(amount),
                            type = "RECURRING_AUTO"
                        )
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

