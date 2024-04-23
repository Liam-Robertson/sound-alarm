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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.util.TreeSet
import java.util.UUID

class BluetoothManager(private val context: Context) {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val serviceUUID: UUID = UUID.fromString("f261adff-f939-4446-82f9-2d00f4109dfe")
    private val characteristicUUID: UUID = UUID.fromString("a2932117-5297-476b-96f7-a873b1075803")
    private val foundDevices = mutableSetOf<String>()

    //    private var retryCount = 0
//    private val maxRetries = 3 // Maximum number of retries
//    private val retryDelay = 2000L // Delay between retries in milliseconds
    var isConnected = false
    val deviceAddress = "E8:6B:EA:E0:05:3E"
//    val deviceAddress = "A8:42:E3:A8:72:B2"

    // Notes:
    // - There is a nasty bug where scanning doesn't detect ble device names, I'm using the device address as the identifier to get around this
    // - Peripherals can only connect to one central at any given time in ble
    // - I think the problem you're having is that you're not disconnecting properly which means that you're taking up the peripherals only connection slot

    // To do list:
    // - Use the companion manager as per this talk - https://www.droidcon.com/2022/11/15/state-of-ble-on-android-in-2022/
    // - Fix the disconnect button (it should log disconnect on the esp32 side)
    // - You need to stop scanning manually - either stop when you get a specific result or stop after a period of time
    // - Add a filter to your scan
    //
    // Long term:
    // - Add security


    init {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter
        Log.d("BluetoothManager", "BluetoothManager initialized")
    }

