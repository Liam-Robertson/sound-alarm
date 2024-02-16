package com.extremewakeup.soundalarm.bluetooth

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.extremewakeup.soundalarm.model.Alarm
import javax.inject.Inject

class BluetoothRepository @Inject constructor(private val bluetoothService: BluetoothService) {

    fun sendAlarmToESP32(alarm: Alarm) {
        Log.d("BluetoothRepository", "Bluetooth Repository: about to send start alarm")
        bluetoothService.sendStartAlarm(alarm)
    }

//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    fun stopAlarmPlaying() {
//        Log.d("BluetoothRepository", "stopAlarmPlaying: Preparing to stop alarm on ESP32")
//        bluetoothService.sendStopAlarm()
//    }
}