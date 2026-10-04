package com.chronos.app.schedule

import com.chronos.app.data.RepeatType
import com.chronos.app.data.ScheduledMessage
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

object NextRun {
    /** Epoch millis of the next occurrence strictly after [now], or null if there is none. */
    fun compute(m: ScheduledMessage, now: ZonedDateTime = ZonedDateTime.now()): Long? {
        val zone = now.zone
        val time = LocalTime.of(m.hour, m.minute)
        when (m.repeatType) {
            RepeatType.ONCE -> {
                val date = m.oneTimeDate?.let { LocalDate.ofEpochDay(it) } ?: return null
                val at = ZonedDateTime.of(date, time, zone)
                return if (at.isAfter(now)) at.toInstant().toEpochMilli() else null
            }
            RepeatType.DAILY -> {
                var at = ZonedDateTime.of(now.toLocalDate(), time, zone)
                if (!at.isAfter(now)) at = at.plusDays(1)
                return at.toInstant().toEpochMilli()
            }
            RepeatType.DAYS -> {
                if (m.daysMask == 0) return null
                for (i in 0..7) {
                    val d = now.toLocalDate().plusDays(i.toLong())
                    if ((m.daysMask shr (d.dayOfWeek.value - 1)) and 1 == 1) {
                        val at = ZonedDateTime.of(d, time, zone)
                        if (at.isAfter(now)) return at.toInstant().toEpochMilli()
                    }
                }
                return null
            }
        }
    }
}
