package com.extremewakeup.soundalarm.worker;

import android.util.Log;
import java.net.HttpURLConnection;
import java.net.URL;

object NetworkUtil {
    fun sendPostRequest(urlString: String, postBody: String): Boolean {
        Log.d("NetworkUtil", "Sending POST request to $urlString with body $postBody")
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "text/plain")

            connection.outputStream.use { os ->
                val output = postBody.toByteArray(Charsets.UTF_8)
                os.write(output, 0, output.size)
                Log.d("NetworkUtil", "POST data sent")
            }

            val responseCode = connection.responseCode
            Log.d("NetworkUtil", "Response Code: $responseCode")

            // Reading response from the server
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d("NetworkUtil", "Response: $response")

            responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            Log.e("NetworkUtil", "POST request failed: ${e.message}")
            false
        }
    }
}
