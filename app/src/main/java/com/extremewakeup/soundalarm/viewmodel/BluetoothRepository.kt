package com.extremewakeup.soundalarm.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.extremewakeup.soundalarm.model.Alarm
import javax.inject.Inject

class BluetoothRepository @Inject constructor(private val bluetoothService: BluetoothService) {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun sendAlarmToESP32(alarm: Alarm) {
        Log.d("BluetoothRepository", "Preparing to send alarm data to ESP32")
        bluetoothService.startBleOperation(alarm)
    }

    // Any other Bluetooth operations the UI might need to trigger
}
