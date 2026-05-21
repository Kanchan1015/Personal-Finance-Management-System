package com.example.pbd.data.repository

import com.example.pbd.data.model.Goal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GoalRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId get() = auth.currentUser?.uid ?: ""

    fun getGoalById(goalId: String): Flow<Goal?> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore
            .collection("goals")
            .document(goalId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val goal = Goal(
                            id = snapshot.id,
                            userId = snapshot.getString("userId") ?: "",
                            title = snapshot.getString("title") ?: "",
                            targetAmount = snapshot.getDouble("targetAmount") ?: 0.0,
                            currentSaved = snapshot.getDouble("currentSaved") ?: 0.0,
                            deadline = snapshot.getLong("deadline") ?: 0L,
                            status = snapshot.getString("status") ?: "ACTIVE"
                        )
                        trySend(goal)
                    } catch (e: Exception) {
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }

    fun getAllGoals(): Flow<List<Goal>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore
            .collection("goals")
            .whereEqualTo("userId", userId)
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