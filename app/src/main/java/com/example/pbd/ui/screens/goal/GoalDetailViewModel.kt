package com.example.pbd.ui.screens.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbd.data.model.Goal
import com.example.pbd.data.model.Transaction
import com.example.pbd.data.model.TransactionCategory
import com.example.pbd.data.model.TransactionType
import com.example.pbd.data.repository.FinanceRepository
import com.example.pbd.data.repository.GoalRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class GoalDetailUiState(
    val isLoading: Boolean = true,
    val goals: List<Goal> = emptyList(),
    val goal: Goal? = null,
    val progressPercent: Int = 0,
    val monthsRemaining: Long = 0,
    val monthlyTargetNeeded: Double = 0.0,
    val isOnTrack: Boolean = false,
    val error: String? = null,
    val discretionarySpend30Days: Double = 0.0,
    val daysSavedWith20PercentRedirect: Int = 0
)

class GoalDetailViewModel(
    private val repository: GoalRepository,
    private val financeRepository: FinanceRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalDetailUiState())
    val uiState: StateFlow<GoalDetailUiState> = _uiState.asStateFlow()

    fun loadGoal(goalId: String) {
        viewModelScope.launch {
            repository.getAllGoals().collect { goals ->
                if (goals.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        goals = emptyList(),
                        goal = null,
                        error = "No active goal found"
                    )
                } else {
                    val primaryGoal = if (goalId == "active" || goalId.isEmpty()) {
                        goals.first()
                    } else {
                        goals.find { it.id == goalId } ?: goals.first()
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        goals = goals,
                        error = null
                    )
                    calculateAndUpdate(primaryGoal)
                }
            }
        }
    }

    fun addGoal(title: String, targetAmount: Double, months: Int) {
        viewModelScope.launch {
            repository.addGoal(title, targetAmount, months)
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    fun boostGoal(goalId: String, amount: Double, goalTitle: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // 1. Transactionally update target goal currentSaved
                repository.updateGoalSavedAmount(goalId, amount)

                // 2. Log a companion savings Transaction
                val companionTransaction = Transaction(
                    userId = currentUserId,
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    currency = "LKR",
                    exchangeRate = 1.0,
                    baseAmountLKR = amount,
                    category = TransactionCategory.SAVINGS,
                    subCategory = "Savings",
                    note = "Saved toward: $goalTitle"
                )
                financeRepository.saveTransaction(companionTransaction)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateAndUpdate(goal: Goal) {
        viewModelScope.launch {
            // Collect transactions to compute discretionary spending
            financeRepository.allTransactions.collect { transactions ->
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                
                // Discretionary spending in the last 30 days
                val discSpend = transactions
                    .filter { it.timestamp >= thirtyDaysAgo && it.type == TransactionType.EXPENSE && it.category == TransactionCategory.DISCRETIONARY }
                    .sumOf { it.baseAmountLKR }

                val progressPercent = if (goal.targetAmount > 0) {
                    ((goal.currentSaved / goal.targetAmount) * 100).toInt().coerceIn(0, 100)
                } else 0

                val monthsRemaining = if (goal.deadline > 0) {
                    val diff = goal.deadline - System.currentTimeMillis()
                    if (diff > 0) TimeUnit.MILLISECONDS.toDays(diff) / 30 else 0L
                } else 0L

                val amountRemaining = goal.targetAmount - goal.currentSaved
                val monthlyTargetNeeded = if (monthsRemaining > 0) {
                    amountRemaining / monthsRemaining
                } else amountRemaining

                val isOnTrack = progressPercent > 0 && monthsRemaining > 0

                // If user redirected 20% of their monthly discretionary spending
                val monthlyRedirect = discSpend * 0.20
                val daysSaved = if (monthlyTargetNeeded > 0 && monthlyRedirect > 0) {
                    ((monthlyRedirect / monthlyTargetNeeded) * 30).toInt().coerceIn(1, 365)
                } else 0

                _uiState.value = _uiState.value.copy(
                    goal = goal,
                    progressPercent = progressPercent,
                    monthsRemaining = monthsRemaining,
                    monthlyTargetNeeded = monthlyTargetNeeded,
                    isOnTrack = isOnTrack,
                    discretionarySpend30Days = discSpend,
                    daysSavedWith20PercentRedirect = daysSaved
                )
            }
        }
    }
}