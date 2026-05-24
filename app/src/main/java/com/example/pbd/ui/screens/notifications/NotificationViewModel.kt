package com.example.pbd.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbd.data.model.NotificationEntity
import com.example.pbd.data.notification.NotificationPreferences
import com.example.pbd.data.repository.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository,
    private val preferences: NotificationPreferences
) : ViewModel() {

    val notifications: StateFlow<List<NotificationEntity>> =
        repository.allNotifications.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val unreadCount: StateFlow<Int> =
        repository.unreadCount.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    fun markAsRead(id: String) {
        viewModelScope.launch { repository.markAsRead(id) }
    }

    fun markAllAsRead() {
        viewModelScope.launch { repository.markAllAsRead() }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch { repository.deleteById(id) }
    }

    // ── Settings helpers ──────────────────────────────────────────────────────

    val budgetThreshold: Double get() = preferences.budgetThreshold
    val largeTransactionThreshold: Double get() = preferences.largeTransactionThreshold
    val dailySummaryHour: Int get() = preferences.dailySummaryHour
    val dailySummaryMinute: Int get() = preferences.dailySummaryMinute
    val notificationsEnabled: Boolean get() = preferences.notificationsEnabled

    fun saveBudgetThreshold(value: Double) {
        preferences.budgetThreshold = value
    }

    fun saveLargeTransactionThreshold(value: Double) {
        preferences.largeTransactionThreshold = value
    }

    fun saveDailySummaryTime(hour: Int, minute: Int) {
        preferences.dailySummaryHour = hour
        preferences.dailySummaryMinute = minute
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        preferences.notificationsEnabled = enabled
    }
}
