package com.example.pbd.data.local

import androidx.room.TypeConverter
import com.example.pbd.data.model.TransactionCategory
import com.example.pbd.data.model.TransactionType

// Room cannot store enums directly, so these converters translate
// them to/from Strings for database storage.
class Converters {

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name // Stores as "INCOME" or "EXPENSE"
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        // Safe lookup: if an unknown value is found, default to EXPENSE
        return TransactionType.entries.find { it.name == value }
            ?: TransactionType.EXPENSE
    }

    @TypeConverter
    fun fromTransactionCategory(value: TransactionCategory): String {
        return value.name // Stores as "COMMITTED", "DISCRETIONARY", etc.
    }

    @TypeConverter
    fun toTransactionCategory(value: String): TransactionCategory {
        // Safe lookup: if an unknown value is found, default to DISCRETIONARY
        return TransactionCategory.entries.find { it.name == value }
            ?: TransactionCategory.DISCRETIONARY
    }
}

