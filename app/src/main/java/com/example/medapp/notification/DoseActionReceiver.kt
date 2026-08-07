package com.example.medapp.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.medapp.data.local.DoseDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DoseActionReceiver : BroadcastReceiver() {

    @Inject lateinit var doseDao: DoseDao

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val doseId = intent.getStringExtra("doseId") ?: return
        val notificationId = intent.getIntExtra("notificationId", doseId.hashCode())

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    "TAKE_DOSE" -> {
                        doseDao.updateStatus(doseId, "Tomado")
                        cancelNotification(context, notificationId)
                    }
                    "POSTPONE_DOSE" -> {
                        val doseName = intent.getStringExtra("doseName") ?: "Medicamento"
                        val doseDescription = intent.getStringExtra("doseDescription") ?: ""
                        cancelNotification(context, notificationId)
                        reschedulePostponed(context, doseId, doseName, doseDescription)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun cancelNotification(context: Context, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }

    private fun reschedulePostponed(context: Context, doseId: String, doseName: String, doseDescription: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + 30 * 60 * 1000 // 30 minutes

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("doseId", doseId)
            putExtra("doseName", doseName)
            putExtra("doseDescription", doseDescription)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, doseId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }
}
