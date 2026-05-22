package com.example.pbd.ui.screens.income

data class IncomeUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
