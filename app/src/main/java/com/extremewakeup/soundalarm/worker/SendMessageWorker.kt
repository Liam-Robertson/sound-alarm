package com.extremewakeup.soundalarm.worker

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.extremewakeup.soundalarm.repository.AlarmRepository
import com.extremewakeup.soundalarm.viewmodel.BluetoothRepository
import com.extremewakeup.soundalarm.viewmodel.BluetoothService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SendMessageWorker @AssistedInject constructor(
    @Assisted private val bluetoothService: BluetoothService,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        TODO("Not yet implemented")
    }


}

    //    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
//        val alarmId = inputData.getInt("ALARM_ID", -1)
//
//        if (alarmId == -1) {
//            Log.e("RetrieveAlarmWorker", "Invalid Alarm ID")
//            return@withContext Result.failure()
//        }
//
//        try {
////            val alarm = alarmRepository.getAlarmById(alarmId)
//
//            if (alarm != null) {
////                bluetoothRepository.sendAlarmToESP32(alarm)
//                Result.success()
//            } else {
//                Log.e("RetrieveAlarmWorker", "Alarm not found")
//                Result.failure()
//            }
//        } catch (e: Exception) {
//            Log.e("RetrieveAlarmWorker", "Error fetching alarm volume", e)
//            Result.failure()
//        }
//    }



//
//@HiltWorker
//class SendMessageWorker @AssistedInject constructor(
//    @Assisted context: Context,
//    @Assisted params: WorkerParameters,
//    private val alarmRepository: AlarmRepository,
//    private val bluetoothRepository: BluetoothRepository
//) : CoroutineWorker(context, params) {
//
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
//        val alarmId = inputData.getInt("ALARM_ID", -1)
//
//        if (alarmId == -1) {
//            Log.e("RetrieveAlarmWorker", "Invalid Alarm ID")
//            return@withContext Result.failure()
//        }
//
//        try {
//            val alarm = alarmRepository.getAlarmById(alarmId)
//
//            if (alarm != null) {
//                bluetoothRepository.sendAlarmToESP32(alarm)
//                Result.success()
//            } else {
//                Log.e("RetrieveAlarmWorker", "Alarm not found")
//                Result.failure()
//            }
//        } catch (e: Exception) {
//            Log.e("RetrieveAlarmWorker", "Error fetching alarm volume", e)
//            Result.failure()
//        }
//    }
//
//}
