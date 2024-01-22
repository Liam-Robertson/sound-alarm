package com.extremewakeup.soundalarm.navigation

import AlarmScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.extremewakeup.soundalarm.ui.*
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val permissionGranted by viewModel.permissionGranted.observeAsState(initial = false)

    NavHost(navController = navController, startDestination = if (permissionGranted) "alarmScreen" else "permissionScreen") {
        composable("alarmScreen") {
            AlarmScreen(navController, viewModel)
        }
        composable("permissionScreen") {
            PermissionScreen(navController, viewModel)
        }
    }
}

