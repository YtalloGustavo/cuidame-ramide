package com.example.medapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val doseId = intent.getStringExtra("doseId") ?: return
        val doseName = intent.getStringExtra("doseName") ?: "Medicamento"
        val doseDescription = intent.getStringExtra("doseDescription") ?: ""

        ensureChannel(context)

        // "Tomar" action
        val takeIntent = Intent(context, DoseActionReceiver::class.java).apply {
            action = "TAKE_DOSE"
            putExtra("doseId", doseId)
            putExtra("notificationId", doseId.hashCode())
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            context, doseId.hashCode() + 1, takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Adiar" action
        val postponeIntent = Intent(context, DoseActionReceiver::class.java).apply {
            action = "POSTPONE_DOSE"
            putExtra("doseId", doseId)
            putExtra("doseName", doseName)
            putExtra("doseDescription", doseDescription)
            putExtra("notificationId", doseId.hashCode())
        }
        val postponePendingIntent = PendingIntent.getBroadcast(
            context, doseId.hashCode() + 2, postponeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Hora de tomar: $doseName")
            .setContentText(doseDescription)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_edit, "Tomar", takePendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "Adiar 30min", postponePendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(doseId.hashCode(), notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lembretes de medicamento",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificacoes para lembrar de tomar medicamentos"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "medapp_reminders"
    }
}
