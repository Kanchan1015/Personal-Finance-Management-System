package com.example.pbd.data.repository

import android.util.Log
import com.example.pbd.data.local.TransactionDao
import com.example.pbd.data.local.RecurringExpenseDao
import com.example.pbd.data.model.ExchangeRateResponse
import com.example.pbd.data.model.RecurringExpense
import com.example.pbd.data.model.Transaction
import com.example.pbd.data.model.TransactionType
import com.example.pbd.data.remote.ExchangeRateApi
import com.example.pbd.data.remote.RetrofitClient
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
    private companion object {
        const val TAG = "FinanceRepository"
    }

    private val exchangeRateApi: ExchangeRateApi = RetrofitClient.exchangeRateApi

    // ── Transactions ──────────────────────────────────────────────────────────

    // Exposes a live stream of all transactions from Room
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    // saveIncome delegates to saveTransaction (offline-first: Room first, then Firestore)
    suspend fun saveIncome(transaction: Transaction): Result<Unit> {
        return try {
            saveTransaction(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExchangeRate(baseCurrency: String): Result<Double> {
        return try {
            val normalizedCurrency = baseCurrency.uppercase()
            Log.d(TAG, "getExchangeRate requestedCurrency=$normalizedCurrency")

            val response: ExchangeRateResponse = exchangeRateApi.getLatestExchangeRates(
                baseCurrency = normalizedCurrency
            )
            Log.d(
                TAG,
                "getExchangeRate apiResult=${response.result}, baseCode=${response.baseCode}, lkrRate=${response.rates["LKR"]}"
            )

            if (response.result.lowercase() != "success") {
                val result = Result.failure<Double>(
                    IllegalStateException("Exchange rate API returned '${response.result}'")
                )
                Log.d(TAG, "getExchangeRate returningResult=$result")
                result
            } else {
                val lkrRate = response.rates["LKR"]
                if (lkrRate != null) {
                    val result = Result.success(lkrRate)
                    Log.d(TAG, "getExchangeRate returningResult=$result")
                    result
                } else {
                    val result = Result.failure<Double>(
                        IllegalStateException("LKR exchange rate not found for ${response.baseCode}")
                    )
                    Log.d(TAG, "getExchangeRate returningResult=$result")
                    result
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getExchangeRate failed for currency=$baseCurrency", e)
            Result.failure(e)
        }
    }

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

    // ── Recurring Expenses ────────────────────────────────────────────────────

    // Live stream of all recurring expense templates
    val allRecurringExpenses: Flow<List<RecurringExpense>> = recurringExpenseDao.getAllRecurringExpenses()

    // Saves a new recurring expense template to local Room
    suspend fun saveRecurringExpense(recurringExpense: RecurringExpense) {
        recurringExpenseDao.insertRecurringExpense(recurringExpense)
    }

    // Called by RecurringExpenseWorker every 12 hours to auto-log any due expenses
    // Called by RecurringExpenseWorker every 12 hours to auto-log any due expenses
    suspend fun checkAndProcessRecurringExpenses() {
        val activeExpenses = recurringExpenseDao.getActiveRecurringExpenses()
        val now = System.currentTimeMillis()

        activeExpenses.forEach { recurring ->
            var nextExecution = recurring.nextExecutionDate
            val cal = java.util.Calendar.getInstance()

            // Catch up in case the worker didn't run for multiple cycles.
            while (nextExecution <= now) {
                val autoTransaction = Transaction(
                    // Deterministic ID prevents duplicates if this occurrence is retried.
                    id = "${recurring.id}:$nextExecution",
                    userId = recurring.userId,
                    type = TransactionType.EXPENSE,
                    amount = recurring.amount,
                    currency = "LKR",
                    exchangeRate = 1.0,
                    baseAmountLKR = recurring.amount,
                    category = recurring.category,
                    subCategory = recurring.subCategory,
                    note = if (recurring.note.isBlank()) "Auto-logged" else "${recurring.note} (Auto-logged)",
                    // Record the scheduled execution time rather than 'now'.
                    timestamp = nextExecution
                )

                saveTransaction(autoTransaction)

                // Advance to the next scheduled execution
                cal.timeInMillis = nextExecution
                if (recurring.interval == "WEEKLY") {
                    cal.add(java.util.Calendar.DAY_OF_YEAR, 7)
                } else {
                    cal.add(java.util.Calendar.MONTH, 1)
                }
                nextExecution = cal.timeInMillis
            }

            // Persist the next due date only if it changed
            if (nextExecution != recurring.nextExecutionDate) {
                recurringExpenseDao.updateNextExecutionDate(recurring.id, nextExecution)
            }
        }
    }
        }
    }
}
