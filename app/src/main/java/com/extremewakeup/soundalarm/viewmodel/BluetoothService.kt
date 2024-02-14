package com.extremewakeup.soundalarm.viewmodel

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.extremewakeup.soundalarm.model.Alarm
import kotlinx.serialization.json.Json
import java.util.UUID

class BluetoothService(private val context: Context) {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val serviceUUID = UUID.fromString("f261adff-f939-4446-82f9-2d00f4109dfe")
    private val characteristicUUID = UUID.fromString("a2932117-5297-476b-96f7-a873b1075803")

    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Log.e("BluetoothService", "Device does not support Bluetooth")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun startBleOperation(alarm: Alarm) {
        if (!context.hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            Log.e("BluetoothService", "BLUETOOTH_SCAN permission not granted")
            requestBluetoothScanPermission()
            return
        }
        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.e("BluetoothService", "ACCESS_FINE_LOCATION permission not granted")
            return
        }
        scanForDevices { device ->
            connectToDevice(device) {
                sendAlarmDataToESP32(alarm)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun sendAlarmDataToESP32(alarm: Alarm) {
        if (context.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            val serviceUUID = UUID.fromString("f261adff-f939-4446-82f9-2d00f4109dfe")
            val characteristicUUID = UUID.fromString("a2932117-5297-476b-96f7-a873b1075803")
            val alarmData = Json.encodeToString(Alarm.serializer(), alarm)

            val service = bluetoothGatt?.getService(serviceUUID) ?: throw NoSuchElementException("Bluetooth service not found")
            val characteristic = service.getCharacteristic(characteristicUUID) ?: throw NoSuchElementException("Bluetooth characteristic not found")

            characteristic.let {
                val writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                val valueToWrite = alarmData.toByteArray(Charsets.UTF_8)
                try {
                    Log.d("BluetoothService", "Sending alarm data to ESP32: $alarmData")
                    val result = bluetoothGatt?.writeCharacteristic(it, valueToWrite, writeType)
                    if (result == 1) {
                        Log.d("BluetoothService", "Alarm data sent successfully.")
                    } else {
                        Log.e("BluetoothService", "Failed to send alarm data.")
                    }
                } catch (e: SecurityException) {
                    Log.e("BluetoothService", "Failed to send alarm data: ${e.message}")
                }
            }
        } else {
            Log.e("BluetoothService", "BLUETOOTH_CONNECT permission not granted")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("BluetoothService", "Connected to GATT server.")
                val hasFineLocationPermission = context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                if (hasFineLocationPermission) {
                    try {
                        gatt?.discoverServices()
                    } catch (e: SecurityException) {
                        Log.i("BluetoothService", "Security exceptions")
                        // Log or handle the potential SecurityException
                    }
                } else {
                    Log.i("BluetoothService", "Doesn't have location permissions")
                    // Request the ACCESS_FINE_LOCATION permission from the user
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("BluetoothViewModel", "Disconnected from GATT server.")
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothService", "Services discovered.")
                val serviceUUID = UUID.fromString("your-service-uuid-here")
                val characteristicUUID = UUID.fromString("your-characteristic-uuid-here")
                val service = gatt?.getService(serviceUUID)
                val characteristic = service?.getCharacteristic(characteristicUUID)

                if (characteristic != null && context.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                    try {
                        val valueToWrite = "Your data here".toByteArray(Charsets.UTF_8)
                        val writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        // New method call, wrapped in permission check
                        val writeResult = gatt?.writeCharacteristic(characteristic, valueToWrite, writeType)
                        Log.d("BLE", "Attempt to write: $writeResult")
                    } catch (e: SecurityException) {
                        // Handle or log the SecurityException
                    }
                } else {
                    Log.w("BLE", "Characteristic not found or BLUETOOTH_CONNECT permission not granted")
                    // Optionally, request the BLUETOOTH_CONNECT permission from the user
                }
            } else {
                Log.w("BLE", "onServicesDiscovered received: $status")
            }
        }

    }

    private fun scanForDevices(onDeviceFound: (BluetoothDevice) -> Unit) {
        if (context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            try {
                val leScanCallback = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult?) {
                        super.onScanResult(callbackType, result)
                        result?.device?.let { device ->
                            // Check for the specific device, e.g., by name or UUID
                            if (device.name == "ESP32_BLE_Alarm_Server") {
                                bluetoothAdapter?.bluetoothLeScanner?.stopScan(this)
                                Log.d("BluetoothService", "ESP32 device found and scanning stopped.")
                                onDeviceFound(device)
                            }
                        }
                    }
                    override fun onScanFailed(errorCode: Int) {
                        super.onScanFailed(errorCode)
                        Log.e("BluetoothService", "Scan failed with error code: $errorCode")
                    }
                }
                bluetoothAdapter?.bluetoothLeScanner?.startScan(leScanCallback)
                Log.d("BluetoothService", "Scanning for devices...")
            } catch (e: SecurityException) {
                // Handle exception or notify user about needed permission
            }
        } else {
            // Request the necessary location permission from the user
            Log.e("BluetoothService", "Location permission not granted for BLE scan")
        }
    }

    private fun connectToDevice(device: BluetoothDevice, onConnected: () -> Unit) {
        if (context.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            try {
                bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                        super.onConnectionStateChange(gatt, status, newState)
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            Log.d("BluetoothService", "Connected to the GATT server.")
                            gatt?.discoverServices()
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            Log.d("BluetoothService", "Disconnected from the GATT server.")
                        }
                    }

                    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                        super.onServicesDiscovered(gatt, status)
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            Log.d("BluetoothService", "BLE services discovered.")
                            onConnected()
                        } else {
                            Log.e("BluetoothService", "Service discovery failed with status: $status")
                        }
                    }
                })
                Log.d("BluetoothService", "Attempting to connect to device ${device.address}")
            } catch (e: SecurityException) {
                // Handle the security exception or inform the user they need to grant the permission
            }
        } else {
            // Prompt the user to grant the BLUETOOTH_CONNECT permission
            Log.e("BluetoothService", "BLUETOOTH_CONNECT permission not granted")
        }
    }

    fun disconnectFromDevice() {
        if (context.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            try {
                bluetoothGatt?.close()
                bluetoothGatt = null
                Log.d("BluetoothService", "Disconnected from device")
            } catch (e: SecurityException) {
                // Handle the security exception or inform the user about the needed permission
            }
        } else {
            // Request the BLUETOOTH_CONNECT permission from the user
        }
    }

    fun Context.hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun performActionWithPermissionCheck(context: Context, permission: String, action: () -> Unit) {
        if (context.hasPermission(permission)) {
            try {
                action()
            } catch (e: SecurityException) {
                // Handle the exception or notify the user to grant permission
            }
        } else {
            // Request the permission from the user
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestBluetoothScanPermission() {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.BLUETOOTH_SCAN),
            BLUETOOTH_SCAN_REQUEST_CODE
        )
    }

    companion object {
        private const val BLUETOOTH_SCAN_REQUEST_CODE = 1001
    }
}
