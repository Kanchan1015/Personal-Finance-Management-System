package com.example.pbd.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pbd.data.model.RecurringExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpense(recurringExpense: RecurringExpense)

    @Query("SELECT * FROM recurring_expenses WHERE isEnabled = 1")
    suspend fun getActiveRecurringExpenses(): List<RecurringExpense>

    @Query("UPDATE recurring_expenses SET nextExecutionDate = :nextDate WHERE id = :id")
    suspend fun updateNextExecutionDate(id: String, nextDate: Long)

    @Query("SELECT * FROM recurring_expenses")
    fun getAllRecurringExpenses(): Flow<List<RecurringExpense>>
}
