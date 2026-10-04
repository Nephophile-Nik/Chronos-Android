package com.chronos.app.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chronos.app.ChronosApp
import com.chronos.app.send.Notifier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_ID, -1L)
        if (id < 0) return
        val app = context.applicationContext as ChronosApp
        val pending = goAsync()
        app.appScope.launch {
            try {
                val m = app.repo.onFired(id) ?: return@launch
                val auto = app.settings.autoSend.first()
                Notifier.due(app, m, auto)
            } finally {
                pending.finish()
            }
        }
    }
}
