package com.example.pbd.data.repository

import com.example.pbd.data.local.TransactionDao
import com.example.pbd.data.local.RecurringExpenseDao
import com.example.pbd.data.model.Transaction
import com.example.pbd.data.model.RecurringExpense
import com.example.pbd.data.model.TransactionType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val recurringExpenseDao: RecurringExpenseDao,
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

    // Exposes a stream of all recurring templates
    val allRecurringExpenses: Flow<List<RecurringExpense>> = recurringExpenseDao.getAllRecurringExpenses()

    // Saves a new recurring expense template to local Room
    suspend fun saveRecurringExpense(recurringExpense: RecurringExpense) {
        recurringExpenseDao.insertRecurringExpense(recurringExpense)
    }

    // Called by Periodic WorkManager task to auto-log due transactions
    suspend fun checkAndProcessRecurringExpenses() {
        val activeExpenses = recurringExpenseDao.getActiveRecurringExpenses()
        val now = System.currentTimeMillis()

        activeExpenses.forEach { recurring ->
            if (recurring.nextExecutionDate <= now) {
                // Time to auto-log a new transaction!
                val autoTransaction = Transaction(
                    userId = recurring.userId,
                    type = TransactionType.EXPENSE,
                    amount = recurring.amount,
                    currency = "LKR",
                    exchangeRate = 1.0,
                    baseAmountLKR = recurring.amount,
                    category = recurring.category,
                    subCategory = recurring.subCategory,
                    note = "${recurring.note} (Auto-logged)",
                    timestamp = recurring.nextExecutionDate
                )

                // Save locally and push to Firestore
                saveTransaction(autoTransaction)

                // Calculate next execution date
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = recurring.nextExecutionDate
                if (recurring.interval == "WEEKLY") {
                    cal.add(java.util.Calendar.DAY_OF_YEAR, 7)
                } else {
                    cal.add(java.util.Calendar.MONTH, 1)
                }

                // Update next execution date in local DB
                recurringExpenseDao.updateNextExecutionDate(recurring.id, cal.timeInMillis)
            }
        }
    }
}

