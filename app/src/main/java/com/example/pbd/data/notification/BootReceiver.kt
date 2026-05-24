package com.example.pbd.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Listens for BOOT_COMPLETED so we can re-schedule the daily AlarmManager alarm.
 *
 * AlarmManager alarms are cleared when the device reboots, so this receiver
 * ensures the daily spending summary is always scheduled after a restart.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            DailyReminderReceiver.scheduleNextAlarm(context)
        }
    }
}
