package com.extremewakeup.soundalarm.ui.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.extremewakeup.soundalarm.worker.SendMessageWorker
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class QRCodeScannerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QRCodeScanner()
        }
    }
}

@Composable
fun QRCodeScanner() {
    val context = LocalContext.current
    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            handleScanResult(result.contents, context)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                startQRScanner(barcodeLauncher)
            } else {
                // Handle permission denial
            }
        }
    )

    // Check and request permission
    DisposableEffect(Unit) {
        launcher.launch(android.Manifest.permission.CAMERA)
        onDispose { }
    }
}

fun startQRScanner(barcodeLauncher: ManagedActivityResultLauncher<ScanOptions, ScanIntentResult>) {
    val options = ScanOptions()
    options.setPrompt("Scan a QR code")
    options.setBeepEnabled(true)
    options.setOrientationLocked(true)
    barcodeLauncher.launch(options)
}

fun handleScanResult(contents: String, context: Context) {
    // Assuming the QR code contains a specific string to indicate stopping the alarm
    if (contents == "stopAlarm") {
        val data = workDataOf("command" to "stopAlarm")
        val sendMessageWorkRequest = OneTimeWorkRequestBuilder<SendMessageWorker>()
            .setInputData(data)
            .build()
        WorkManager.getInstance(context).enqueue(sendMessageWorkRequest)
    }
}

