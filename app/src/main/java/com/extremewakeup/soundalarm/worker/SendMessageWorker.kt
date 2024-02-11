package com.extremewakeup.soundalarm.worker

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.extremewakeup.soundalarm.repository.AlarmRepository
import com.extremewakeup.soundalarm.viewmodel.BluetoothRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SendMessageWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val alarmRepository: AlarmRepository,
    private val bluetoothRepository: BluetoothRepository
) : CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val alarmId = inputData.getInt("ALARM_ID", -1)

        if (alarmId == -1) {
            Log.e("RetrieveAlarmWorker", "Invalid Alarm ID")
            return@withContext Result.failure()
        }

        try {
            val alarm = alarmRepository.getAlarmById(alarmId)

            if (alarm != null) {
                bluetoothRepository.sendAlarmToESP32(alarm)
                Result.success()
            } else {
                Log.e("RetrieveAlarmWorker", "Alarm not found")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e("RetrieveAlarmWorker", "Error fetching alarm volume", e)
            Result.failure()
        }
    }

}
