package com.example.pbd.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pbd.data.local.AppDatabase
import com.example.pbd.data.repository.FinanceRepository
import com.google.firebase.firestore.FirebaseFirestore

class RecurringExpenseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = FinanceRepository(
                transactionDao = database.transactionDao(),
                recurringExpenseDao = database.recurringExpenseDao(),
                firestore = FirebaseFirestore.getInstance()
            )
            repository.checkAndProcessRecurringExpenses()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
