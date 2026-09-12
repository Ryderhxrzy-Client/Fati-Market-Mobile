package com.fati_market

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Foreground delivery when Firebase is unavailable. Never marks chats as read. */
internal class ChatNotificationPoller(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("fatimarket_prefs", 0)
    private val client = OkHttpClient.Builder().callTimeout(15, TimeUnit.SECONDS).build()
    private var session = ""
    private var cursor: Long? = null

    suspend fun poll() {
        val auth = prefs.getString("auth_token", "").orEmpty()
        if (auth != session) { session = auth; cursor = null }
        if (auth.isBlank()) return
        val query = cursor?.let { "?after_id=$it" }.orEmpty()
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url("${MarketplaceApi.BASE_URL}/notifications/chat$query")
                    .header("Authorization", "Bearer $auth").header("Accept", "application/json").build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w("FatiChatNotifications", "Chat notification sync failed: HTTP ${response.code}")
                        null
                    } else JSONObject(response.body?.string().orEmpty()).getJSONObject("data")
                }
            }.onFailure { Log.w("FatiChatNotifications", "Chat notification sync unavailable", it) }.getOrNull()
        } ?: return
        // Do not route an old account's response after logout, or consume it while hidden.
        if (prefs.getString("auth_token", "") != auth || !InAppNotifications.isForeground) return
        val messages = result.getJSONArray("messages")
        for (index in 0 until messages.length()) {
            val message = messages.getJSONObject(index)
            val data = message.keys().asSequence().associateWith { message.optString(it) }
            InAppNotifications.fromPush(data)?.let(InAppNotifications::post)
        }
        cursor = result.getLong("cursor")
    }
}
