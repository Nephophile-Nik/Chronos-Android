package com.chronos.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("chronos_settings")

class SettingsStore(context: Context) {
    private val ds = context.applicationContext.dataStore
    private val autoKey = booleanPreferencesKey("auto_send")
    private val successKey = booleanPreferencesKey("notify_success")

    /** false = confirmation-based (safer default). */
    val autoSend: Flow<Boolean> = ds.data.map { it[autoKey] ?: false }
    val notifySuccess: Flow<Boolean> = ds.data.map { it[successKey] ?: true }

    suspend fun setAutoSend(v: Boolean) { ds.edit { it[autoKey] = v } }
    suspend fun setNotifySuccess(v: Boolean) { ds.edit { it[successKey] = v } }
}
