package com.chronos.app.send

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.chronos.app.ChronosApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Presses WhatsApp's Send button, but only when:
 *  1. Chronos just opened a chat (SendCoordinator.pending is set), and
 *  2. the composer actually contains the message we prefilled.
 * Anything else: do nothing and let the timeout report a failure.
 */
class WhatsAppAccessibilityService : AccessibilityService() {
    private var lastTry = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val p = SendCoordinator.pending ?: return
        val pkg = event?.packageName?.toString() ?: return
        if (pkg != p.pkg) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastTry < 600) return
        lastTry = now

        val root = rootInActiveWindow ?: return
        if (root.packageName?.toString() != p.pkg) return

        // Safety check: composer must contain our prefilled text.
        val composer = findComposer(root) ?: return
        val typed = composer.text?.toString().orEmpty()
        val probe = p.body.trim().take(20)
        if (probe.isNotEmpty() && !typed.contains(probe)) return

        val send = findSendButton(root, p.pkg) ?: return
        val app = applicationContext as ChronosApp
        val title = runBlocking(kotlinx.coroutines.Dispatchers.IO) { app.repo.get(p.id)?.title ?: "Message" }

        if (send.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            SendCoordinator.success(app, p.id, title)
            Handler(Looper.getMainLooper()).postDelayed(
                { performGlobalAction(GLOBAL_ACTION_HOME) }, 1500
            )
        }
    }

    private fun findComposer(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>().apply { add(root) }
        while (queue.isNotEmpty()) {
            val n = queue.removeFirst()
            if (n.className?.toString() == "android.widget.EditText") return n
            for (i in 0 until n.childCount) n.getChild(i)?.let(queue::add)
        }
        return null
    }

    private fun findSendButton(root: AccessibilityNodeInfo, pkg: String): AccessibilityNodeInfo? {
        root.findAccessibilityNodeInfosByViewId("$pkg:id/send")
            .firstOrNull { it.isEnabled }?.let { return it }
        return root.findAccessibilityNodeInfosByText("Send").firstOrNull {
            it.isEnabled && it.contentDescription?.toString().equals("Send", ignoreCase = true)
        }
    }

    override fun onInterrupt() {}
}
