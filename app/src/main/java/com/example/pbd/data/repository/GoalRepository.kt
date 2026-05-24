package com.example.pbd.data.repository

import android.content.Context
import com.example.pbd.data.model.Goal
import com.example.pbd.data.model.NotificationEntity
import com.example.pbd.data.notification.NotificationHelper
import com.example.pbd.data.notification.NotificationPreferences
import com.example.pbd.data.worker.GoalDeadlineWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class GoalRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val context: Context,
    private val notificationRepository: NotificationRepository
) {

    private val userId get() = auth.currentUser?.uid ?: ""
    private val notifPrefs = NotificationPreferences(context)

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

                // Check deadline for each active goal whenever we get an update
                if (notifPrefs.notificationsEnabled) {
                    goals.filter { it.status == "ACTIVE" }.forEach { goal ->
                        GoalDeadlineWorker.checkAndNotify(context, goal.id, goal.title, goal.deadline)
                    }
                }

                trySend(goals)
            }

        awaitClose { listener.remove() }
    }

    suspend fun addGoal(title: String, targetAmount: Double, months: Int) {
        val currentUserId = auth.currentUser?.uid ?: return
        val deadline = System.currentTimeMillis() + (months.toLong() * 30 * 24 * 60 * 60 * 1000)
        val goal = hashMapOf(
            "userId" to currentUserId,
            "title" to title,
            "targetAmount" to targetAmount,
            "currentSaved" to 0.0,
            "deadline" to deadline,
            "status" to "ACTIVE"
        )
        firestore.collection("goals").add(goal)
    }

    suspend fun updateGoalSavedAmount(goalId: String, addedAmount: Double) {
        val docRef = firestore.collection("goals").document(goalId)
        var goalTitle = ""
        var newPercent = 0
        var oldPercent = 0

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            goalTitle = snapshot.getString("title") ?: ""
            val currentSaved = snapshot.getDouble("currentSaved") ?: 0.0
            val targetAmount = snapshot.getDouble("targetAmount") ?: 0.0
            val newSaved = currentSaved + addedAmount
            val status = if (newSaved >= targetAmount) "COMPLETED" else "ACTIVE"

            // Compute milestone percentages before and after
            oldPercent = if (targetAmount > 0) ((currentSaved / targetAmount) * 100).toInt() else 0
            newPercent = if (targetAmount > 0) ((newSaved / targetAmount) * 100).toInt() else 0

            transaction.update(docRef, mapOf(
                "currentSaved" to newSaved,
                "status" to status
            ))
        }.await()

        // Fire a milestone notification if a milestone boundary was crossed
        if (notifPrefs.notificationsEnabled) {
            val milestones = listOf(25, 50, 75, 100)
            val crossedMilestone = milestones.firstOrNull { it in (oldPercent + 1)..newPercent }
            if (crossedMilestone != null) {
                NotificationHelper.notifyGoalMilestone(context, goalTitle, crossedMilestone, goalId)
                CoroutineScope(Dispatchers.IO).launch {
                    val emoji = when {
                        crossedMilestone >= 100 -> "🏆"
                        crossedMilestone >= 75  -> "🎯"
                        crossedMilestone >= 50  -> "🌟"
                        else                    -> "🚀"
                    }
                    notificationRepository.save(
                        NotificationEntity(
                            id = UUID.randomUUID().toString(),
                            title = "$emoji Goal Milestone Reached!",
                            message = if (crossedMilestone >= 100)
                                "Congratulations! You've fully funded your '$goalTitle' goal!"
                            else
                                "You're $crossedMilestone% of the way to your '$goalTitle' goal!",
                            type = "GOAL_MILESTONE"
                        )
                    )
                }
            }
        }
    }

    suspend fun deleteGoal(goalId: String) {
        firestore.collection("goals").document(goalId).delete()
    }
}