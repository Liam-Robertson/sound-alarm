package com.extremewakeup.soundalarm.navigation

import AlarmScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.extremewakeup.soundalarm.ui.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val permissionGranted by viewModel.permissionGranted.observeAsState(initial = false)

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            navController.navigate("alarmScreen") {
                popUpTo("permissionScreen") { inclusive = true }
            }
        } else {
            navController.navigate("permissionScreen") {
                popUpTo("alarmScreen") { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = "permissionScreen") {
        composable("alarmScreen") {
            AlarmScreen(navController, viewModel)
        }
        composable("permissionScreen") {
            PermissionScreen(navController, viewModel)
        }
    }
}


