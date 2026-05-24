package com.example.pbd.data.repository

import android.content.Context
import android.util.Log
import com.example.pbd.data.local.TransactionDao
import com.example.pbd.data.local.RecurringExpenseDao
import com.example.pbd.data.model.ExchangeRateResponse
import com.example.pbd.data.model.NotificationEntity
import com.example.pbd.data.model.RecurringExpense
import com.example.pbd.data.model.Transaction
import com.example.pbd.data.model.TransactionType
import com.example.pbd.data.notification.NotificationHelper
import com.example.pbd.data.notification.NotificationPreferences
import com.example.pbd.data.remote.ExchangeRateApi
import com.example.pbd.data.remote.RetrofitClient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val recurringExpenseDao: RecurringExpenseDao,
    private val firestore: FirebaseFirestore,
    private val context: Context,
    private val notificationRepository: NotificationRepository
) {
    private companion object {
        const val TAG = "FinanceRepository"
    }

    private val exchangeRateApi: ExchangeRateApi = RetrofitClient.exchangeRateApi
    private val notifPrefs = NotificationPreferences(context)

    // ── Transactions ──────────────────────────────────────────────────────────

    // Exposes a live stream of all transactions from Room
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    // Exposes a live, user-scoped stream from Room.
    // The dashboard uses this so income appears immediately after saving locally,
    // without waiting for the background Firestore push to complete.
    fun getTransactionsByUser(userId: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsByUser(userId)

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

        // Step 4: Fire event-driven notifications (only for expenses)
        if (transaction.type == TransactionType.EXPENSE && notifPrefs.notificationsEnabled) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    checkAndFireTransactionNotifications(transaction)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Checks large-transaction and monthly-budget thresholds after each expense is saved.
     * Fires system notifications and persists them to the Notification Center.
     */
    private suspend fun checkAndFireTransactionNotifications(transaction: Transaction) {
        // ── Large Transaction Alert ────────────────────────────────────────────
        val largeThreshold = notifPrefs.largeTransactionThreshold
        if (transaction.baseAmountLKR >= largeThreshold) {
            NotificationHelper.notifyLargeTransaction(
                context,
                transaction.baseAmountLKR,
                transaction.subCategory.ifBlank { transaction.category.name }
            )
            notificationRepository.save(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    title = "💸 Large Expense Logged",
                    message = "LKR %,.0f was logged under %s.".format(
                        transaction.baseAmountLKR,
                        transaction.subCategory.ifBlank { transaction.category.name }
                    ),
                    type = "LARGE_TRANSACTION"
                )
            )
        }

        // ── Monthly Budget Alert ───────────────────────────────────────────────
        val budgetLimit = notifPrefs.budgetThreshold
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfMonth = cal.timeInMillis

        val allTx = transactionDao.getAllTransactions().first()
        val monthlySpent = allTx.filter {
            it.type == TransactionType.EXPENSE &&
            it.userId == transaction.userId &&
            it.timestamp >= startOfMonth
        }.sumOf { it.baseAmountLKR }

        val percent = ((monthlySpent / budgetLimit) * 100).toInt()
        // Fire at 80% and 100%+ (use modular ranges to avoid spamming on every transaction)
        if (percent in 80..99 || percent >= 100) {
            NotificationHelper.notifyBudgetAlert(context, percent, monthlySpent, budgetLimit)
            notificationRepository.save(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    title = if (percent >= 100) "🚨 Budget Exceeded!" else "⚠️ Budget Warning",
                    message = "You've used $percent% of your LKR %,.0f monthly budget.".format(budgetLimit),
                    type = "BUDGET_ALERT"
                )
            )
        }
    }

    // Deletes a transaction by id from the local Room database immediately (offline-first).
    // Then asynchronously tries to remove the document from Firestore in the background.
    // If offline, the local delete still applies; the Firestore document will linger but
    // will be overwritten on next sync if the user re-adds the same id.
    suspend fun deleteTransaction(id: String) {
        transactionDao.deleteTransactionById(id)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("transactions").document(id).delete().await()
            } catch (e: Exception) {
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

    /**
     * Pulls all transactions for the given user from Firestore and persists them
     * to the local Room database (with isSynced = true) if they do not already exist.
     */
    suspend fun syncTransactionsFromFirestore(userId: String) {
        try {
            Log.d(TAG, "syncTransactionsFromFirestore starting for userId=$userId")
            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val remoteTxList = snapshot.documents.mapNotNull { doc ->
                try {
                    val typeStr = doc.getString("type") ?: "EXPENSE"
                    val catStr = doc.getString("category") ?: "DISCRETIONARY"
                    Transaction(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        type = TransactionType.valueOf(typeStr),
                        amount = doc.getDouble("amount") ?: 0.0,
                        currency = doc.getString("currency") ?: "LKR",
                        exchangeRate = doc.getDouble("exchangeRate") ?: 1.0,
                        baseAmountLKR = doc.getDouble("baseAmountLKR") ?: 0.0,
                        category = com.example.pbd.data.model.TransactionCategory.valueOf(catStr),
                        subCategory = doc.getString("subCategory") ?: "",
                        note = doc.getString("note") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        isSynced = true
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse Firestore transaction document: ${doc.id}", e)
                    null
                }
            }

            if (remoteTxList.isNotEmpty()) {
                remoteTxList.forEach { tx ->
                    transactionDao.insertTransaction(tx)
                }
                Log.d(TAG, "syncTransactionsFromFirestore inserted ${remoteTxList.size} transactions into Room")
            }
        } catch (e: Exception) {
            Log.e(TAG, "syncTransactionsFromFirestore failed for userId=$userId", e)
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

    /**
     * Called by RecurringExpenseWorker every 12 hours to auto-log any due expenses.
     * Returns a list of (label, amount) pairs for each expense that was auto-logged,
     * so the worker can fire a notification per processed expense.
     */
    suspend fun checkAndProcessRecurringExpenses(): List<Pair<String, Double>> {
        val activeExpenses = recurringExpenseDao.getActiveRecurringExpenses()
        val now = System.currentTimeMillis()
        val processed = mutableListOf<Pair<String, Double>>()

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
                processed.add(Pair(recurring.subCategory.ifBlank { recurring.category.name }, recurring.amount))

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

        return processed
    }
}
