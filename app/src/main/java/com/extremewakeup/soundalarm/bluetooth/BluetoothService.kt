package com.extremewakeup.soundalarm.bluetooth

import android.util.Log
import com.extremewakeup.soundalarm.model.Alarm
import com.extremewakeup.soundalarm.model.AlarmTiming
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BluetoothService @Inject constructor(
    private val bluetoothManager: BluetoothManager
) {

    fun bondWithDevice() {
        bluetoothManager.discoverAndBond()
    }

    fun disconnectFromEsp32() {
        bluetoothManager.disconnectDevice()
    }

    fun scanAndConnect() {
        bluetoothManager.scanAndConnect()
    }

    fun sendAlarmData(alarm: Alarm) {
        val alarmData = Json.encodeToString(MapSerializer(String.serializer(), AlarmTiming.serializer()), mapOf("startAlarm" to AlarmTiming(time = alarm.time, volume = alarm.volume))).toByteArray(Charsets.UTF_8)
        Log.d("BluetoothService", "BluetoothService: Sending alarm data")
        bluetoothManager.sendMessageToEsp32(alarmData)
    }

    fun sendStartAlarm(alarm: Alarm) {
        Log.d("BluetoothService", "BluetoothService: Sending start alarm command")
        bluetoothManager.sendMessageToEsp32("startAlarm".toByteArray())
    }

    fun sendStopAlarm() {
//        val alarmData = Json.encodeToString(MapSerializer(String.serializer(), String.serializer()), mapOf("stopAlarm" to "true")).toByteArray(Charsets.UTF_8)
        Log.d("BluetoothService", "BluetoothService: Sending stop alarm command")
        bluetoothManager.sendMessageToEsp32("stopAlarm".toByteArray())
    }

}
