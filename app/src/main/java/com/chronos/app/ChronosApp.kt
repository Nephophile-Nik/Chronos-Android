package com.chronos.app

import android.app.Application
import com.chronos.app.data.ChronosDatabase
import com.chronos.app.data.ChronosRepository
import com.chronos.app.data.SettingsStore
import com.chronos.app.send.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ChronosApp : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val db by lazy { ChronosDatabase.get(this) }
    val repo by lazy { ChronosRepository(this, db.dao()) }
    val settings by lazy { SettingsStore(this) }

    override fun onCreate() {
        super.onCreate()
        Notifier.ensureChannels(this)
        // Re-arm alarms on every process start (cheap, idempotent).
        appScope.launch { repo.rescheduleAll() }
    }
}
