package com.fati_market

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.content.LocusIdCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.fati_market.R
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val API_BASE = "https://fati-api.alertaraqc.com/api"
private const val TAG = "FatiPushService"
private const val CHANNEL_ID = "fati_chat_messages"

/** Order and payment updates, separate from chat so it can be silenced apart. */
private const val ORDER_CHANNEL_ID = "fati_order_updates"
private const val ORDER_NOTIFICATION_TAG = "fati_order"
private const val REPLY_KEY = "chat_reply"
private const val AVATAR_SIZE_PX = 256
private const val MAX_AVATAR_BYTES = 5 * 1024 * 1024

private data class ChatLine(
    val text: String,
    val at: Long,
    val senderName: String,
    val senderId: String,
    val avatarUrl: String,
)

private val chatHistory = mutableMapOf<String, MutableList<ChatLine>>()
private val avatarCache = mutableMapOf<String, Bitmap>()

fun requestNotificationPermissionAndRegister(context: Context) {
    val activity = context as? Activity
    if (Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        activity?.requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 2001)
    }

    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
        registerFcmToken(context, token)
    }
}

class FatiFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        registerFcmToken(this, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data

        // A system notification while the user is already inside the app just
        // covers the screen they are looking at. Hand it to the in-app banner
        // instead, which can also be replied to in place.
        if (InAppNotifications.isForeground) {
            // In the foreground the system tray is never used: the in-app
            // banner shows what it can, and anything it cannot parse is
            // dropped rather than covering the screen the user is on.
            InAppNotifications.fromPush(data)?.let { InAppNotifications.post(it) }
            return
        }

        when (data["type"]) {
            "chat_message" -> showChatNotification(this, data)

            // Order lifecycle: a new order for Admin, and payment verified /
            // declined / ready / completed for the buyer.
            "order_placed",
            "payment_proof_submitted",
            "order_update",
            // Listing lifecycle: offer accepted / declined / scheduled, and
            // the 6h / 1h / 30m meet-up reminders. Same channel - these are
            // the seller's equivalent of an order update.
            "item_update",
            "meetup_reminder" -> showOrderNotification(this, data)
        }
    }
}

fun registerFcmToken(context: Context, token: String) {
    val prefs = context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE)
    val authToken = prefs.getString("auth_token", "") ?: ""
    if (authToken.isBlank() || token.isBlank()) return

    Thread {
        val body = JSONObject().apply {
            put("token", token)
            put("platform", "android")
            put("device_id", android.os.Build.MODEL)
        }.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$API_BASE/device-tokens")
            .header("Authorization", "Bearer $authToken")
            .header("Accept", "application/json")
            .post(body)
            .build()
        runCatching { OkHttpClient().newCall(request).execute().close() }
    }.start()
}

/**
 * A notification for an order event.
 *
 * Kept on its own channel so a buyer can silence chatter without missing the
 * message that says their payment went through. The transaction and item ids
 * ride along in the intent so opening it can go straight to the order.
 */
private fun showOrderNotification(context: Context, data: Map<String, String>) {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(
        NotificationChannel(ORDER_CHANNEL_ID, "Orders", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Order and payment updates"
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)
            lightColor = 0xFF1F6B43.toInt()
        }
    )

    val title = data["title"].orEmpty().ifBlank { "Order update" }
    val body = data["body"].orEmpty().ifBlank { data["item_title"].orEmpty() }
    val transactionId = data["transaction_id"].orEmpty()

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("notification_type", data["type"].orEmpty())
        putExtra("transaction_id", transactionId)
        putExtra("item_id", data["item_id"].orEmpty())
        putExtra("buyer_id", data["buyer_id"].orEmpty())
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        transactionId.toIntOrNull() ?: 0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val notification = NotificationCompat.Builder(context, ORDER_CHANNEL_ID)
        .setSmallIcon(R.drawable.push_icon)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_STATUS)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    // One notification per order, so a later update replaces the earlier one
    // rather than stacking up.
    manager.notify(
        ORDER_NOTIFICATION_TAG,
        transactionId.toIntOrNull() ?: System.currentTimeMillis().toInt(),
        notification,
    )
}

