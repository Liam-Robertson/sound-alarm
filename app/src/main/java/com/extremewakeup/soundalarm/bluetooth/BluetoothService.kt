package com.extremewakeup.soundalarm.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.extremewakeup.soundalarm.model.Alarm
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

class BluetoothService @Inject constructor(
    private val context: Context,
    private val bluetoothManager: BluetoothManager
) {

    var isConnected = false
    private var isScanningOrConnecting = false

    fun bondWithDevice() {
        bluetoothManager.discoverAndBond()
    }

    fun initiateConnection(onConnected: () -> Unit) {
//        if (isConnected) {
//            Log.d("BluetoothService", "initiateConnection: is already connected")
//            return
//        }
        if (isScanningOrConnecting) {
            Log.d("BluetoothService", "initiateConnection: is already scanning or connecting")
            return
        }

        isScanningOrConnecting = true
        Log.d("BluetoothService", "bluetoothService: Starting device scan")
        bluetoothManager.scanForDevices { device ->
            Log.d("BluetoothService", "bluetoothService: Device found, attempting connection")
            // Ensure device is bonded before connecting
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return@scanForDevices
            }
            if (device.bondState != BluetoothDevice.BOND_BONDED) {
                bluetoothManager.discoverAndBond()
            }
            // Delay for 2 seconds after scanning has stopped before trying to connect
            Handler(Looper.getMainLooper()).postDelayed({
                bluetoothManager.connectToDevice(device) {
                    Log.d("BluetoothService", "bluetoothService: Device connected, ready for commands")
                    isConnected = true
                    isScanningOrConnecting = false
                    onConnected()
                }
            }, 2000)
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

//    fun getConnectionState(): Boolean {
//        val sharedPreferences = context.getSharedPreferences("BluetoothPreferences", Context.MODE_PRIVATE)
//        return sharedPreferences.getBoolean("isConnected", false)
//    }

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

}
