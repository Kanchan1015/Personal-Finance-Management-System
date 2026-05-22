package com.example.pbd.ui.screens.income

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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IncomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository = FinanceRepository(
        transactionDao = AppDatabase.getDatabase(application).transactionDao(),
        firestore = FirebaseFirestore.getInstance()
    )

    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    fun saveIncome(
        amount: Double,
        currency: String,
        category: TransactionCategory,
        exchangeRate: Double = 1.0,
        baseAmountLKR: Double = amount
    ) {
        viewModelScope.launch {
            _uiState.value = IncomeUiState(isLoading = true)

            try {
                val incomeTransaction = Transaction(
                    userId = "",
                    type = TransactionType.INCOME,
                    amount = amount,
                    currency = currency,
                    exchangeRate = exchangeRate,
                    baseAmountLKR = baseAmountLKR,
                    category = category
                )

                repository.saveIncome(incomeTransaction)
                    .onSuccess {
                        _uiState.value = IncomeUiState(isSuccess = true)
                    }
                    .onFailure { throwable ->
                        _uiState.value = IncomeUiState(
                            errorMessage = throwable.message ?: "Something went wrong"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = IncomeUiState(
                    errorMessage = e.message ?: "Something went wrong"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = IncomeUiState()
    }
}

class IncomeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return IncomeViewModel(application) as T
    }
}
