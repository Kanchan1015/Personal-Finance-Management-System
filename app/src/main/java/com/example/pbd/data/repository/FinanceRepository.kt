package com.example.pbd.data.repository

import com.example.pbd.data.local.TransactionDao
import com.example.pbd.data.model.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val firestore: FirebaseFirestore
) {
    // Exposes a live stream of all transactions from Room
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun saveTransaction(transaction: Transaction) {
        // Step 1: Always save to the local Room database first.
        // This means the app works even with no internet.
        transactionDao.insertTransaction(transaction)

        // Step 2: Try to push to Firebase asynchronously in the background.
        // This ensures Firestore connection hangs/timeouts never freeze the saving process or keep the UI loading.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                pushToFirestore(transaction)
                // Step 3: If Firebase succeeds, mark it as synced in Room.
                transactionDao.markAsSynced(transaction.id)
            } catch (e: Exception) {
                // If offline, the transaction stays in Room with isSynced = false.
                // The SyncWorker will retry this later automatically.
                e.printStackTrace()
            }
        }
    }

    // Called by SyncWorker when the device comes back online.
    suspend fun syncUnsyncedTransactions() {
        val unsynced = transactionDao.getUnsyncedTransactions()
        unsynced.forEach { transaction ->
            try {
                pushToFirestore(transaction)
                transactionDao.markAsSynced(transaction.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Builds a clean Map so we only send Firestore what it needs.
    // We deliberately exclude 'isSynced' because that is a local-only field.
    private suspend fun pushToFirestore(transaction: Transaction) {
        val firestoreData = mapOf(
            "userId"        to transaction.userId,
            "type"          to transaction.type.name,           // Stored as "INCOME" or "EXPENSE"
            "amount"        to transaction.amount,
            "currency"      to transaction.currency,
            "exchangeRate"  to transaction.exchangeRate,
            "baseAmountLKR" to transaction.baseAmountLKR,       // Used by the dashboard to calculate totals
            "category"      to transaction.category.name,       // Stored as "COMMITTED" or "DISCRETIONARY"
            "subCategory"   to transaction.subCategory,         // e.g. "Food", "Transport" — used for icons in history
            "note"          to transaction.note,
            "timestamp"     to transaction.timestamp
        )
        firestore.collection("transactions")
            .document(transaction.id)
            .set(firestoreData)
            .await()
    }
}

