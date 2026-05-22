package com.example.pbd.ui.screens.income

data class IncomeUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val exchangeRate: Double = 1.0,
    val convertedAmountLKR: Double = 0.0,
    val isExchangeRateLoading: Boolean = false,
    val exchangeRateErrorMessage: String? = null
)
