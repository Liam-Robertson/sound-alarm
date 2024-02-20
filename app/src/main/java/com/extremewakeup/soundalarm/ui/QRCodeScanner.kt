package com.extremewakeup.soundalarm.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import android.Manifest

@Composable
fun QRCodeScanner(onQRScanned: (String) -> Unit) {
    val scannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            onQRScanned(result.contents)
        } else {
            // Handle scan cancellation or error
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val options = ScanOptions().apply {
                setPrompt("Scan a QR code")
                setBeepEnabled(true)
                setOrientationLocked(true)
            }
            scannerLauncher.launch(options)
        } else {
            // Handle permission denial
        }
    }

    // Requesting camera permission
    DisposableEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        onDispose { }
    }

    // Optionally, add UI elements to indicate the QR scanner is ready
    Text(text = "Scan QR Code to stop the alarm", style = MaterialTheme.typography.h6)
}
