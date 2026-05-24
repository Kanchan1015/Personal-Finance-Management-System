package com.example.pbd.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbd.data.model.User
import com.example.pbd.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import android.content.Context
import com.example.pbd.data.notification.NotificationPreferences
import com.example.pbd.data.notification.DailyReminderReceiver

// State for the Profile UI
sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val preferences: NotificationPreferences,
    private val context: Context
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    val budgetThreshold: Double get() = preferences.budgetThreshold
    val largeTransactionThreshold: Double get() = preferences.largeTransactionThreshold
    val dailySummaryHour: Int get() = preferences.dailySummaryHour
    val dailySummaryMinute: Int get() = preferences.dailySummaryMinute
    val notificationsEnabled: Boolean get() = preferences.notificationsEnabled

    init {
        // Automatically load the profile when this screen is opened
        loadUserProfile()
    }

    fun loadUserProfile() {
        val userId = authRepository.getCurrentUserId()
        
        if (userId != null) {
            _profileState.value = ProfileState.Loading
            viewModelScope.launch {
                val result = authRepository.getUserProfile(userId)
                result.fold(
                    onSuccess = { user -> _profileState.value = ProfileState.Success(user) },
                    onFailure = { e -> _profileState.value = ProfileState.Error(e.message ?: "Failed to load profile") }
                )
            }
        } else {
            _profileState.value = ProfileState.Error("No user is currently logged in.")
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun updateUserProfile(
        name: String,
        baseCurrency: String,
        savingsPercentage: Int,
        notificationsEnabled: Boolean,
        budget: Double,
        largeTx: Double,
        hour: Int,
        minute: Int
    ) {
        val currentState = _profileState.value
        if (currentState is ProfileState.Success) {
            val updatedUser = currentState.user.copy(
                name = name,
                baseCurrency = baseCurrency,
                savingsPercentage = savingsPercentage,
                notificationsEnabled = notificationsEnabled
            )
            _profileState.value = ProfileState.Loading

            // Save notification preferences
            preferences.budgetThreshold = budget
            preferences.largeTransactionThreshold = largeTx
            preferences.dailySummaryHour = hour
            preferences.dailySummaryMinute = minute
            preferences.notificationsEnabled = notificationsEnabled

            // Reschedule the daily summary notification alarm with new times/settings immediately
            DailyReminderReceiver.scheduleNextAlarm(context)

            viewModelScope.launch {
                val result = authRepository.updateUserProfile(updatedUser)
                result.fold(
                    onSuccess = {
                        _profileState.value = ProfileState.Success(updatedUser)
                    },
                    onFailure = { e ->
                        _profileState.value = ProfileState.Error(e.message ?: "Failed to update profile")
                    }
                )
            }
        }
    }
}
