package com.example.medapp.notification

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
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var doseDao: DoseDao

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON" &&
            intent.action != "com.htc.intent.action.QUICKBOOT_POWERON"
        ) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val doses = doseDao.getAllOnce()
                doses.forEach { dose ->
                    ReminderScheduler.scheduleReminder(
                        context = context,
                        doseId = dose.id,
                        doseName = dose.name,
                        doseDescription = dose.doseDescription,
                        timeString = dose.time
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}