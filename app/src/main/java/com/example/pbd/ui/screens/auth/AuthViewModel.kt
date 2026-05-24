package com.example.pbd.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbd.data.model.User
import com.example.pbd.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

//  various states of the authentication process
sealed class AuthState {
    object Idle : AuthState() // Initial state before any action is taken
    object Loading : AuthState() // When a network request is in progress
    data class Success(val user: User) : AuthState() // On successful login/registration
    data class PasswordResetSent(val email: String) : AuthState()
    data class Error(val message: String) : AuthState() // On failure with an error message
}

// ViewModel connecting the UI to the AuthRepository
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Internal mutable state that  update based on network results
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    
    
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Handles user login and updates state based on the result
    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { user -> _authState.value = AuthState.Success(user) },
                onFailure = { e -> _authState.value = AuthState.Error(e.message ?: "An unknown error occurred during login") }
            )
        }
    }

    // Handles user registration, creates Firestore profile, and updates state
    fun register(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.register(name, email, password)
            result.fold(
                onSuccess = { user -> _authState.value = AuthState.Success(user) },
                onFailure = { e -> _authState.value = AuthState.Error(e.message ?: "An unknown error occurred during registration") }
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken)
            result.fold(
                onSuccess = { user -> _authState.value = AuthState.Success(user) },
                onFailure = { e ->
                    _authState.value = AuthState.Error(
                        e.message ?: "Unable to sign in with Google"
                    )
                }
            )
        }
    }

    fun sendPasswordResetEmail(email: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            _authState.value = AuthState.Error("Enter your email address first")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(trimmedEmail)
            result.fold(
                onSuccess = { _authState.value = AuthState.PasswordResetSent(trimmedEmail) },
                onFailure = { e ->
                    _authState.value = AuthState.Error(
                        e.message ?: "Unable to send password reset email"
                    )
                }
            )
        }
    }

    // Resets the state back to Idle (useful after navigating away or dismissing an error)
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // Logs the current user out via Firebase and clears the local state
    fun logout() {
        authRepository.logout()
        _authState.value = AuthState.Idle
    }
}
