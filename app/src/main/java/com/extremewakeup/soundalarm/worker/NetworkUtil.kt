package com.extremewakeup.soundalarm.worker

import java.net.HttpURLConnection
import java.net.URL

object NetworkUtil {
    fun sendPostRequest(urlString: String, postBody: String): Boolean {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "text/plain")

            connection.outputStream.use { os ->
                val output = postBody.toByteArray(Charsets.UTF_8)
                os.write(output, 0, output.size)
            }

            val responseCode = connection.responseCode
            responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
