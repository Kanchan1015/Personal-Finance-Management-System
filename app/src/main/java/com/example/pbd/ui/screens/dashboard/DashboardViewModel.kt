package com.example.pbd.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbd.data.model.Goal
import com.example.pbd.data.model.Transaction
import com.example.pbd.data.model.TransactionCategory
import com.example.pbd.data.model.TransactionType
import com.example.pbd.data.repository.DashboardRepository
import com.example.pbd.data.repository.FinanceRepository
import com.example.pbd.data.repository.GoalRepository
import com.google.firebase.auth.FirebaseAuth
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
    val error: String? = null,
    val userName: String = "User"
)

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val repository: DashboardRepository,
    private val goalRepository: GoalRepository,
    private val financeRepository: FinanceRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
        loadGoals()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val name = dashboardRepository.getUserName()
            _uiState.value = _uiState.value.copy(userName = name)
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: ""
            financeRepository.allTransactions.collect { allTransactions ->
                val transactions = allTransactions.filter { it.userId == currentUserId }

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
            dashboardRepository.getGoals().collect { goals ->
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

    fun boostActiveGoal(amount: Double) {
        val activeGoal = _uiState.value.activeGoal ?: return
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // 1. Transactionally update the goal saved amount in Firestore
                goalRepository.updateGoalSavedAmount(activeGoal.id, amount)

                // 2. Save a companion Transaction
                val companionTransaction = Transaction(
                    userId = currentUserId,
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    currency = "LKR",
                    exchangeRate = 1.0,
                    baseAmountLKR = amount,
                    category = TransactionCategory.SAVINGS,
                    subCategory = "Savings",
                    note = "Saved toward: ${activeGoal.title}"
                )
                financeRepository.saveTransaction(companionTransaction)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}