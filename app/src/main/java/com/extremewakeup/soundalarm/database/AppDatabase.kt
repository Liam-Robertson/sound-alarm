package com.extremewakeup.soundalarm.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.extremewakeup.soundalarm.dao.AlarmDao
import com.extremewakeup.soundalarm.model.Alarm

@Database(entities = [Alarm::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}
