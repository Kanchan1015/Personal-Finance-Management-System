package com.example.pbd.data.notification

import android.content.Context
import android.content.SharedPreferences

/**
 * Thin SharedPreferences wrapper for user-configurable notification settings.
 * All thresholds have sensible defaults so the feature works out-of-the-box.
 */
class NotificationPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "notification_prefs"

        private const val KEY_BUDGET_THRESHOLD       = "budget_threshold"
        private const val KEY_LARGE_TX_THRESHOLD     = "large_tx_threshold"
        private const val KEY_DAILY_SUMMARY_HOUR     = "daily_summary_hour"
        private const val KEY_DAILY_SUMMARY_MINUTE   = "daily_summary_minute"
        private const val KEY_NOTIFICATIONS_ENABLED  = "notifications_enabled"

        // Defaults per user decision:
        const val DEFAULT_BUDGET_THRESHOLD   = 50_000.0   // LKR 50,000/month
        const val DEFAULT_LARGE_TX_THRESHOLD = 5_000.0    // LKR 5,000 per transaction
        const val DEFAULT_DAILY_HOUR         = 22          // 10 PM
        const val DEFAULT_DAILY_MINUTE       = 0
    }

    var budgetThreshold: Double
        get() = java.lang.Double.longBitsToDouble(
            prefs.getLong(KEY_BUDGET_THRESHOLD, java.lang.Double.doubleToLongBits(DEFAULT_BUDGET_THRESHOLD))
        )
        set(value) = prefs.edit().putLong(KEY_BUDGET_THRESHOLD, java.lang.Double.doubleToLongBits(value)).apply()

    var largeTransactionThreshold: Double
        get() = java.lang.Double.longBitsToDouble(
            prefs.getLong(KEY_LARGE_TX_THRESHOLD, java.lang.Double.doubleToLongBits(DEFAULT_LARGE_TX_THRESHOLD))
        )
        set(value) = prefs.edit().putLong(KEY_LARGE_TX_THRESHOLD, java.lang.Double.doubleToLongBits(value)).apply()

    /** Hour (0–23) for the daily spending summary alarm. */
    var dailySummaryHour: Int
        get() = prefs.getInt(KEY_DAILY_SUMMARY_HOUR, DEFAULT_DAILY_HOUR)
        set(value) = prefs.edit().putInt(KEY_DAILY_SUMMARY_HOUR, value).apply()

    /** Minute (0–59) for the daily spending summary alarm. */
    var dailySummaryMinute: Int
        get() = prefs.getInt(KEY_DAILY_SUMMARY_MINUTE, DEFAULT_DAILY_MINUTE)
        set(value) = prefs.edit().putInt(KEY_DAILY_SUMMARY_MINUTE, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()
}
