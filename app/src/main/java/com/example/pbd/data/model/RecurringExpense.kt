package com.example.pbd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "recurring_expenses")
data class RecurringExpense(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val amount: Double,
    val category: TransactionCategory,
    val subCategory: String,
    val note: String,
    val interval: String,           // "WEEKLY" or "MONTHLY"
    val nextExecutionDate: Long,    // Timestamp of the next auto-log date
    val isEnabled: Boolean = true
)
