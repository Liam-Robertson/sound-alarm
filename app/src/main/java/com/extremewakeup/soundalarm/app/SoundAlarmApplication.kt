package com.extremewakeup.soundalarm.app

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.extremewakeup.soundalarm.repository.AlarmRepository
import com.extremewakeup.soundalarm.bluetooth.BluetoothRepository
import com.extremewakeup.soundalarm.worker.SendMessageWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SoundAlarmApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: CustomWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(workerFactory)
            .build()
}

class CustomWorkerFactory @Inject constructor(private val bluetoothRepository: BluetoothRepository, private val alarmRepository: AlarmRepository) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = SendMessageWorker(appContext, workerParameters, alarmRepository, bluetoothRepository)
}