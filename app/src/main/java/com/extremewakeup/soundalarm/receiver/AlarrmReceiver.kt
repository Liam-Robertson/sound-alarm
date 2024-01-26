package com.extremewakeup.soundalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.extremewakeup.soundalarm.worker.SendMessageWorker

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Enqueue the work request to send a message to the ESP32
        val sendMessageRequest = OneTimeWorkRequest.Builder(SendMessageWorker::class.java).build()
        WorkManager.getInstance(context).enqueue(sendMessageRequest)
    }
}
