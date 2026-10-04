package com.chronos.app.send

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.chronos.app.ChronosApp
import com.chronos.app.data.ScheduledMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val WA_PACKAGES = listOf("com.whatsapp", "com.whatsapp.w4b")

/**
 * Holds the single in-flight send. The accessibility service only acts while [pending] is set,
 * so it can never press Send for anything Chronos did not just open itself.
 */
object SendCoordinator {
    data class Pending(val id: Long, val body: String, val pkg: String)

    @Volatile var pending: Pending? = null
        private set

    private val handler = Handler(Looper.getMainLooper())
    private var timeout: Runnable? = null
    private const val TIMEOUT_MS = 30_000L

    private fun installedWhatsApp(ctx: Context): String? = WA_PACKAGES.firstOrNull {
        try { ctx.packageManager.getPackageInfo(it, 0); true } catch (_: Exception) { false }
    }

    fun start(app: ChronosApp, m: ScheduledMessage, auto: Boolean) {
        val pkg = installedWhatsApp(app)
        if (pkg == null) { fail(app, m.id, m.title, "WhatsApp is not installed."); return }

        val digits = m.phone.filter { it.isDigit() }
        val manual = m.isGroup || digits.isEmpty()

        val intent = if (manual) {
            // No reliable deep link to a group: open WhatsApp's chat picker with the text prefilled.
            Intent(Intent.ACTION_SEND).setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, m.body).setPackage(pkg)
        } else {
            // The number in the link IS the recipient check: WhatsApp opens exactly that chat.
            Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits?text=${Uri.encode(m.body)}"))
                .setPackage(pkg)
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val willAuto = auto && !manual
        if (willAuto) {
            pending = Pending(m.id, m.body, pkg)
            timeout = Runnable {
                if (pending?.id == m.id) {
                    fail(app, m.id, m.title, "Timed out: couldn't confirm the chat or find the Send button.")
                }
            }.also { handler.postDelayed(it, TIMEOUT_MS) }
        }

        try {
            app.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            fail(app, m.id, m.title, "Couldn't open WhatsApp.")
            return
        }

        if (!willAuto) {
            val note = if (manual) "Opened WhatsApp: pick the chat and press Send yourself"
            else "Opened chat: press Send yourself"
            app.appScope.launch { app.repo.setStatus(m.id, note) }
        }
    }

    fun success(app: ChronosApp, id: Long, title: String) {
        clear()
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        app.appScope.launch {
            app.repo.setStatus(id, "Send tapped at $time (delivery not verifiable)")
            if (app.settings.notifySuccess.first()) {
                Notifier.status(app, id, "Chronos: sent", "\"$title\" was sent at $time.")
            }
        }
    }

    fun fail(app: ChronosApp, id: Long, title: String, reason: String) {
        clear()
        app.appScope.launch {
            app.repo.setStatus(id, "Failed: $reason")
            Notifier.status(app, id, "Chronos: needs manual action", "\"$title\": $reason Please send it manually.")
        }
    }

    private fun clear() {
        timeout?.let { handler.removeCallbacks(it) }
        timeout = null
        pending = null
    }
}
