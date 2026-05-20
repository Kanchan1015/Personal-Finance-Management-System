package com.example.pbd.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbd.data.model.Goal
import com.example.pbd.data.model.Transaction
import com.example.pbd.data.model.TransactionType
import com.example.pbd.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netBalance: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val categoryBreakdown: Map<String, Double> = emptyMap(),
    val activeGoal: Goal? = null,
    val goalProgress: Float = 0f,
    val error: String? = null
)

class DashboardViewModel : ViewModel() {

    private val repository = DashboardRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
        loadGoals()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getTransactions().collect { transactions ->

                val income = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.baseAmountLKR }

                val expenses = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.baseAmountLKR }

                val categoryBreakdown = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.subCategory.ifEmpty { it.category.name } }
                    .mapValues { entry -> entry.value.sumOf { it.baseAmountLKR } }
                    .toList()
                    .sortedByDescending { it.second }
                    .toMap()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalIncome = income,
                    totalExpenses = expenses,
                    netBalance = income - expenses,
                    recentTransactions = transactions.take(5),
                    categoryBreakdown = categoryBreakdown,
                    error = null
                )
            }
        }
    }

    private fun loadGoals() {
        viewModelScope.launch {
            repository.getGoals().collect { goals ->
                val activeGoal = goals.firstOrNull()
                val progress = if (activeGoal != null && activeGoal.targetAmount > 0) {
                    (activeGoal.currentSaved / activeGoal.targetAmount).toFloat()
                        .coerceIn(0f, 1f)
                } else 0f

                _uiState.value = _uiState.value.copy(
                    activeGoal = activeGoal,
                    goalProgress = progress
                )
            }
        }
    }
}