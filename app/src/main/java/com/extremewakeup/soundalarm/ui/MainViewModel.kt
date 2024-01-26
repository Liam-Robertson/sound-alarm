package com.extremewakeup.soundalarm.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extremewakeup.soundalarm.model.Alarm
import com.extremewakeup.soundalarm.repository.AlarmRepository
import com.extremewakeup.soundalarm.utils.scheduleAlarms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    private val _permissionGranted = MutableLiveData<Boolean>()
    val permissionGranted: LiveData<Boolean> = _permissionGranted

    private val _alarmList = MutableLiveData<List<Alarm>>()
    val alarmList: LiveData<List<Alarm>> = _alarmList

    fun updatePermissionStatus(isGranted: Boolean) {
        _permissionGranted.value = isGranted
    }

    init {
        refreshAlarmList()
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

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                alarmRepository.insertAlarm(alarm)
                refreshAlarmList()
            } catch (e: Exception) {
                // Handle error
            }
        }
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


    fun scheduleAlarms(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentAlarms = alarmList.value ?: return@launch
            scheduleAlarms(context, currentAlarms)
        }
    }

    fun selectDaysActive(alarm: Alarm, dayIndex: Int) {
        null
    }

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


}
