package com.chronos.app.send

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.chronos.app.data.ScheduledMessage
import com.chronos.app.schedule.EXTRA_ID

const val EXTRA_AUTO = "auto_send"

object Notifier {
    private const val CH_DUE = "chronos_due"
    private const val CH_STATUS = "chronos_status"

    fun ensureChannels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CH_DUE, "Messages due", NotificationManager.IMPORTANCE_HIGH)
        )
        nm.createNotificationChannel(
            NotificationChannel(CH_STATUS, "Sending status", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    @SuppressLint("MissingPermission")
    private fun post(ctx: Context, id: Int, n: android.app.Notification) {
        val ok = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (ok) NotificationManagerCompat.from(ctx).notify(id, n)
    }

    fun due(ctx: Context, m: ScheduledMessage, auto: Boolean) {
        val canAuto = auto && !m.isGroup && m.phone.isNotBlank()
        val pi = PendingIntent.getActivity(
            ctx, m.id.toInt(),
            Intent(ctx, SendActivity::class.java)
                .putExtra(EXTRA_ID, m.id)
                .putExtra(EXTRA_AUTO, canAuto)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val b = NotificationCompat.Builder(ctx, CH_DUE)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Time to send: ${m.title}")
            .setContentText(
                if (canAuto) "Sending to ${m.recipientName}…"
                else "Tap to open the chat with ${m.recipientName}"
            )
            .setStyle(NotificationCompat.BigTextStyle().bigText(m.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pi)
        if (canAuto) b.setFullScreenIntent(pi, true)
        post(ctx, m.id.toInt(), b.build())
    }

    fun status(ctx: Context, id: Long, title: String, text: String) {
        val n = NotificationCompat.Builder(ctx, CH_STATUS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()
        post(ctx, (id + 1_000_000).toInt(), n)
    }
}
