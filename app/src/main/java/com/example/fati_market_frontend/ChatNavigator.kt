package com.fati_market

import kotlinx.coroutines.flow.MutableSharedFlow

internal data class OpenChatEvent(
    val itemId: Int,
    val otherUserId: Int = 0,
    val itemTitle: String = "",
    val itemPhoto: String = "",
)

internal object ChatNavigator {
    val events = MutableSharedFlow<OpenChatEvent>(extraBufferCapacity = 8)

    fun open(itemId: Int, otherUserId: Int = 0, itemTitle: String = "", itemPhoto: String = "") {
        events.tryEmit(OpenChatEvent(itemId, otherUserId, itemTitle, itemPhoto))
    }
}

