package com.extremewakeup.soundalarm.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.extremewakeup.soundalarm.model.Alarm

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarm")
    fun getAll(): LiveData<List<Alarm>>

    @Insert
    fun insertAll(vararg alarms: Alarm)

    @Update
    fun updateAlarm(alarm: Alarm)

    @Query("DELETE FROM Alarm WHERE id = :id")
    fun deleteById(id: Int)
}
