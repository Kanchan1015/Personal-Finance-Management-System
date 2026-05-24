package com.example.pbd.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pbd.data.model.Transaction
import com.example.pbd.data.model.RecurringExpense
import com.example.pbd.data.model.NotificationEntity

// Version must be bumped every time the database schema changes (e.g., adding new columns).
// v1 → v2: added 'exchangeRate' and 'baseAmountLKR'
// v2 → v3: added 'subCategory' for UI display labels
// v3 → v4: added 'recurring_expenses' table
// v4 → v5: added 'notifications' table for in-app Notification Center
@Database(
    entities = [Transaction::class, RecurringExpense::class, NotificationEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_database"
                )
                // During development, if the schema changes, just wipe and recreate.
                // In a production app with real users, you would write a proper Migration instead.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
