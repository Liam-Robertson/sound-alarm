package com.extremewakeup.soundalarm.ui

import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController

@Composable
fun PermissionScreen(navController: NavHostController, viewModel: MainViewModel) {
    val context = LocalContext.current

    Button(onClick = {
        Log.d("PermissionScreen", "Request Permission button clicked")
        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
    }) {
        Text("Request Permission")
    }

    val permissionGranted = viewModel.isExactAlarmPermissionGranted(context)
    LaunchedEffect(permissionGranted) {
        Log.d("PermissionScreen", "LaunchedEffect triggered with permissionGranted: $permissionGranted")
        if (permissionGranted) {
            Log.d("PermissionScreen", "Permission granted, navigating to alarmScreen")
            navController.navigate("alarmScreen") {
                popUpTo("permissionScreen") { inclusive = true }
            }
        }
    }
}
