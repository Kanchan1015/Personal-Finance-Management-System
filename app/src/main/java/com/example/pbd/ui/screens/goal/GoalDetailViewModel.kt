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
    val goal: Goal? = null,
    val progressPercent: Int = 0,
    val monthsRemaining: Long = 0,
    val monthlyTargetNeeded: Double = 0.0,
    val isOnTrack: Boolean = false,
    val error: String? = null
)

class GoalDetailViewModel : ViewModel() {

    private val repository = GoalRepository()

    private val _uiState = MutableStateFlow(GoalDetailUiState())
    val uiState: StateFlow<GoalDetailUiState> = _uiState.asStateFlow()

    fun loadGoal(goalId: String) {
        viewModelScope.launch {
            if (goalId.isEmpty()) {
                repository.getAllGoals().collect { goals ->
                    val goal = goals.firstOrNull()
                    if (goal != null) {
                        calculateAndUpdate(goal)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "No active goal found"
                        )
                    }
                }
            } else {
                repository.getGoalById(goalId).collect { goal ->
                    if (goal != null) {
                        calculateAndUpdate(goal)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Goal not found"
                        )
                    }
                }
            }
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

        // On track if they've saved at least what they should have by now
        val totalMonths = if (goal.deadline > 0) {
            val totalDiff = goal.deadline - (goal.id.hashCode().toLong())
            (TimeUnit.MILLISECONDS.toDays(
                goal.deadline - System.currentTimeMillis()
            ) / 30).coerceAtLeast(0)
        } else 0L

        val isOnTrack = progressPercent > 0 && monthsRemaining > 0

        _uiState.value = GoalDetailUiState(
            isLoading = false,
            goal = goal,
            progressPercent = progressPercent,
            monthsRemaining = monthsRemaining,
            monthlyTargetNeeded = monthlyTargetNeeded,
            isOnTrack = isOnTrack,
            error = null
        )
    }
}