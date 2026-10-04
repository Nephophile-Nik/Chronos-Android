package com.chronos.app.send

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.chronos.app.ChronosApp
import com.chronos.app.schedule.EXTRA_ID
import kotlinx.coroutines.launch

/** Invisible launcher: loads the message and opens WhatsApp. */
class SendActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getLongExtra(EXTRA_ID, -1L)
        val auto = intent.getBooleanExtra(EXTRA_AUTO, false)
        val app = application as ChronosApp
        lifecycleScope.launch {
            val m = if (id >= 0) app.repo.get(id) else null
            if (m != null) {
                NotificationManagerCompat.from(app).cancel(id.toInt())
                SendCoordinator.start(app, m, auto)
            }
            finish()
        }
    }
}
