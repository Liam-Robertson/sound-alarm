package com.extremewakeup.soundalarm.worker

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.extremewakeup.soundalarm.bluetooth.BluetoothService
import com.extremewakeup.soundalarm.repository.AlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltWorker
class SendMessageWorker @Inject constructor(
    context: Context,
    params: WorkerParameters,
    private val alarmRepository: AlarmRepository,
    private val bluetoothService: BluetoothService
) : CoroutineWorker(context, params) {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val alarmId = inputData.getInt("ALARM_ID", -1)
        if (alarmId == -1) {
            Log.e("SendMessageWorker", "Invalid Alarm ID")
            return@withContext Result.failure()
        }

        try {
            val alarm = alarmRepository.getAlarmById(alarmId)
            if (alarm != null) {
                Log.d("SendMessageWorker", "Worker sending alarm to ESP32: $alarm")
                bluetoothService.sendAlarmData(alarm)
                Result.success()
            } else {
                Log.e("SendMessageWorker", "Alarm not found")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e("SendMessageWorker", "Error sending alarm to ESP32", e)
            Result.failure()
        }
    }
}
