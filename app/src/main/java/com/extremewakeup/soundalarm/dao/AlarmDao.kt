package com.extremewakeup.soundalarm.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.extremewakeup.soundalarm.model.Alarm

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarm")
    suspend fun getAll(): List<Alarm>

    @Insert
    suspend fun insert(alarm: Alarm): Long

    @Query("SELECT * FROM alarm WHERE id = :alarmId")
    suspend fun getAlarmById(alarmId: Int): Alarm?
}
