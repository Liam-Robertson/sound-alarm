package com.extremewakeup.soundalarm.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class SendMessageWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        return try {
            val url = URL("http://192.168.0.29:80")
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }

            OutputStreamWriter(httpURLConnection.outputStream).use { outputStreamWriter ->
                outputStreamWriter.write("startAlarm")
            }

            val responseCode = httpURLConnection.responseCode
            httpURLConnection.disconnect()

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}