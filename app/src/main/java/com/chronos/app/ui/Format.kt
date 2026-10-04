package com.chronos.app.ui

import com.chronos.app.data.RepeatType
import com.chronos.app.data.ScheduledMessage
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val DAY_NAMES = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

fun timeText(h: Int, m: Int) = "%02d:%02d".format(h, m)

fun scheduleText(m: ScheduledMessage): String = when (m.repeatType) {
    RepeatType.ONCE -> {
        val d = m.oneTimeDate?.let { LocalDate.ofEpochDay(it) }
        "Once on ${d?.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())) ?: "?"}, ${timeText(m.hour, m.minute)}"
    }
    RepeatType.DAILY -> "Every day at ${timeText(m.hour, m.minute)}"
    RepeatType.DAYS -> DAY_NAMES.filterIndexed { i, _ -> (m.daysMask shr i) and 1 == 1 }
        .joinToString(", ") + " at ${timeText(m.hour, m.minute)}"
}

fun nextText(m: ScheduledMessage): String {
    if (!m.enabled) return "Paused"
    val t = m.nextRunAt ?: return "No upcoming run"
    return Instant.ofEpochMilli(t).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEE, dd MMM, HH:mm", Locale.getDefault()))
}
