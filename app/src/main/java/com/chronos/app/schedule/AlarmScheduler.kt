package com.chronos.app.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

const val EXTRA_ID = "message_id"

class AlarmScheduler(private val context: Context) {
    private val am = context.getSystemService(AlarmManager::class.java)

    private fun pending(id: Long): PendingIntent = PendingIntent.getBroadcast(
        context,
        id.toInt(),
        Intent(context, AlarmReceiver::class.java).putExtra(EXTRA_ID, id),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    fun schedule(id: Long, atMillis: Long) {
        cancel(id)
        val exactAllowed = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
        if (exactAllowed) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pending(id))
        } else {
            // Falls back to a few-minutes-late alarm until the user grants "Alarms & reminders".
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pending(id))
        }
    }

    fun cancel(id: Long) = am.cancel(pending(id))
}