    fun discoverAndBond() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Check if already bonded without starting discovery
        val alreadyBonded = bluetoothAdapter.bondedDevices.any { it.address == deviceAddress }
        if (alreadyBonded) {
            Log.d("BluetoothManager", "Device already bonded.")
            return
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action: String? = intent.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val foundAddress = device.address
                    if (foundAddress == deviceAddress) {
                        Log.d(
                            "BluetoothManager",
                            "Attempting to create bond with device: $foundAddress"
                        )
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        device.createBond()
                    }
                }
            }
        }

        // Register the BroadcastReceiver
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, filter)

        // Start discovery
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle permission request
            return
        }
        bluetoothAdapter.startDiscovery()
        // Don't forget to unregister the receiver appropriately to avoid memory leaks
    }

    fun scanAndConnect() {
        Log.d("BluetoothService", "bluetoothService: Starting device scan")
        scanForDevices { device ->
            Log.d("BluetoothService", "bluetoothService: Device found, attempting connection")
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return@scanForDevices
            }
            Handler(Looper.getMainLooper()).postDelayed({
                connectToEsp32(device) {
                    Log.d("BluetoothService", "bluetoothService: Device connected, ready for commands")
                    return@connectToEsp32
                }
            }, 2000)
        }
    }

    fun scanForDevices(onDeviceFound: (BluetoothDevice) -> Unit) {
        Log.d("BluetoothManager", "bluetoothManager: Starting device scan")
        val foundDevices = TreeSet<BluetoothDevice>(compareBy { it.address })
        val leScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("BluetoothManager", "scanForDevices: BLUETOOTH_CONNECT permission not granted")
                    return
                }
                result?.device?.let { device ->
                    Log.d("BluetoothManager", "bluetoothManager: on scan result received")
                    Log.d("BluetoothManager", "device name ${device.name}")
                    Log.d("BluetoothManager", "device address ${device.address}")
                    foundDevices.add(device) // Add device to the TreeSet using the specified comparator
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BluetoothManager", "scanForDevices: Scan failed with error code: $errorCode")
            }
        }

        bluetoothAdapter?.bluetoothLeScanner?.startScan(leScanCallback)
        Handler(Looper.getMainLooper()).postDelayed({
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(leScanCallback)
            Log.d("BluetoothManager", "scanForDevices: Scanning stopped")
            foundDevices.find { (it.address ?: "").contains("A8:42:E3:A8:72:B2") }?.let { device ->
                Log.e("BluetoothManager", "FOUND ESP32_BLE_Server")
                Thread.sleep(2000)
                onDeviceFound(device)
            }
        }, 2000)
    }


    fun connectToEsp32(device: BluetoothDevice, onConnected: () -> Unit) {
        Log.d("BluetoothManager", "connectToDevice: Attempting to connect to device ${device.address}")
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("BluetoothManager", "BLUETOOTH_CONNECT permissions not granted")
            return
        }

        bluetoothGatt = device.connectGatt(context, true, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("BluetoothManager", "BLUETOOTH_CONNECT permissions not granted")
                    return
                }
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d("BluetoothManager", "connectToDevice: Connected to GATT server, starting service discovery")
                        isConnected = true
                        Thread.sleep(1000)
                        gatt?.discoverServices()
                        Thread.sleep(1000)
                        onConnected()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.e("BluetoothManager", "connectToDevice: Disconnected from GATT server")
                        isConnected = false
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(
                        "BluetoothManager",
                        "connectToDevice: BLE services discovered, device ready for communication"
                    )
                } else {
                    Log.e(
                        "BluetoothManager",
                        "connectToDevice: Service discovery failed with status: $status"
                    )
                }
            }
        })
    }

    fun stopPlayingAlarm() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("BluetoothManager", "BLUETOOTH_CONNECT permissions not granted")
            return
        }
        Log.d("BluetoothManager", "stopPlayingAlarm: Sending command to stop alarm")
        val service = bluetoothGatt?.getService(serviceUUID)
        val characteristic = service?.getCharacteristic(characteristicUUID)
        characteristic?.let {
            val alarmData = Json.encodeToString(
                MapSerializer(String.serializer(), String.serializer()),
                mapOf("stopAlarm" to "true")
            ).toByteArray(Charsets.UTF_8)
            it.value = alarmData
            val writeResult = bluetoothGatt?.writeCharacteristic(it)
            Log.d(
                "BluetoothManager",
                "stopPlayingAlarm: Command ${if (writeResult == true) "sent successfully" else "failed to send"}"
            )
        }
    }


    fun initiateConnection(onConnected: () -> Unit) {
        Log.d("BluetoothService", "bluetoothService: Starting device scan")
        scanForDevices { device ->
            Log.d("BluetoothService", "bluetoothService: Device found, attempting connection")
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return@scanForDevices
            }
            // Delay for 2 seconds after scanning has stopped before trying to connect
            Handler(Looper.getMainLooper()).postDelayed({
                connectToEsp32(device) {
                    Log.d("BluetoothService", "bluetoothService: Device connected, ready for commands")
                    onConnected()
                }
            }, 2000)
        }
    }

    fun sendMessageToEsp32(message: ByteArray) {
        sendBluetoothMessage(message)
//        if (isConnected) {
//            Log.d("BluetoothManager", "sendMessageToEsp32: isConnected=true, sending message")
//            sendBluetoothMessage(message)
//        } else {
//            Log.d("BluetoothManager", "sendMessageToEsp32: isConnected=false, starting connection")
//            initiateConnection {
//                sendBluetoothMessage(message)
//            }
//        }
    }

    fun sendBluetoothMessage(message: ByteArray) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BluetoothManager", "BLUETOOTH_CONNECT permissions not granted")
            return
        }
        Log.d("BluetoothManager", "sendMessageToEsp32: Preparing to send message")
        val service = bluetoothGatt?.getService(serviceUUID)
        val characteristic = service?.getCharacteristic(characteristicUUID)
        Log.d("BluetoothManager", "BluetoothManager: characteristic: $characteristic")
        Log.d("BluetoothManager", "11111")
        characteristic?.let {
            Log.d("BluetoothManager", "2222")
            it.value = message
            val writeResult = bluetoothGatt?.writeCharacteristic(it)
            Log.d("BluetoothManager", "33333")
            if (writeResult == true) {
                Log.d("BluetoothManager", "sendAlarmDataToESP32: Sending data succeeded")
            } else {
                Log.e("BluetoothManager", "sendAlarmDataToESP32: Sending data failed")
            }
        }
    }

    fun disconnectDevice() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("BluetoothManager", "BLUETOOTH_CONNECT permissions not granted")
            return
        }

        Thread.sleep(2000)
        bluetoothGatt?.let { gatt ->
            Log.d("BluetoothManager", "disconnectDevice: Disconnecting from GATT server")
            gatt.disconnect()
            Thread.sleep(1000)
            gatt.close()  // Properly close the GATT connection
            bluetoothGatt = null  // Clear the reference to avoid memory leaks
            isConnected = false
            Log.d("BluetoothManager", "disconnectDevice: Disconnected and resources released")
        } ?: Log.d("BluetoothManager", "disconnectDevice: No device connected")
    }
}

