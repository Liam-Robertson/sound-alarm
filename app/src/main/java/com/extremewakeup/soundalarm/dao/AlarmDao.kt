package com.extremewakeup.soundalarm.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.extremewakeup.soundalarm.model.Alarm

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarm")
    fun getAll(): LiveData<List<Alarm>>

    @Insert
    fun insert(alarm: Alarm): Long

}
