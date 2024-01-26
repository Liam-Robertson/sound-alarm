package com.extremewakeup.soundalarm.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class SendMessageWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        return try {
            val urlString = "http://192.168.0.30:80"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "text/plain")

            // Send the POST request
            val out = OutputStreamWriter(connection.outputStream)
            out.write("startAlarm")
            out.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // The request was successful
                Result.success()
            } else {
                // The request failed
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
