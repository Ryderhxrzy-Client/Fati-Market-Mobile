package com.fati_market

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.fati_market.ui.components.PrimaryButton
import com.fati_market.ui.components.StatusTone
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The in-app notification banner.
 *
 * Sits above the whole app and shows pushes that arrive while the user is
 * already inside it, instead of a system notification covering the screen they
 * are looking at.
 *
 * A chat banner can be replied to without leaving the current screen, which is
 * the whole point of showing it here rather than sending the user away.
 */
@Composable
fun InAppNotificationHost(
    modifier: Modifier = Modifier,
    onOpenChat: (itemId: Int, senderId: Int) -> Unit = { _, _ -> },
    onOpenOrder: (transactionId: Int) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val token = remember {
        context.getSharedPreferences("fatimarket_prefs", 0).getString("auth_token", "") ?: ""
    }

    var current by remember { mutableStateOf<InAppNotification?>(null) }
    var replying by remember { mutableStateOf(false) }

    // Track foreground state so the messaging service knows to route here.
    DisposableEffect(Unit) {
        InAppNotifications.onEnterForeground()
        onDispose { InAppNotifications.onEnterBackground() }
    }

    LaunchedEffect(Unit) {
        InAppNotifications.events.collect { notification ->
            replying = false
            current = notification
        }
    }

    // Auto-dismiss, unless the user has opened the reply box - pulling a
    // half-typed reply out from under them would be worse than lingering.
    LaunchedEffect(current?.key, replying) {
        val showing = current ?: return@LaunchedEffect
        if (replying) return@LaunchedEffect

        delay(if (showing is InAppNotification.Chat) 6000 else 5000)
        if (!replying) current = null
    }

    Box(modifier = modifier.fillMaxSize()) {
        content()

        AnimatedVisibility(
            visible = current != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(10f),
        ) {
            when (val notification = current) {
                is InAppNotification.Chat -> ChatBanner(
                    notification = notification,
                    token = token,
                    replying = replying,
                    onStartReply = { replying = true },
                    onDismiss = { current = null; replying = false },
                    onOpen = {
                        current = null
                        replying = false
                        onOpenChat(notification.itemId, notification.senderId)
                    },
                )

                is InAppNotification.Order -> OrderBanner(
                    notification = notification,
                    onDismiss = { current = null },
                    onOpen = {
                        current = null
                        onOpenOrder(notification.transactionId)
                    },
                )

                null -> Unit
            }
        }
    }
}

/** Shared shell: status-bar inset, rounded card, strong shadow. */
@Composable
private fun BannerShell(
    containerColor: Color,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        // Sits over arbitrary app content, so it needs a real shadow to read as
        // a separate layer rather than part of the screen underneath.
        shadowElevation = 12.dp,
        tonalElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            content = content,
        )
    }
}

@Composable
private fun ChatBanner(
    notification: InAppNotification.Chat,
    token: String,
    replying: Boolean,
    onStartReply: () -> Unit,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var reply by remember(notification.key) { mutableStateOf("") }
    var sending by remember(notification.key) { mutableStateOf(false) }
    var sent by remember(notification.key) { mutableStateOf(false) }
    var error by remember(notification.key) { mutableStateOf<String?>(null) }

    fun send() {
        if (reply.isBlank() || sending) return

        scope.launch {
            sending = true
            error = null

            val result = withContext(Dispatchers.IO) {
                MarketplaceApi.sendChatReply(
                    token = token,
                    itemId = notification.itemId,
                    receiverId = notification.senderId,
                    message = reply.trim(),
                )
            }
            sending = false

            when (result) {
                is MarketplaceApi.Result.Ok -> {
                    sent = true
                    reply = ""
                    delay(900)
                    onDismiss()
                }
                is MarketplaceApi.Result.Failure -> error = result.message
            }
        }
    }

    // Surface, not surfaceVariant: the banner must stay legible over whatever
    // screen it lands on, including a photo-heavy catalog.
    BannerShell(containerColor = MaterialTheme.colorScheme.surface, onDismiss = onDismiss) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !replying, onClick = onOpen),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (notification.senderPhoto != null) {
                AsyncImage(
                    model = notification.senderPhoto,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        notification.senderName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notification.senderName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (notification.itemTitle.isNotBlank()) {
                    Text(
                        notification.itemTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        when {
            sent -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Icon(
                    Icons.Filled.Check,
                    null,
                    tint = LocalMarketAccents.current.success,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    "Reply sent",
                    style = MaterialTheme.typography.labelMedium,
                    color = LocalMarketAccents.current.success,
                )
            }

            replying -> Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedTextField(
                    value = reply,
                    onValueChange = { reply = it; error = null },
                    placeholder = { Text("Type your reply") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    maxLines = 3,
                    enabled = !sending,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { send() }),
                    trailingIcon = {
                        IconButton(onClick = { send() }, enabled = reply.isNotBlank() && !sending) {
                            if (sending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Send,
                                    contentDescription = "Send reply",
                                    tint = if (reply.isNotBlank()) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }
                    },
                )

                error?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            else -> Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                TextButton(onClick = onStartReply) {
                    Icon(Icons.Filled.Reply, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(Spacing.xs))
                    Text("Reply", style = MaterialTheme.typography.labelLarge)
                }
                TextButton(onClick = onOpen) {
                    Text("Open chat", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun OrderBanner(
    notification: InAppNotification.Order,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
) {
    val accents = LocalMarketAccents.current

    // Tone the icon by what happened, so a verified payment reads differently
    // from a new order at a glance.
    val (icon, tint) = when {
        notification.kind == "order_placed" -> Icons.Filled.ShoppingCart to accents.info
        notification.body.contains("declin", true) ||
            notification.body.contains("cancel", true) -> Icons.Filled.ErrorOutline to
            MaterialTheme.colorScheme.error
        notification.body.contains("verif", true) ||
            notification.body.contains("complet", true) -> Icons.Filled.CheckCircle to accents.success
        else -> Icons.Filled.Notifications to accents.info
    }

    BannerShell(containerColor = MaterialTheme.colorScheme.surface, onDismiss = onDismiss) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
