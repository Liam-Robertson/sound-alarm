package com.extremewakeup.soundalarm.viewmodel

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extremewakeup.soundalarm.model.Alarm
import com.extremewakeup.soundalarm.receiver.AlarmReceiver
import com.extremewakeup.soundalarm.repository.AlarmRepository
import com.extremewakeup.soundalarm.utils.scheduleAlarm
import com.extremewakeup.soundalarm.worker.NetworkUtil
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

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

    private val _isQRScannerVisible = MutableLiveData<Boolean>(false)
    val isQRScannerVisible: LiveData<Boolean> = _isQRScannerVisible

    fun updatePermissionStatus(isGranted: Boolean) {
        _permissionGranted.value = isGranted
    }

    init {
        refreshAlarmList()
    }

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {
                alarmRepository.insertAlarm(alarm)
                scheduleAlarm(alarm)
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
    }

    private fun scheduleAlarm(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.app.ACTION_SEND_BLE_MESSAGE"
            putExtra("ALARM_DATA", Json.encodeToString(alarm))
        }
        val alarmId = alarm.id ?: 0 // Assuming alarm.id is nullable, provide a default or handle appropriately
        val pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val triggerTime = alarm.time.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }


    fun onAlarmTriggered() {
        _isQRScannerVisible.value = true
    }

    private fun refreshAlarmList() {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedAlarms = alarmRepository.getAlarmList() // This now returns List<Alarm>
            withContext(Dispatchers.Main) {
                _alarmList.value = updatedAlarms
            }
        }
    }

    fun isExactAlarmPermissionGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return context.checkSelfPermission(Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    fun onQRCodeScanned(qrCode: String) {
        if (qrCode == "b9069d49-0956-4e34-b454-401044599906") {
//            sendMessageToESP32("stopAlarm")
        }
    }

    fun selectDaysActive(alarm: Alarm, dayIndex: Int) {
        null
    }

}




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