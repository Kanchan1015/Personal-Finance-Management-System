package com.example.pbd.data.repository

import com.example.pbd.data.model.Goal
import com.example.pbd.data.model.Transaction
import com.example.pbd.data.model.TransactionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DashboardRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId get() = auth.currentUser?.uid ?: ""

    fun getTransactions(): Flow<List<Transaction>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore
            .collection("transactions")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Transaction(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            type = TransactionType.valueOf(
                                doc.getString("type") ?: "EXPENSE"
                            ),
                            amount = doc.getDouble("amount") ?: 0.0,
                            currency = doc.getString("currency") ?: "LKR",
                            exchangeRate = doc.getDouble("exchangeRate") ?: 1.0,
                            baseAmountLKR = doc.getDouble("baseAmountLKR") ?: 0.0,
                            category = com.example.pbd.data.model.TransactionCategory.valueOf(
                                doc.getString("category") ?: "DISCRETIONARY"
                            ),
                            subCategory = doc.getString("subCategory") ?: "",
                            note = doc.getString("note") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            isSynced = true
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(transactions)
            }

        awaitClose { listener.remove() }
    }

    fun getGoals(): Flow<List<Goal>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore
            .collection("goals")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val goals = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Goal(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            title = doc.getString("title") ?: "",
                            targetAmount = doc.getDouble("targetAmount") ?: 0.0,
                            currentSaved = doc.getDouble("currentSaved") ?: 0.0,
                            deadline = doc.getLong("deadline") ?: 0L,
                            status = doc.getString("status") ?: "ACTIVE"
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(goals)
            }

        awaitClose { listener.remove() }
    }
}