package com.example.pbd.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val baseCurrency: String = "LKR",
    val totalBalanceLKR: Double = 0.0
)
