package com.extremewakeup.soundalarm.repository

import androidx.lifecycle.LiveData
import com.extremewakeup.soundalarm.dao.AlarmDao
import com.extremewakeup.soundalarm.model.Alarm
import javax.inject.Inject

class AlarmRepository @Inject constructor(private val alarmDao: AlarmDao) {

    fun getAlarms(): LiveData<List<Alarm>> {
        return alarmDao.getAll()
    }

    suspend fun insertAlarm(alarm: Alarm) {
        alarmDao.insertAll(alarm)
    }

    // Add other necessary methods for update, delete, etc.
}