private fun showChatNotification(context: Context, data: Map<String, String>) {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(
        NotificationChannel(CHANNEL_ID, "Messages", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "New messages from Fati Market conversations"
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)
            lightColor = 0xFF1F6B43.toInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) setAllowBubbles(true)
        }
    )

    val messageId = data["message_id"].orEmpty()
    val itemId = data["item_id"].orEmpty()
    val senderId = data["sender_id"].orEmpty()
    val senderName = data["sender_name"].orEmpty().ifBlank { "New message" }
    val text = data["message"].orEmpty()
    val avatarUrl = data["sender_profile_picture"].orEmpty()
    val conversationKey = "$itemId:$senderId"
    val notificationId = conversationKey.hashCode()
    val lines = chatHistory.getOrPut(conversationKey) { mutableListOf() }
    lines.add(
        ChatLine(
            text = text,
            at = System.currentTimeMillis(),
            senderName = senderName,
            senderId = senderId,
            avatarUrl = avatarUrl,
        )
    )
    while (lines.size > 8) lines.removeAt(0)

    val openIntent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        putExtra("open_chat", true)
        putExtra("chat_item_id", itemId)
        putExtra("chat_user_id", senderId)
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val openPending = PendingIntent.getActivity(context, notificationId, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    val markReadIntent = Intent(context, MarkChatReadReceiver::class.java).apply {
        putExtra("message_id", messageId)
    }
    val markReadPending = PendingIntent.getBroadcast(context, (messageId + "read").hashCode(), markReadIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    val replyIntent = Intent(context, ChatReplyReceiver::class.java).apply {
        putExtra("item_id", itemId)
        putExtra("receiver_id", senderId)
        putExtra("message_id", messageId)
    }
    val replyPending = PendingIntent.getBroadcast(context, (messageId + "reply").hashCode(), replyIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    val remoteInput = RemoteInput.Builder(REPLY_KEY).setLabel("Reply").build()

    // MessagingStyle's constructor person is the current user, not the
    // sender. This is the same setup used by Apsara's working implementation.
    val prefs = context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE)
    val self = Person.Builder()
        .setName("You")
        .setKey("self")
        .apply {
            loadAvatar(prefs.getString("user_profile_picture", "").orEmpty())
                ?.let(::circleBitmap)
                ?.let { setIcon(IconCompat.createWithBitmap(it)) }
        }
        .build()

    // This is a one-to-one marketplace conversation. Group mode adds a
    // separate conversation-title row and makes a short message unnecessarily
    // tall; direct mode lets Android use the sender as the compact headline.
    val style = NotificationCompat.MessagingStyle(self)
        .setGroupConversation(false)

    lines.forEach { line ->
        val sender = senderPerson(line.senderName, line.senderId, line.avatarUrl)
        style.addMessage(line.text, line.at, sender)
    }

    val senderAvatar = senderAvatar(senderName, avatarUrl)
    val currentSender = Person.Builder()
        .setName(senderName)
        .setKey(senderId.ifBlank { senderName })
        .setIcon(IconCompat.createWithBitmap(senderAvatar))
        .build()
    val shortcutId = publishChatShortcut(
        context,
        conversationKey,
        senderName,
        currentSender,
        senderAvatar,
        openIntent,
    )

    val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
    // MessagingStyle plus this long-lived shortcut makes Android render the
    // actual conversation template (large avatar + small app-icon badge).
    notificationBuilder.setSmallIcon(R.drawable.push_icon)
    notificationBuilder.setContentTitle(senderName)
    notificationBuilder.setContentText(text)
    notificationBuilder.setStyle(style)
    notificationBuilder.addPerson(currentSender)
    // Person/shortcut metadata remains the primary conversation identity.
    // Some OEM collapsed MessagingStyle templates do not promote Person.icon;
    // the large-icon slot is the supported fallback that keeps the sender's
    // avatar prominent instead of replacing the required small icon.
    notificationBuilder.setLargeIcon(senderAvatar)
    notificationBuilder.setContentIntent(openPending)
    notificationBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.push_icon, "Mark as read", markReadPending).build())
    notificationBuilder.addAction(
        NotificationCompat.Action.Builder(R.drawable.push_icon, "Reply", replyPending)
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .setShowsUserInterface(false)
            .build()
    )
    notificationBuilder.setAutoCancel(true)
    notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
    notificationBuilder.setCategory(NotificationCompat.CATEGORY_MESSAGE)
    notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
    notificationBuilder.setColor(0xFF1F6B43.toInt())
    notificationBuilder.setOnlyAlertOnce(false)
    if (shortcutId != null) {
        notificationBuilder.setShortcutId(shortcutId)
        notificationBuilder.setLocusId(LocusIdCompat(shortcutId))
    }
    val notification = notificationBuilder.build()
    manager.notify(notificationId, notification)
}

