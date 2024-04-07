package com.extremewakeup.soundalarm.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.extremewakeup.soundalarm.model.Alarm
import com.extremewakeup.soundalarm.model.AlarmTiming
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.util.UUID

class BluetoothManager(private val context: Context) {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val serviceUUID: UUID = UUID.fromString("f261adff-f939-4446-82f9-2d00f4109dfe")
    private val characteristicUUID: UUID = UUID.fromString("a2932117-5297-476b-96f7-a873b1075803")
    private val foundDevices = mutableSetOf<String>()
    private var retryCount = 0
    private val maxRetries = 3 // Maximum number of retries
    private val retryDelay = 2000L // Delay between retries in milliseconds

    init {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter
        Log.d("BluetoothManager", "BluetoothManager initialized")
    }

    fun scanForDevices(onDeviceFound: (BluetoothDevice) -> Unit) {
        Log.d("BluetoothManager", "bluetoothManager: Starting device scan")
        val leScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                result?.device?.let { device ->
                    Log.d("BluetoothManager", "bluetoothManager: on scan result received")
                    if (device.name == "ESP32_BLE_Alarm_Server" && foundDevices.add(device.address)) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            Log.e("BluetoothManager", "scanForDevices: BLUETOOTH_SCAN permissions not granted")
                            return
                        }
                        Log.d("BluetoothManager", "scanForDevices: ESP32 device found, scanning stopped")
                        bluetoothAdapter?.bluetoothLeScanner?.stopScan(this)
                        onDeviceFound(device)
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BluetoothManager", "scanForDevices: Scan failed with error code: $errorCode")
            }
        }
        foundDevices.clear()
        bluetoothAdapter?.bluetoothLeScanner?.startScan(leScanCallback)
    }

    fun connectToDevice(device: BluetoothDevice, onConnected: () -> Unit) {
        Log.d("BluetoothManager", "connectToDevice: Attempting to connect to device ${device.address}")
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BluetoothManager", "BLUETOOTH_CONNECT permissions not granted")
            return
        }
        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("BluetoothManager", "BLUETOOTH_CONNECT permissions not granted")
                    return
                }
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d("BluetoothManager", "connectToDevice: Connected to GATT server, starting service discovery")
                        gatt?.discoverServices()
                        retryCount = 0 // Reset retry count on success
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.e("BluetoothManager", "connectToDevice: Disconnected from GATT server")
                        if (retryCount < maxRetries) {
                            retryCount++
                            Log.d("BluetoothManager", "Attempt $retryCount to reconnect")
                            Handler(Looper.getMainLooper()).postDelayed({
                                connectToDevice(device, onConnected)
                            }, retryDelay)
                        } else {
                            Log.e("BluetoothManager", "Max retries reached, giving up on connection.")
                        }
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BluetoothManager", "connectToDevice: BLE services discovered, device ready for communication")
                    onConnected()
                } else {
                    Log.e("BluetoothManager", "connectToDevice: Service discovery failed with status: $status")
                }
            }
        })
    }

    fun sendAlarmDataToESP32(alarm: Alarm) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BluetoothManager", "BLUETOOTH_CONNECT permissions not granted")
            return
        }
        Log.d("BluetoothManager", "sendAlarmDataToESP32: Preparing to send alarm data")
        val service = bluetoothGatt?.getService(serviceUUID)
        val characteristic = service?.getCharacteristic(characteristicUUID)
        characteristic?.let {
            val alarmData = Json.encodeToString(MapSerializer(String.serializer(), AlarmTiming.serializer()), mapOf("startAlarm" to AlarmTiming(time = alarm.time, volume = alarm.volume))).toByteArray(Charsets.UTF_8)
            it.value = alarmData
            val writeResult = bluetoothGatt?.writeCharacteristic(it)
            if (writeResult == true) {
                Log.d("BluetoothManager", "sendAlarmDataToESP32: Sending data succeeded")
            } else {
                Log.e("BluetoothManager", "sendAlarmDataToESP32: Sending data failed")
            }
        }
    }

    fun stopPlayingAlarm() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BluetoothManager", "BLUETOOTH_CONNECT permissions not granted")
            return
        }
        Log.d("BluetoothManager", "stopPlayingAlarm: Sending command to stop alarm")
        val service = bluetoothGatt?.getService(serviceUUID)
        val characteristic = service?.getCharacteristic(characteristicUUID)
        characteristic?.let {
            val alarmData = Json.encodeToString(MapSerializer(String.serializer(), String.serializer()), mapOf("stopAlarm" to "true")).toByteArray(Charsets.UTF_8)
            it.value = alarmData
            val writeResult = bluetoothGatt?.writeCharacteristic(it)
            Log.d("BluetoothManager", "stopPlayingAlarm: Command ${if (writeResult == true) "sent successfully" else "failed to send"}")
        }
    }

    fun disconnectFromDevice() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BluetoothManager", "BLUETOOTH_CONNECT permissions not granted")
            return
        }
        Log.d("BluetoothManager", "disconnectFromDevice: Disconnecting and closing GATT")
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
