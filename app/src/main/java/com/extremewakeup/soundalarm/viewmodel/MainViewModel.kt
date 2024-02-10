package com.extremewakeup.soundalarm.viewmodel

import android.Manifest
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
import com.extremewakeup.soundalarm.worker.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val bluetoothRepository: BluetoothRepository
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
                bluetoothRepository.sendAlarmToESP32(alarm)
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
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
            sendMessageToESP32("stopAlarm")
        }
    }

    private fun sendMessageToESP32(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val urlString = "http://192.168.0.30:80"
            val success = NetworkUtil.sendPostRequest(urlString, message)
            if (success) {
                Log.d("sendMessageToESP32", "Message sent successfully")
            } else {
                Log.e("sendMessageToESP32", "Failed to send message")
            }
        }
    }

    fun scheduleAlarms(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentAlarms = alarmList.value ?: return@launch
            scheduleAlarms(context, currentAlarms)
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