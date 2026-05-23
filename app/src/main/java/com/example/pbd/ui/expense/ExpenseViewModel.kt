package com.example.pbd.ui.expense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pbd.data.local.AppDatabase
import com.example.pbd.data.model.Transaction
import com.example.pbd.data.model.TransactionCategory
import com.example.pbd.data.model.TransactionType
import com.example.pbd.data.repository.FinanceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// AndroidViewModel gives us the Application context safely (no memory leaks)
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    // Build the repository here inside the ViewModel, not in the UI
    private val repository: FinanceRepository = FinanceRepository(
        transactionDao = AppDatabase.getDatabase(application).transactionDao(),
        firestore = FirebaseFirestore.getInstance()
    )

    private val _uiState = MutableStateFlow<ExpenseUiState>(ExpenseUiState.Idle)
    val uiState: StateFlow<ExpenseUiState> = _uiState

    fun saveExpense(amount: Double, category: TransactionCategory, subCategory: String, note: String) {
        // Get the real Firebase Auth UID — the auth guard guarantees this is non-null
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _uiState.value = ExpenseUiState.Error("You must be logged in to save an expense.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                val transaction = Transaction(
                    userId = userId,
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    currency = "LKR",       // Expenses are always in LKR
                    exchangeRate = 1.0,     // 1:1 since it's already LKR
                    baseAmountLKR = amount, // This is what the dashboard reads to calculate the balance
                    category = category,
                    subCategory = subCategory, // e.g. "Food", "Transport" — for display in history
                    note = note
                )
                repository.saveTransaction(transaction)
                _uiState.value = ExpenseUiState.Success
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    fun resetState() {
        _uiState.value = ExpenseUiState.Idle
    }
}

// Simple factory that provides the Application to our ViewModel
class ExpenseViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ExpenseViewModel(application) as T
    }
}

// Represents all possible states of the Add Expense screen
sealed class ExpenseUiState {
    object Idle : ExpenseUiState()       // Default: waiting for user input
    object Loading : ExpenseUiState()   // Saving in progress
    object Success : ExpenseUiState()   // Saved successfully, navigate away
    data class Error(val message: String) : ExpenseUiState() // Something went wrong
}

