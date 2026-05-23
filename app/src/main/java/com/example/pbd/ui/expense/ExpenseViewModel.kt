package com.example.pbd.ui.expense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pbd.data.local.AppDatabase
import com.example.pbd.data.model.Transaction
import com.example.pbd.data.model.RecurringExpense
import com.example.pbd.data.model.TransactionCategory
import com.example.pbd.data.model.TransactionType
import com.example.pbd.data.repository.FinanceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// AndroidViewModel gives us the Application context safely (no memory leaks)
class ExpenseViewModel(private val repository: FinanceRepository, private val auth: FirebaseAuth) : ViewModel() {

    // Build the repository here inside the ViewModel, not in the UI

    private val _uiState = MutableStateFlow<ExpenseUiState>(ExpenseUiState.Idle)
    val uiState: StateFlow<ExpenseUiState> = _uiState

    fun saveExpense(
        amount: Double,
        category: TransactionCategory,
        subCategory: String,
        note: String,
        timestamp: Long = System.currentTimeMillis(),
        isRecurring: Boolean = false,
        recurringInterval: String = "MONTHLY"
    ) {
        // Get the real Firebase Auth UID — the auth guard guarantees this is non-null
        val userId = auth.currentUser?.uid
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
                    note = note,
                    timestamp = timestamp
                )
                repository.saveTransaction(transaction)

                if (isRecurring) {
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = timestamp
                    if (recurringInterval == "WEEKLY") {
                        cal.add(java.util.Calendar.DAY_OF_YEAR, 7)
                    } else {
                        cal.add(java.util.Calendar.MONTH, 1)
                    }
                    val recurringExpense = RecurringExpense(
                        userId = userId,
                        amount = amount,
                        category = category,
                        subCategory = subCategory,
                        note = note,
                        interval = recurringInterval,
                        nextExecutionDate = cal.timeInMillis,
                        isEnabled = true
                    )
                    repository.saveRecurringExpense(recurringExpense)
                }

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
