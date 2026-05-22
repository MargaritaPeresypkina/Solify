package com.example.solify.presentation.debug

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AgentDebugLog {
    private const val TAG = "AgentDebug"
    private const val SESSION_ID = "b0a22c"
    private const val ENDPOINT = "http://10.0.2.2:7922/ingest/c3f39ab1-8683-4de0-9b45-96a1ea20cf5a"

    // #region agent log
    fun log(
        hypothesisId: String,
        location: String,
        message: String,
        data: Map<String, Any?> = emptyMap(),
        runId: String = "pre-fix"
    ) {
        val payload = JSONObject().apply {
            put("sessionId", SESSION_ID)
            put("hypothesisId", hypothesisId)
            put("location", location)
            put("message", message)
            put("timestamp", System.currentTimeMillis())
            put("runId", runId)
            put("data", JSONObject(data))
        }
        Log.d(TAG, payload.toString())
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val connection = URL(ENDPOINT).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("X-Debug-Session-Id", SESSION_ID)
                connection.doOutput = true
                connection.outputStream.use { it.write(payload.toString().toByteArray()) }
                connection.responseCode
                connection.disconnect()
            }
        }
    }
    // #endregion
}
