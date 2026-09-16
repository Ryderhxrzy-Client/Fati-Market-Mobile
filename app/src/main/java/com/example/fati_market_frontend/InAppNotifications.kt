package com.fati_market

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Routes incoming pushes to the right place depending on where the user is.
 *
 * A system notification while the user is already looking at the app is noise -
 * it covers the screen they are using to say something they can see. So the
 * messaging service checks [isForeground] and, when the app is on screen, emits
 * here instead for the in-app banner to render.
 *
 * The service and the UI share one process, so a plain object holds this state.
 */
object InAppNotifications {

    private val foreground = AtomicBoolean(false)

    /** True while an activity is resumed. */
    val isForeground: Boolean get() = foreground.get()

    fun onEnterForeground() = foreground.set(true)

    fun onEnterBackground() = foreground.set(false)

    // Keep arrivals during activity/Compose startup until the single banner
    // host is collecting. SharedFlow(replay = 0) loses those arrivals.
    private val _events = Channel<InAppNotification>(
        capacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val events: Flow<InAppNotification> = _events.receiveAsFlow()

    private val deliveredChats = linkedSetOf<Int>()

    @Synchronized
    fun claimChat(messageId: Int): Boolean {
        if (messageId <= 0) return true
        if (!deliveredChats.add(messageId)) return false
        while (deliveredChats.size > 256) deliveredChats.remove(deliveredChats.first())
        return true
    }

    fun post(notification: InAppNotification) {
        if (notification is InAppNotification.Chat && !claimChat(notification.messageId)) return
        _events.trySend(notification)
    }

    /** Build one from an FCM data payload, or null if it is not one we show. */
    fun fromPush(data: Map<String, String>): InAppNotification? = when (data["type"]) {
        "chat_message" -> InAppNotification.Chat(
            messageId = data["message_id"]?.toIntOrNull() ?: 0,
            itemId = data["item_id"]?.toIntOrNull() ?: 0,
            itemTitle = data["item_title"].orEmpty(),
            senderId = data["sender_id"]?.toIntOrNull() ?: 0,
            senderName = data["sender_name"].orEmpty().ifBlank { "New message" },
            senderPhoto = data["sender_profile_picture"]?.takeIf { it.isNotBlank() },
            body = data["message"].orEmpty(),
        )

        // Item events share the order banner shape: a headline and a body.
        "order_placed", "payment_proof_submitted", "order_update",
        "item_update" /* , "meetup_reminder" - BOOKING/SCHEDULE DISABLED */ -> InAppNotification.Order(
            transactionId = data["transaction_id"]?.toIntOrNull() ?: 0,
            itemId = data["item_id"]?.toIntOrNull() ?: 0,
            title = data["title"].orEmpty().ifBlank { "Order update" },
            body = data["body"].orEmpty().ifBlank { data["item_title"].orEmpty() },
            kind = data["type"].orEmpty(),
        )

        else -> null
    }
}

/** What the in-app banner can show. */
sealed class InAppNotification {

    /** A unique key so the host can dismiss the right banner. */
    abstract val key: Long

    /** A chat message. Carries what an inline reply needs to be sent back. */
    data class Chat(
        val messageId: Int,
        val itemId: Int,
        val itemTitle: String,
        val senderId: Int,
        val senderName: String,
        val senderPhoto: String?,
        val body: String,
        override val key: Long = System.nanoTime(),
    ) : InAppNotification()

    /** An order or payment update. */
    data class Order(
        val transactionId: Int,
        val itemId: Int,
        val title: String,
        val body: String,
        val kind: String,
        override val key: Long = System.nanoTime(),
    ) : InAppNotification()
}
