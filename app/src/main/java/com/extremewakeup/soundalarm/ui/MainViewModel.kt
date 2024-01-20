package com.extremewakeup.soundalarm.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extremewakeup.soundalarm.model.Alarm
import com.extremewakeup.soundalarm.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    val alarms: LiveData<List<Alarm>> = alarmRepository.getAlarms()

    fun addAlarm(alarm: Alarm) = viewModelScope.launch {
        alarmRepository.insertAlarm(alarm)
    }

}
