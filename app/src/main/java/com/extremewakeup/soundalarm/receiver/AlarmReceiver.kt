package com.extremewakeup.soundalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.extremewakeup.soundalarm.worker.SendMessageWorker

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)

        val workRequest = OneTimeWorkRequest.Builder(SendMessageWorker::class.java)
            .setInputData(workDataOf("ALARM_ID" to alarmId))
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
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
