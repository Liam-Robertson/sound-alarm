package com.extremewakeup.soundalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        thread {
            try {
                val url = URL("http://192.168.0.29:80")
                val httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.requestMethod = "POST"
                httpURLConnection.doOutput = true
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val out = OutputStreamWriter(httpURLConnection.outputStream)
                out.write("startAlarm")
                out.close()

                httpURLConnection.responseCode

                httpURLConnection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
            }
        }
    }
}