private fun senderPerson(name: String, id: String, avatarUrl: String): Person =
    Person.Builder()
        .setName(name)
        .setKey(id.ifBlank { name })
        .setIcon(IconCompat.createWithBitmap(senderAvatar(name, avatarUrl)))
        .build()

private fun senderAvatar(name: String, avatarUrl: String): Bitmap =
    loadAvatar(avatarUrl)?.let(::circleBitmap) ?: fallbackAvatar(name)

private fun loadAvatar(url: String): Bitmap? {
    if (url.isBlank()) return null
    avatarCache[url]?.let { return it }
    return runCatching<Bitmap?> {
        val parsedUrl = URL(url)
        if (parsedUrl.protocol !in setOf("http", "https")) return@runCatching null

        val connection = (parsedUrl.openConnection() as HttpURLConnection).apply {
            connectTimeout = 5000
            readTimeout = 5000
            doInput = true
            instanceFollowRedirects = true
        }
        try {
            connection.connect()
            if (connection.responseCode !in 200..299) return@runCatching null
            if (connection.contentLengthLong > MAX_AVATAR_BYTES) return@runCatching null

            val bytes = connection.inputStream.use(::readAvatarBytes)
            decodeSampledAvatar(bytes)
        } finally {
            connection.disconnect()
        }
    }.getOrNull()?.also { avatarCache[url] = it }
}

private fun readAvatarBytes(input: java.io.InputStream): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8 * 1024)
    var total = 0
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        total += count
        if (total > MAX_AVATAR_BYTES) throw IOException("Avatar image is too large")
        output.write(buffer, 0, count)
    }
    return output.toByteArray()
}

private fun decodeSampledAvatar(bytes: ByteArray): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > AVATAR_SIZE_PX * 2) {
        sampleSize *= 2
    }
    return BitmapFactory.decodeByteArray(
        bytes,
        0,
        bytes.size,
        BitmapFactory.Options().apply { inSampleSize = sampleSize },
    )
}

