package com.example.pbd.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbd.data.model.Transaction
import com.example.pbd.data.repository.FinanceRepository
import com.example.pbd.data.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionHistoryViewModel(
    private val repository: FinanceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: ""
            if (userId.isNotEmpty()) {
                repository.syncTransactionsFromFirestore(userId)
            }
        }
    }

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

    // Deletes a transaction by id from Room instantly; Firestore removal happens asynchronously.
    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    // Re-saves an edited transaction using the same id (Room REPLACE strategy).
    // This updates the local record immediately and triggers an async Firestore push.
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.saveTransaction(transaction)
        }
    }
}
