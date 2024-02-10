package com.extremewakeup.soundalarm.viewmodel

import com.extremewakeup.soundalarm.model.Alarm
import javax.inject.Inject

class BluetoothRepository @Inject constructor(private val bluetoothService: BluetoothService) {

    fun sendAlarmToESP32(alarm: Alarm) {
        bluetoothService.sendAlarmDataToESP32(alarm)
    }

    // Any other Bluetooth operations the UI might need to trigger
}
