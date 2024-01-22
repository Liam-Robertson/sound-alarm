package com.extremewakeup.soundalarm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.extremewakeup.soundalarm.navigation.AppNavigation
import com.extremewakeup.soundalarm.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.provider.Settings

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")

        if (!viewModel.isExactAlarmPermissionGranted(this)) {
            // Navigate to system settings for the user to enable the permission
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }

        setContent {
            AppNavigation(viewModel)
        }

        viewModel.scheduleAlarms(this)
    }
}
