package com.example.pbd.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pbd.data.local.AppDatabase
import com.example.pbd.data.repository.FinanceRepository
import com.google.firebase.firestore.FirebaseFirestore

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FinanceRepository(
            database.transactionDao(),
            FirebaseFirestore.getInstance()
        )

        return try {
            repository.syncUnsyncedTransactions()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
