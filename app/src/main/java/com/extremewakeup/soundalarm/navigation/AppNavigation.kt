package com.extremewakeup.soundalarm.navigation

import AlarmScreen
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.extremewakeup.soundalarm.viewmodel.MainViewModel
import com.extremewakeup.soundalarm.ui.PermissionScreen

@Composable
fun AppNavigation(viewModel: MainViewModel, context: Context) {
    val navController = rememberNavController()
    val permissionGranted by viewModel.permissionGranted.observeAsState(initial = false)

    // Define the NavHost with all your composable destinations
    NavHost(navController = navController, startDestination = "permissionScreen") {
        composable("alarmScreen") {
            AlarmScreen(navController, viewModel, context)
        }
        composable("permissionScreen") {
            PermissionScreen(navController, viewModel)
        }
        // Add other composable destinations as needed
    }

    // Use LaunchedEffect to listen for changes in permission status and navigate accordingly
    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            navController.navigate("alarmScreen") {
                popUpTo("permissionScreen") { inclusive = true }
            }
        }
    }
}
