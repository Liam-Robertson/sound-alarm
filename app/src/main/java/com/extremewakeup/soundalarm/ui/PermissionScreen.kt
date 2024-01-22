package com.extremewakeup.soundalarm.ui

import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import android.Manifest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import com.extremewakeup.soundalarm.MainActivity

@Composable
fun PermissionScreen(navController: NavHostController, viewModel: MainViewModel) {
    val context = LocalContext.current
//    val permissionGranted by viewModel.permissionGranted.observeAsState()
    val permissionGranted = true

    Button(onClick = {
        Log.d("PermissionScreen", "Request Permission button clicked")
//        (context as? MainActivity)?.requestPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    }) {
        Text("Request Permission")
    }

    LaunchedEffect(permissionGranted) {
        Log.d("PermissionScreen", "LaunchedEffect triggered with permissionGranted: $permissionGranted")
        if (permissionGranted == true) {
            Log.d("PermissionScreen", "Permission granted, navigating to alarmScreen")
            navController.navigate("alarmScreen") {
                popUpTo("permissionScreen") { inclusive = true }
            }
        }
    }
}
