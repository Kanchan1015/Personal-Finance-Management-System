package com.example.pbd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: TransactionType,
    val amount: Double,                   // The raw amount entered by the user
    val currency: String = "LKR",         // The currency of the raw amount
    val exchangeRate: Double = 1.0,       // Exchange rate used at the time of entry
    val baseAmountLKR: Double = 0.0,      // Amount converted to LKR (used by the dashboard)
    val category: TransactionCategory,    // Broad group: COMMITTED or DISCRETIONARY
    val subCategory: String = "",         // Specific label shown in UI: "Food", "Transport", etc.
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false         // Local-only flag: false = not yet sent to Firebase
)

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class TransactionCategory {
    // Income categories (used by Member 2's income screens)
    SALARY,
    FREELANCE,
    CRYPTO,
    // Expense categories (used by this screen)
    COMMITTED,
    DISCRETIONARY
}
