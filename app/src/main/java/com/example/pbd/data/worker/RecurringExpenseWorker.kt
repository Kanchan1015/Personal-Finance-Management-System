package com.example.pbd.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pbd.data.repository.FinanceRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RecurringExpenseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: FinanceRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            repository.checkAndProcessRecurringExpenses()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
