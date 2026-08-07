package com.example.medapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object ReminderScheduler {

    fun scheduleReminder(
        context: Context,
        doseId: String,
        doseName: String,
        doseDescription: String,
        timeString: String
    ) {
        val (hour, minute) = parseTime(timeString) ?: return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("doseId", doseId)
            putExtra("doseName", doseName)
            putExtra("doseDescription", doseDescription)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            doseId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancelReminder(context: Context, doseId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            doseId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun parseTime(timeString: String): Pair<Int, Int>? {
        return try {
            val cleaned = timeString.trim()
            if (cleaned.contains(":")) {
                val parts = cleaned.split(":")
                Pair(parts[0].toInt(), parts[1].toInt())
            } else if (cleaned.length == 4) {
                Pair(cleaned.substring(0, 2).toInt(), cleaned.substring(2, 4).toInt())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
