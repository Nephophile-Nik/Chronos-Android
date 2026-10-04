package com.chronos.app.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chronos.app.ChronosApp
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as ChronosApp
        val pending = goAsync()
        app.appScope.launch {
            try { app.repo.rescheduleAll() } finally { pending.finish() }
        }
    }
}
