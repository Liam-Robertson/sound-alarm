package com.extremewakeup.soundalarm.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import com.extremewakeup.soundalarm.model.Alarm
import javax.inject.Inject

class BluetoothRepository @Inject constructor(private val bluetoothService: BluetoothService) {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun sendAlarmToESP32(alarm: Alarm) {
        bluetoothService.sendAlarmDataToESP32(alarm)
    }

    // Any other Bluetooth operations the UI might need to trigger
}
