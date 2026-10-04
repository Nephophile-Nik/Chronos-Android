package com.chronos.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM scheduled_messages ORDER BY id DESC")
    fun observeAll(): Flow<List<ScheduledMessage>>

    @Query("SELECT * FROM scheduled_messages WHERE id = :id")
    suspend fun getById(id: Long): ScheduledMessage?

    @Query("SELECT * FROM scheduled_messages WHERE enabled = 1")
    suspend fun getEnabled(): List<ScheduledMessage>

    @Insert suspend fun insert(m: ScheduledMessage): Long
    @Update suspend fun update(m: ScheduledMessage)
    @Delete suspend fun delete(m: ScheduledMessage)

    @Query("UPDATE scheduled_messages SET lastStatus = :status, lastRunAt = :at WHERE id = :id")
    suspend fun setStatus(id: Long, status: String, at: Long)
}
