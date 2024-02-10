package com.extremewakeup.soundalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.extremewakeup.soundalarm.viewmodel.BluetoothRepository
import com.extremewakeup.soundalarm.viewmodel.BluetoothService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Extract the alarm data
        val alarmDataJson = intent.getStringExtra("ALARM_DATA")
        alarmDataJson?.let {
            // Initialize BluetoothService and send the message
            val bluetoothService = BluetoothService(context)
            bluetoothService.sendData(it)
        }
    }
}




//class AlarmReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        // Enqueue the work request to send a message to the ESP32
//        val sendMessageRequest = OneTimeWorkRequest.Builder(SendMessageWorker::class.java).build()
//        WorkManager.getInstance(context).enqueue(sendMessageRequest)
//        Intent(context, MainActivity::class.java).apply {
//            putExtra("showQRScanner", true)
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//            context.startActivity(this)
//        }
//    }
//}
