package com.chronos.app.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ChronosTheme { ChronosRoot(vm) } }
    }
}

@Composable
fun ChronosRoot(vm: MainViewModel) {
    var screen by rememberSaveable { mutableStateOf("home") }
    var editingId by rememberSaveable { mutableLongStateOf(0L) }
    val messages by vm.messages.collectAsState()

    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    BackHandler(enabled = screen != "home") { screen = "home" }

    when (screen) {
        "edit" -> EditorScreen(
            existing = messages.firstOrNull { it.id == editingId },
            onSave = { vm.save(it); screen = "home" },
            onBack = { screen = "home" },
        )
        "settings" -> SettingsScreen(vm, onBack = { screen = "home" })
        else -> DashboardScreen(
            messages = messages,
            onAdd = { editingId = 0L; screen = "edit" },
            onEdit = { editingId = it.id; screen = "edit" },
            onToggle = { m, on -> vm.setEnabled(m, on) },
            onDelete = { vm.delete(it) },
            onSettings = { screen = "settings" },
        )
    }
}
