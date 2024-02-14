package com.extremewakeup.soundalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.extremewakeup.soundalarm.MainActivity
import com.extremewakeup.soundalarm.worker.SendMessageWorker

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val inputData = workDataOf("ALARM_ID" to alarmId)

        val sendMessageRequest = OneTimeWorkRequest.Builder(SendMessageWorker::class.java)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(sendMessageRequest)
        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("showQRScanner", true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(mainActivityIntent)
    }
}
