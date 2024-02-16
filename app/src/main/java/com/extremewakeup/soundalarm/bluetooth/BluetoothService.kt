package com.extremewakeup.soundalarm.bluetooth

import android.content.Context
import android.util.Log
import com.extremewakeup.soundalarm.model.Alarm

class BluetoothService(private val context: Context) {
    private val bluetoothManager = BluetoothManager(context)
    var isConnected = false
    private var isScanningOrConnecting = false

    fun initiateConnection(onConnected: () -> Unit) {
        if (isScanningOrConnecting) {
            Log.d("BluetoothService", "initiateConnection: Already scanning or connecting")
            return
        }

        isScanningOrConnecting = true
        Log.d("BluetoothService", "bluetoothService: Starting device scan")
        bluetoothManager.scanForDevices { device ->
            Log.d("BluetoothService", "bluetoothService: Device found, attempting connection")
            bluetoothManager.connectToDevice(device) {
                Log.d("BluetoothService", "bluetoothService: Device connected, ready for commands")
                isConnected = true
                isScanningOrConnecting = false
                onConnected()
            }
        }
    }

    fun sendStartAlarm(alarm: Alarm) {
        if (isConnected) {
            Log.d("BluetoothService", "sendStartAlarm: Sending start alarm command")
            bluetoothManager.sendAlarmDataToESP32(alarm)
        } else if (!isConnected && isScanningOrConnecting) {
            Log.d("BluetoothService", "Bluetooth Service: is connecting")
            sendStartAlarm(alarm)
        }
        else {
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
