package com.example.pbd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Persists every notification fired so the user can review them
 * in the Notification Center screen, even after dismissing the system notification.
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    /** One of: BUDGET_ALERT, LARGE_TRANSACTION, RECURRING_AUTO, GOAL_MILESTONE, GOAL_DEADLINE, DAILY_SUMMARY, WEEKLY_REPORT */
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
