package com.extremewakeup.soundalarm

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.extremewakeup.soundalarm.navigation.AppNavigation
import com.extremewakeup.soundalarm.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        setContent {
            AppNavigation(viewModel, this)
        }
        viewModel.alarmList.observe(this) { alarms ->
            viewModel.scheduleAlarms(this)
        }
        if (intent.getBooleanExtra("showQRScanner", false)) {
            viewModel.onAlarmTriggered()
        }
        viewModel.updatePermissionsStatus(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.updatePermissionsStatus(this)
    }

    private fun isExactAlarmPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            return alarmManager?.canScheduleExactAlarms() ?: false
        }
        return true
    }



}
