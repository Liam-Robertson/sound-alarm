package com.extremewakeup.soundalarm

import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.extremewakeup.soundalarm.navigation.AppNavigation
import com.extremewakeup.soundalarm.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        setContent {
            AppNavigation(viewModel, this)
        }
        viewModel.scheduleAlarms(this)
        if (intent.getBooleanExtra("showQRScanner", false)) {
            viewModel.onAlarmTriggered()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updatePermissionsStatus(this, isExactAlarmPermissionGranted())
        if (intent.getBooleanExtra("showQRScanner", false)) {
            viewModel.onAlarmTriggered()
        }
    }

    private fun isExactAlarmPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            return alarmManager?.canScheduleExactAlarms() ?: false
        }
        return true
    }
}
