package com.example.pbd.ui.screens.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbd.data.model.Goal
import com.example.pbd.data.repository.GoalRepository
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
    val error: String? = null
)

class GoalDetailViewModel(private val repository: GoalRepository) : ViewModel() {

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

    private fun calculateAndUpdate(goal: Goal) {
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

        _uiState.value = _uiState.value.copy(
            goal = goal,
            progressPercent = progressPercent,
            monthsRemaining = monthsRemaining,
            monthlyTargetNeeded = monthlyTargetNeeded,
            isOnTrack = isOnTrack
        )
    }
}