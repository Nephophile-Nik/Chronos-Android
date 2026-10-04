package com.chronos.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RepeatType { ONCE, DAILY, DAYS }

@Entity(tableName = "scheduled_messages")
data class ScheduledMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val recipientName: String,
    /** Digits with country code, e.g. 919876543210. Empty for groups. */
    val phone: String = "",
    val isGroup: Boolean = false,
    val hour: Int,
    val minute: Int,
    /** LocalDate epoch-day; used only for ONCE. */
    val oneTimeDate: Long? = null,
    val repeatType: RepeatType = RepeatType.DAILY,
    /** Bit 0 = Monday ... bit 6 = Sunday; used only for DAYS. */
    val daysMask: Int = 0,
    val enabled: Boolean = true,
    val nextRunAt: Long? = null,
    val lastStatus: String = "Not run yet",
    val lastRunAt: Long? = null,
)
