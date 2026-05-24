package com.example.pbd

import android.app.Application
import androidx.work.*
import com.example.pbd.data.notification.DailyReminderReceiver
import com.example.pbd.data.notification.NotificationHelper
import com.example.pbd.data.worker.GoalDeadlineWorker
import com.example.pbd.data.worker.SyncWorker
import com.example.pbd.data.worker.RecurringExpenseWorker
import com.example.pbd.data.worker.WeeklyReportWorker
import com.example.pbd.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class PbdApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@PbdApplication)
            modules(appModule)
        }

        // ── Notification system ────────────────────────────────────────────────
        // Create all notification channels (safe to call repeatedly; Android is idempotent)
        NotificationHelper.createChannels(this)

        // Schedule the daily spending summary alarm (10 PM by default)
        DailyReminderReceiver.scheduleNextAlarm(this)

        // ── Workers ───────────────────────────────────────────────────────────
        setupSyncWorker()
        setupRecurringExpenseWorker()
        setupWeeklyReportWorker()
        setupGoalDeadlineWorker()
    }

    private fun setupSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun setupRecurringExpenseWorker() {
        val recurringRequest = PeriodicWorkRequestBuilder<RecurringExpenseWorker>(12, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "RecurringExpenseWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            recurringRequest
        )
    }

    private fun setupWeeklyReportWorker() {
        // Runs every 7 days; WorkManager handles the exact scheduling
        val weeklyRequest = PeriodicWorkRequestBuilder<WeeklyReportWorker>(7, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WeeklyReportWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            weeklyRequest
        )
    }

    private fun setupGoalDeadlineWorker() {
        // Runs once a day to check for upcoming goal deadlines
        val deadlineRequest = PeriodicWorkRequestBuilder<GoalDeadlineWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "GoalDeadlineWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            deadlineRequest
        )
    }
}

