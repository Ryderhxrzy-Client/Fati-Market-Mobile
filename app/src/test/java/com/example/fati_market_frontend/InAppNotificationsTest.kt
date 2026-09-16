package com.fati_market

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.*
import org.junit.Test

class InAppNotificationsTest {
    @Test
    fun pushAndPollingForSameChatProduceOneBanner() = runBlocking {
        val chat = InAppNotification.Chat(90001, 20, "Item", 30, "Seller", null, "Hello")
        InAppNotifications.post(chat)
        InAppNotifications.post(chat.copy())
        assertEquals(chat, withTimeout(1000) { InAppNotifications.events.first() })
        assertNull(withTimeoutOrNull(50) { InAppNotifications.events.first() })
    }

    @Test
    fun alreadyShownBackgroundPushIsNotRepeatedByPolling() = runBlocking {
        assertTrue(InAppNotifications.claimChat(90002))
        InAppNotifications.post(InAppNotification.Chat(90002, 20, "Item", 30, "Seller", null, "Hello"))
        assertNull(withTimeoutOrNull(50) { InAppNotifications.events.first() })
    }

    @Test
    fun arrivalBeforeBannerStartsIsDeliveredOnlyOnce() = runBlocking {
        val notification = InAppNotification.Order(1, 2, "Updated", "Ready", "order_update")
        InAppNotifications.post(notification)
        assertEquals(notification, withTimeout(1000) { InAppNotifications.events.first() })
        assertNull(withTimeoutOrNull(50) { InAppNotifications.events.first() })
    }

    @Test
    fun backendChatPayloadContainsReplyAndNavigationDetails() {
        val notification = InAppNotifications.fromPush(mapOf(
            "type" to "chat_message", "message_id" to "10", "item_id" to "20",
            "sender_id" to "30", "sender_name" to "Seller", "message" to "Hello",
        )) as InAppNotification.Chat
        assertEquals(10, notification.messageId)
        assertEquals(20, notification.itemId)
        assertEquals(30, notification.senderId)
        assertEquals("Hello", notification.body)
    }

    @Test
    fun allBackendOrderAndItemEventsProduceBanners() {
        for (type in listOf("order_placed", "payment_proof_submitted", "order_update", "item_update" /* , "meetup_reminder" - BOOKING/SCHEDULE DISABLED */)) {
            val notification = InAppNotifications.fromPush(mapOf(
                "type" to type, "title" to "Update", "body" to "Details",
            )) as InAppNotification.Order
            assertEquals(type, notification.kind)
            assertEquals("Details", notification.body)
        }
    }
}
