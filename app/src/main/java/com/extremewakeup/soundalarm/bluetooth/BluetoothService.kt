package com.extremewakeup.soundalarm.bluetooth

import android.content.Context
import android.util.Log
import com.extremewakeup.soundalarm.model.Alarm

class BluetoothService(private val context: Context) {
    private val bluetoothManager = BluetoothManager(context)
    private var isConnected = false

    fun initiateConnection(onConnected: () -> Unit) {
        Log.d("BluetoothService", "initiateConnection: Starting device scan")
        bluetoothManager.scanForDevices { device ->
            Log.d("BluetoothService", "initiateConnection: Device found, attempting connection")
            bluetoothManager.connectToDevice(device) {
                Log.d("BluetoothService", "initiateConnection: Device connected, ready for commands")
                isConnected = true
                onConnected()
            }
        }
    }

    fun sendStartAlarm(alarm: Alarm) {
        if (isConnected) {
            Log.d("BluetoothService", "sendStartAlarm: Sending start alarm command")
            bluetoothManager.sendAlarmDataToESP32(alarm)
        } else {
            Log.d("BluetoothService", "sendStartAlarm: Device not connected, waiting for connection")
            initiateConnection {
                sendStartAlarm(alarm)
            }
        }
    }

//    fun sendStopAlarm() {
//        Log.d("BluetoothService", "sendStopAlarm: Sending stop alarm command")
//        bluetoothManager.stopPlayingAlarm()
//    }

    fun disconnect() {
        Log.d("BluetoothService", "disconnect: Disconnecting from device")
        bluetoothManager.disconnectFromDevice()
    }
}
