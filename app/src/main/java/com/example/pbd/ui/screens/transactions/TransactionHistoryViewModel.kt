package com.example.pbd.ui.screens.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pbd.data.local.AppDatabase
import com.example.pbd.data.model.Transaction
import com.example.pbd.data.repository.FinanceRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TransactionHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository = FinanceRepository(
        transactionDao = AppDatabase.getDatabase(application).transactionDao(),
        recurringExpenseDao = AppDatabase.getDatabase(application).recurringExpenseDao(),
        firestore = FirebaseFirestore.getInstance()
    )

    // Flow that retrieves all transactions (both INCOME and EXPENSE) from the local
    // database and exposes them sorted newest-first.
    val transactionsState: StateFlow<List<Transaction>> = repository.allTransactions
        .map { dbTransactions ->
            // Sort by timestamp descending — show newest first
            dbTransactions.sortedByDescending { it.timestamp }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

class TransactionHistoryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TransactionHistoryViewModel(application) as T
    }
}
