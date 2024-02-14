package com.extremewakeup.soundalarm.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.extremewakeup.soundalarm.viewmodel.MainViewModel

@Composable
fun PermissionScreen(navController: NavHostController, viewModel: MainViewModel) {
    val context = LocalContext.current

    Column {
        Button(onClick = {
            Log.d("PermissionScreen", "Request Schedule Exact Alarm Permission button clicked")
            context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        }) {
            Text("Request Schedule Exact Alarm Permission")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            Log.d("PermissionScreen", "Request Bluetooth Permission button clicked")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    context.findActivity(), arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 0)
            }
        }) {
            Text("Request Bluetooth Permission")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            Log.d("PermissionScreen", "Request Bluetooth Scan Permission button clicked")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    context.findActivity(), arrayOf(Manifest.permission.BLUETOOTH_SCAN), 0)
            }
        }) {
            Text("Request Bluetooth Scan Permission")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            Log.d("PermissionScreen", "Request Fine Location Permission button clicked")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    context.findActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
            }
        }) {
            Text("Request Fine Location Permission")
        }
    }

    val permissionGranted = viewModel.permissionGranted.observeAsState().value ?: false
    LaunchedEffect(permissionGranted) {
        Log.d("PermissionScreen", "LaunchedEffect triggered with permissionGranted: $permissionGranted")
        if (permissionGranted) {
            Log.d("PermissionScreen", "Permissions granted, navigating to alarmScreen")
            navController.navigate("alarmScreen") {
                popUpTo("permissionScreen") { inclusive = true }
            }
        }
    }
}

fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> throw IllegalStateException("Context cannot be cast to Activity")
}