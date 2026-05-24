package com.example.pbd.ui.screens.income

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pbd.data.local.AppDatabase
import com.example.pbd.data.model.Goal
import com.example.pbd.data.model.Transaction
import com.example.pbd.data.model.TransactionCategory
import com.example.pbd.data.model.TransactionType
import com.example.pbd.data.repository.FinanceRepository
import com.example.pbd.data.repository.GoalRepository
import com.example.pbd.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IncomeViewModel(
    private val repository: FinanceRepository,
    private val goalRepository: GoalRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    private companion object {
        const val TAG = "IncomeViewModel"
    }

    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    init {
        loadActiveGoal()
        loadUserProfilePreferences()
    }

    private fun loadActiveGoal() {
        viewModelScope.launch {
            goalRepository.getAllGoals().collect { goals ->
                val activeGoal = goals.firstOrNull { it.status == "ACTIVE" }
                _uiState.value = _uiState.value.copy(activeGoal = activeGoal)
            }
        }
    }

    private fun loadUserProfilePreferences() {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                    .get()
                    .addOnSuccessListener { doc ->
                        val user = doc.toObject(User::class.java)
                        if (user != null) {
                            _uiState.value = _uiState.value.copy(savingsPercentage = user.savingsPercentage)
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load user profile preferences", e)
            }
        }
    }

    fun fetchExchangeRate(amount: Double, currency: String) {
        Log.d(TAG, "fetchExchangeRate requestedCurrency=$currency, amount=$amount")

        if (amount <= 0.0) {
            _uiState.value = _uiState.value.copy(
                exchangeRate = 1.0,
                convertedAmountLKR = 0.0,
                isExchangeRateLoading = false,
                exchangeRateErrorMessage = "Enter a valid amount"
            )
            Log.d(TAG, "fetchExchangeRate invalidAmount state=${_uiState.value}")
            return
        }

        if (currency.uppercase() == "LKR") {
            _uiState.value = _uiState.value.copy(
                exchangeRate = 1.0,
                convertedAmountLKR = amount,
                isExchangeRateLoading = false,
                exchangeRateErrorMessage = null
            )
            Log.d(TAG, "fetchExchangeRate localCurrency state=${_uiState.value}")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExchangeRateLoading = true,
                exchangeRateErrorMessage = null
            )
            Log.d(TAG, "fetchExchangeRate loadingState=${_uiState.value}")

            val result = repository.getExchangeRate(currency)
            Log.d(TAG, "fetchExchangeRate repositoryResult=$result")

            result
                .onSuccess { rate ->
                    val convertedAmountLKR = amount * rate
                    _uiState.value = _uiState.value.copy(
                        exchangeRate = rate,
                        convertedAmountLKR = convertedAmountLKR,
                        isExchangeRateLoading = false,
                        exchangeRateErrorMessage = null
                    )
                    Log.d(
                        TAG,
                        "fetchExchangeRate success rate=$rate, convertedAmountLKR=$convertedAmountLKR, state=${_uiState.value}"
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        exchangeRate = 1.0,
                        convertedAmountLKR = 0.0,
                        isExchangeRateLoading = false,
                        exchangeRateErrorMessage = throwable.message ?: "Unable to fetch exchange rate"
                    )
                    Log.e(TAG, "fetchExchangeRate failure state=${_uiState.value}", throwable)
                }
        }
    }

    fun saveIncome(
        amount: Double,
        currency: String,
        category: TransactionCategory,
        subCategory: String = ""
    ) {
        if (amount <= 0.0) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Enter a valid amount"
            )
            return
        }

        viewModelScope.launch {
            val currentState = _uiState.value
            val resolvedExchangeRate = if (currency.uppercase() == "LKR") {
                1.0
            } else {
                currentState.exchangeRate
            }
            val resolvedBaseAmountLKR = if (currency.uppercase() == "LKR") {
                amount
            } else {
                currentState.convertedAmountLKR
            }

            if (currency.uppercase() != "LKR" && resolvedBaseAmountLKR <= 0.0) {
                _uiState.value = currentState.copy(
                    errorMessage = currentState.exchangeRateErrorMessage
                        ?: "Exchange rate is unavailable for $currency"
                )
                return@launch
            }

            _uiState.value = currentState.copy(
                isLoading = true,
                isSuccess = false,
                errorMessage = null
            )

            // Get the real Firebase Auth UID — the auth guard guarantees this is non-null
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "You must be logged in to save income."
                )
                return@launch
            }

            try {
                val incomeTransaction = Transaction(
                    userId = userId,
                    type = TransactionType.INCOME,
                    amount = amount,
                    currency = currency,
                    exchangeRate = resolvedExchangeRate,
                    baseAmountLKR = resolvedBaseAmountLKR,
                    category = category,
                    subCategory = subCategory
                )

                repository.saveIncome(incomeTransaction)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            errorMessage = null
                        )
                    }
                    .onFailure { throwable ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = false,
                            errorMessage = throwable.message ?: "Something went wrong"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = e.message ?: "Something went wrong"
                )
            }
        }
    }

    fun routeSavingsToGoal(amountLKR: Double, goal: Goal, percent: Int = 20) {
        val currentUserId = auth.currentUser?.uid ?: return
        val routedAmount = amountLKR * (percent / 100f)
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // 1. Transactionally update target goal currentSaved
                goalRepository.updateGoalSavedAmount(goal.id, routedAmount)

                // 2. Log a companion savings Transaction
                val companionTransaction = Transaction(
                    userId = currentUserId,
                    type = TransactionType.EXPENSE,
                    amount = routedAmount,
                    currency = "LKR",
                    exchangeRate = 1.0,
                    baseAmountLKR = routedAmount,
                    category = TransactionCategory.SAVINGS,
                    subCategory = "Savings",
                    note = "Routed ${percent}% of logged income to ${goal.title}"
                )
                repository.saveTransaction(companionTransaction)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRoutingSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Smart routing failed."
                )
            }
        }
    }

    fun resetState() {
        val activeGoal = _uiState.value.activeGoal
        _uiState.value = IncomeUiState(activeGoal = activeGoal)
    }
}
