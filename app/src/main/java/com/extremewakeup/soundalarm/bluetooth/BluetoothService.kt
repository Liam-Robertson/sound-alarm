package com.extremewakeup.soundalarm.bluetooth

import android.content.Context
import android.util.Log
import com.extremewakeup.soundalarm.model.Alarm
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

class BluetoothService @Inject constructor(private val bluetoothManager: BluetoothManager) {

    private val connectionMutex = Mutex()
    var isConnected = false
    private var isScanningOrConnecting = false

    private var lastConnectionAttemptTime = 0L
    private val connectionAttemptDelay = 2000L

    private fun canAttemptConnection(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastConnectionAttemptTime > connectionAttemptDelay) {
            lastConnectionAttemptTime = currentTime
            return true
        }
        return false
    }

    fun initiateConnection(onConnected: () -> Unit) {
        if (isScanningOrConnecting || !canAttemptConnection()) {
            Log.d("BluetoothService", "initiateConnection: Connection attempt debounced")
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
        Log.d("BluetoothService", "isConnected = $isConnected")
        if (isConnected) {
            Log.d("BluetoothService", "sendStartAlarm: Sending start alarm command")
            bluetoothManager.sendAlarmDataToESP32(alarm)
        }
        else {
            Log.d("BluetoothService", "sendStartAlarm: Device not connected, starting connection")
            initiateConnection {
                bluetoothManager.sendAlarmDataToESP32(alarm)
            }
        }
    }

    fun sendStopAlarm() {
        Log.d("BluetoothService", "isConnected = $isConnected")
        if (isConnected) {
            Log.d("BluetoothService", "sendStopAlarm: Sending stop alarm command")
            bluetoothManager.stopPlayingAlarm()
        }
        else {
            Log.d("BluetoothService", "sendStopAlarm: Device not connected, starting connection")
            initiateConnection {
                bluetoothManager.stopPlayingAlarm()
            }
        }
    }

    fun disconnect() {
        Log.d("BluetoothService", "disconnect: Disconnecting from device")
        bluetoothManager.disconnectFromDevice()
    }
}
