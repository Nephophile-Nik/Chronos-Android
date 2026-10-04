package com.chronos.app.data

import android.content.Context
import com.chronos.app.schedule.AlarmScheduler
import com.chronos.app.schedule.NextRun

class ChronosRepository(context: Context, private val dao: MessageDao) {
    private val scheduler = AlarmScheduler(context.applicationContext)

    val messages = dao.observeAll()

    suspend fun get(id: Long) = dao.getById(id)

    /** Insert or update, recompute next run time and (re)arm the alarm. */
    suspend fun save(m: ScheduledMessage) {
        val next = if (m.enabled) NextRun.compute(m) else null
        val id = if (m.id == 0L) dao.insert(m.copy(nextRunAt = next))
        else { dao.update(m.copy(nextRunAt = next)); m.id }
        if (next != null) scheduler.schedule(id, next) else scheduler.cancel(id)
    }

    suspend fun setEnabled(m: ScheduledMessage, on: Boolean) = save(m.copy(enabled = on))

    suspend fun delete(m: ScheduledMessage) {
        scheduler.cancel(m.id)
        dao.delete(m)
    }

    suspend fun setStatus(id: Long, status: String) =
        dao.setStatus(id, status, System.currentTimeMillis())

    /** Called when an alarm fires. Re-arms the next occurrence and returns the message to send. */
    suspend fun onFired(id: Long): ScheduledMessage? {
        val m = dao.getById(id) ?: return null
        if (!m.enabled) return null
        val once = m.repeatType == RepeatType.ONCE
        val next = if (once) null else NextRun.compute(m)
        dao.update(
            m.copy(
                nextRunAt = next,
                enabled = !once && next != null,
                lastStatus = "Due, waiting to send",
                lastRunAt = System.currentTimeMillis(),
            )
        )
        if (next != null) scheduler.schedule(id, next)
        return m
    }

    /** After boot / app update / timezone change: re-arm everything, flag missed one-time messages. */
    suspend fun rescheduleAll() {
        dao.getEnabled().forEach { m ->
            val next = NextRun.compute(m)
            if (next == null) {
                dao.update(m.copy(enabled = false, nextRunAt = null, lastStatus = "Missed (device was off)"))
            } else {
                dao.update(m.copy(nextRunAt = next))
                scheduler.schedule(m.id, next)
            }
        }
    }
}
