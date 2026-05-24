package com.example.pbd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: Double = 0.0,
    val currency: String = "LKR",
    val exchangeRate: Double = 1.0,
    val baseAmountLKR: Double = 0.0,
    val category: TransactionCategory = TransactionCategory.DISCRETIONARY,
    val subCategory: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class TransactionCategory {
    SALARY,
    FREELANCE,
    CRYPTO,
    COMMITTED,
    DISCRETIONARY,
    SAVINGS
}
