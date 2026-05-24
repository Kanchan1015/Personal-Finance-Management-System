package com.example.pbd.data.repository

import com.example.pbd.data.local.NotificationDao
import com.example.pbd.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class NotificationRepository(private val dao: NotificationDao) {

    /** Live stream of all notifications — drives the Notification Center UI. */
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()

    /** Live stream of the unread count — drives the bell badge. */
    val unreadCount: Flow<Int> = dao.getUnreadCount()

    suspend fun save(notification: NotificationEntity) {
        dao.insert(notification)
        // Auto-prune records older than 30 days to keep the DB lean
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        dao.deleteOlderThan(cutoff)
    }

    suspend fun markAsRead(id: String) = dao.markAsRead(id)

    suspend fun markAllAsRead() = dao.markAllAsRead()

    suspend fun deleteById(id: String) = dao.deleteById(id)
}
