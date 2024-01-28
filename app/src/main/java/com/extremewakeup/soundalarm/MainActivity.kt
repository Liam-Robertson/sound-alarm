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
import com.extremewakeup.soundalarm.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // To do list:
    // - Make it a https request instead of http
    // - Delete the security config you made to bypass https and remove this line from the manifest @xml/network_security_config

    // Long term to do:
    // - Create a default alarm which is quiet wake up noises getting gradually louder then 30 seconds in it turns into a claxon

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
    }

    override fun onResume() {
        super.onResume()
        viewModel.updatePermissionStatus(isExactAlarmPermissionGranted())
    }

    private fun isExactAlarmPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            return alarmManager?.canScheduleExactAlarms() ?: false
        }
        return true
    }

}
