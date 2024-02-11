package com.extremewakeup.soundalarm.repository

import androidx.lifecycle.LiveData
import com.extremewakeup.soundalarm.dao.AlarmDao
import com.extremewakeup.soundalarm.model.Alarm
import javax.inject.Inject

class AlarmRepository @Inject constructor(private val alarmDao: AlarmDao) {

    suspend fun getAlarmList(): List<Alarm> {
        return alarmDao.getAll()
    }

    suspend fun insertAlarm(alarm: Alarm) {
        alarmDao.insert(alarm)
    }

    suspend fun getAlarmById(alarmId: Int): Alarm? {
        return alarmDao.getAlarmById(alarmId)
    }
}
