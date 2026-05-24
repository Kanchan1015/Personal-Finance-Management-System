package com.example.pbd.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbd.data.model.User
import com.example.pbd.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// State for the Profile UI
sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

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
        notificationsEnabled: Boolean
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
