package com.example.pbd

import android.app.Application
import androidx.work.*
import com.example.pbd.data.worker.SyncWorker
import com.example.pbd.data.worker.RecurringExpenseWorker
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
        
        setupSyncWorker()
        setupRecurringExpenseWorker()
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
}
