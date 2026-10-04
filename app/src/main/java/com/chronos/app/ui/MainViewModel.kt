package com.chronos.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chronos.app.ChronosApp
import com.chronos.app.data.ScheduledMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as ChronosApp

    val messages = app.repo.messages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val autoSend = app.settings.autoSend
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val notifySuccess = app.settings.notifySuccess
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun save(m: ScheduledMessage) = viewModelScope.launch { app.repo.save(m) }
    fun setEnabled(m: ScheduledMessage, on: Boolean) = viewModelScope.launch { app.repo.setEnabled(m, on) }
    fun delete(m: ScheduledMessage) = viewModelScope.launch { app.repo.delete(m) }
    fun setAutoSend(v: Boolean) = viewModelScope.launch { app.settings.setAutoSend(v) }
    fun setNotifySuccess(v: Boolean) = viewModelScope.launch { app.settings.setNotifySuccess(v) }
}