private fun circleBitmap(source: Bitmap): Bitmap {
    val size = minOf(source.width, source.height).coerceAtLeast(1)
    val square = Bitmap.createBitmap(source, (source.width - size) / 2, (source.height - size) / 2, size, size)
    val scaled = if (size == AVATAR_SIZE_PX) square else {
        Bitmap.createScaledBitmap(square, AVATAR_SIZE_PX, AVATAR_SIZE_PX, true)
    }
    val output = Bitmap.createBitmap(AVATAR_SIZE_PX, AVATAR_SIZE_PX, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(output)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    canvas.drawCircle(AVATAR_SIZE_PX / 2f, AVATAR_SIZE_PX / 2f, AVATAR_SIZE_PX / 2f, paint)
    paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(scaled, 0f, 0f, paint)
    return output
}

private fun fallbackAvatar(name: String): Bitmap {
    val cacheKey = "fallback:${name.trim().uppercase()}"
    avatarCache[cacheKey]?.let { return it }

    val bitmap = Bitmap.createBitmap(AVATAR_SIZE_PX, AVATAR_SIZE_PX, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1F6B43.toInt()
    }
    canvas.drawCircle(AVATAR_SIZE_PX / 2f, AVATAR_SIZE_PX / 2f, AVATAR_SIZE_PX / 2f, paint)

    paint.apply {
        color = android.graphics.Color.WHITE
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = AVATAR_SIZE_PX * 0.48f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val baseline = AVATAR_SIZE_PX / 2f - (paint.ascent() + paint.descent()) / 2f
    canvas.drawText(initial, AVATAR_SIZE_PX / 2f, baseline, paint)
    return bitmap.also { avatarCache[cacheKey] = it }
}

private fun publishChatShortcut(
    context: Context,
    key: String,
    title: String,
    sender: Person,
    avatar: Bitmap?,
    openIntent: Intent
): String? = runCatching<String?> {
    val id = "chat_$key"
    val shortcutIntent = Intent(openIntent).apply {
        setClass(context, MainActivity::class.java)
        // Android rejects dynamic shortcut launch intents without an action.
        // The old failure was swallowed, so the notification never qualified
        // for the native Conversation template.
        action = Intent.ACTION_VIEW
    }
    val shortcut = ShortcutInfoCompat.Builder(context, id)
        .setShortLabel(title)
        .setLongLabel(title)
        .setLongLived(true)
        .setIntent(shortcutIntent)
        .setPerson(sender)
        .setCategories(setOf("android.shortcut.conversation"))
        .apply { avatar?.let { setIcon(IconCompat.createWithBitmap(it)) } }
        .build()
    if (ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)) id else null
}.onFailure {
    Log.w(TAG, "Unable to publish conversation shortcut for $key", it)
}.getOrNull()

class MarkChatReadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getStringExtra("message_id") ?: return
        apiPost(context, "/messages/$messageId/read", JSONObject())
    }
}

class ChatReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reply = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(REPLY_KEY)?.toString()?.trim().orEmpty()
        if (reply.isBlank()) return
        val json = JSONObject().apply {
            put("receiver_id", intent.getStringExtra("receiver_id")?.toIntOrNull() ?: 0)
            put("message", reply)
        }
        val pending = goAsync()
        Thread {
            try {
                val sent = apiPostSync(context, "/messages/${intent.getStringExtra("item_id")}", json)
                val itemId = intent.getStringExtra("item_id").orEmpty()
                val senderId = intent.getStringExtra("receiver_id").orEmpty()
                if (sent) {
                    // Reposting is not needed after a successful send. Canceling
                    // the card explicitly stops Android's inline-reply spinner.
                    context.getSystemService(NotificationManager::class.java)
                        .cancel("$itemId:$senderId".hashCode())
                }
            } finally {
                pending.finish()
            }
        }.start()
    }
}

private fun apiPost(context: Context, path: String, json: JSONObject) {
    val prefs = context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE)
    val token = prefs.getString("auth_token", "") ?: return
    Thread {
        val request = Request.Builder()
            .url(API_BASE + path)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()
        runCatching { OkHttpClient().newCall(request).execute().close() }
    }.start()
}

private fun apiPostSync(context: Context, path: String, json: JSONObject): Boolean {
    val prefs = context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE)
    val token = prefs.getString("auth_token", "") ?: return false
    val request = Request.Builder()
        .url(API_BASE + path)
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(json.toString().toRequestBody("application/json".toMediaType()))
        .build()
    return runCatching {
        OkHttpClient().newCall(request).execute().use { it.isSuccessful }
    }.getOrDefault(false)
}
