package com.extremewakeup.soundalarm.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class SendMessageWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val urlString = "http://192.168.0.30:80"
        val success = NetworkUtil.sendPostRequest(urlString, "startAlarm")
        return if (success) Result.success() else Result.failure()
    }
}
