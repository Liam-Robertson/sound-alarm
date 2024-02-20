package com.extremewakeup.soundalarm.viewmodel

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extremewakeup.soundalarm.model.Alarm
import com.extremewakeup.soundalarm.repository.AlarmRepository
import com.extremewakeup.soundalarm.utils.scheduleAlarms
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import com.extremewakeup.soundalarm.bluetooth.BluetoothRepository
import com.extremewakeup.soundalarm.navigation.AppNavigation
import com.extremewakeup.soundalarm.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

@HiltViewModel
class MainViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val bluetoothRepository: BluetoothRepository,
    private val alarmManager: AlarmManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _permissionGranted = MutableLiveData<Boolean>()
    val permissionGranted: LiveData<Boolean> = _permissionGranted

    private val _alarmList = MutableLiveData<List<Alarm>>()
    val alarmList: LiveData<List<Alarm>> = _alarmList

    val _isQRScannerVisible = MutableLiveData<Boolean>(false)
    val isQRScannerVisible: LiveData<Boolean> = _isQRScannerVisible

    private val _bluetoothPermissionGranted = MutableLiveData<Boolean>()
    val bluetoothPermissionGranted: LiveData<Boolean> = _bluetoothPermissionGranted

    fun updateBluetoothPermissionStatus(isGranted: Boolean) {
        _bluetoothPermissionGranted.value = isGranted
    }

    private val schedulingMutex = Mutex()

//    fun updatePermissionsStatus(isGranted: Boolean) {
//        _permissionGranted.value = isGranted
//    }

    fun updatePermissionsStatus(context: Context, isExactAlarmPermissionGranted: Boolean) {
        val isBluetoothPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            // Pre-Android 12 (S), BLUETOOTH_CONNECT permission is not needed.
            true
        }
        val isBluetoothScanGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            // Pre-Android 12 (S), BLUETOOTH_CONNECT permission is not needed.
            true
        }
        val isFineLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            // Pre-Android 12 (S), BLUETOOTH_CONNECT permission is not needed.
            true
        }
        _permissionGranted.value = isExactAlarmPermissionGranted && isBluetoothPermissionGranted && isBluetoothScanGranted && isFineLocationGranted
    }

    init {
        viewModelScope.launch {
            refreshAlarmList()
        }
    }

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {
                Log.e("MainActivity", "BEEENG")
                Log.d("AlarmList", "Current alarm: ${alarm.time}")
                alarmRepository.insertAlarm(alarm)
                refreshAlarmList()
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
    }

    fun scheduleAlarms(context: Context) {
        Log.e("MainActivity", "BUUUUNG")
        viewModelScope.launch(Dispatchers.IO) {
            val currentAlarms = alarmList.value ?: return@launch
            Log.d("AlarmList", "Current alarms before scheduling: ${alarmList.value}")
            scheduleAlarms(context, currentAlarms)
        }
    }

    fun onAlarmTriggered() {
        _isQRScannerVisible.value = true
    }

//    private fun refreshAlarmList() {
//        viewModelScope.launch(Dispatchers.IO) {
//            val updatedAlarms = alarmRepository.getAlarmList()
//            withContext(Dispatchers.Main) {
//                _alarmList.value = updatedAlarms
//            }
//        }
//    }

    private suspend fun refreshAlarmList() {
        val updatedAlarms = withContext(Dispatchers.IO) {
            alarmRepository.getAlarmList()
        }
        withContext(Dispatchers.Main) {
            _alarmList.value = updatedAlarms
            scheduleAlarms(context)
        }
    }


    fun isExactAlarmPermissionGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return context.checkSelfPermission(Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun onQRCodeScanned(qrCode: String) {
        if (qrCode == "b9069d49-0956-4e34-b454-401044599906") {
            Log.d("QR Code Scanner", "Correct QR Code scanned")
            bluetoothRepository.stopAlarmPlaying()
        }
    }

    fun selectDaysActive(alarm: Alarm, dayIndex: Int) {
        null
    }
}


//private fun scheduleAlarm(alarm: Alarm) {
//    val intent = Intent(context, AlarmReceiver::class.java).apply {
//        action = "com.example.app.ACTION_SEND_BLE_MESSAGE"
//        putExtra("ALARM_DATA", Json.encodeToString(alarm))
//    }
//    val alarmId = alarm.id
//    val pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//    val triggerTime = alarm.time.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
//    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
//}


//    fun addAlarm(alarm: Alarm, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                alarmRepository.insertAlarm(alarm)
//                withContext(Dispatchers.Main) {
//                    onSuccess()
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    onFailure(e.message ?: "Error creating user")
//                }
//            }
//        }
//    }



//    fun selectDaysActive(alarm: Alarm, dayIndex: Int) {
//        val updatedAlarms = _alarmList.value?.map { currentAlarm ->
//            if (currentAlarm == alarm) {
//                val dayIndexStr = dayIndex.toString()
//                val newSelectedDays: MutableList<String> = currentAlarm.daysSelected.toMutableList()
//                if (newSelectedDays.contains(dayIndexStr)) {
//                    newSelectedDays.remove(dayIndexStr)
//                } else {
//                    newSelectedDays.add(dayIndexStr)
//                }
//
//                currentAlarm.copy(daysSelected = newSelectedDays)
//            } else {
//                currentAlarm
//            }
//        } ?: emptyList()
//        _alarmList.value = updatedAlarms
//    }