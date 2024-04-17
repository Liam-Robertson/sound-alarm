package com.extremewakeup.soundalarm.dependencyinjection

import android.app.AlarmManager
import android.content.Context
import androidx.room.Room
import com.extremewakeup.soundalarm.bluetooth.BluetoothManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.extremewakeup.soundalarm.database.AppDatabase
import com.extremewakeup.soundalarm.dao.AlarmDao
import com.extremewakeup.soundalarm.bluetooth.BluetoothRepository
import com.extremewakeup.soundalarm.bluetooth.BluetoothService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideBluetoothService(@ApplicationContext context: Context, bluetoothManager: BluetoothManager): BluetoothService {
        return BluetoothService(context, bluetoothManager)
    }

    @Singleton
    @Provides
    fun provideBluetoothRepository(bluetoothService: BluetoothService): BluetoothRepository {
        return BluetoothRepository(bluetoothService)
    }

    @Singleton
    @Provides
    fun provideBluetoothManager(@ApplicationContext context: Context): BluetoothManager {
        return BluetoothManager(context)
    }

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "sound_alarm_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideAlarmDao(database: AppDatabase): AlarmDao {
        return database.alarmDao()
    }

    @Singleton
    @Provides
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}
