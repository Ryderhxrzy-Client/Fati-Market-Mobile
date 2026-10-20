package com.fati_market

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import compose.icons.LucideIcons
import compose.icons.lucideicons.ShoppingBag
import com.fati_market.ui.components.InfoBanner
import com.fati_market.ui.components.ItemStatusPill
import com.fati_market.ui.components.LoadingState
import com.fati_market.ui.components.MarketPageTopBar
import com.fati_market.ui.components.MarketPanel
import com.fati_market.ui.components.PagerDots
import com.fati_market.ui.components.PriceSize
import com.fati_market.ui.components.PriceTag
import com.fati_market.ui.components.PrimaryButton
import com.fati_market.ui.components.RewardChip
import com.fati_market.ui.components.RoundIconButton
import com.fati_market.ui.components.SoftDivider
import com.fati_market.ui.components.StatusTone
import com.fati_market.ui.components.StoreLogo
import com.fati_market.ui.theme.Elevation
import com.fati_market.ui.theme.FavoriteRed
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Item details screen displayed as a full-screen dialog overlay.
 * Uses Scaffold to place the purchase bottom bar identical in position,
 * height (72.dp), and insets to MarketBottomBar.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ItemDetailDialog(
    item: Item,
    token: String,
    isFavorited: Boolean,
    onFavoriteToggle: (itemId: Int, nowFav: Boolean) -> Unit,
    onGoToChat: () -> Unit,
    onDismiss: () -> Unit,
    /** Opens checkout. Absent for surfaces where buying does not apply. */
    onBuyNow: (Item) -> Unit = {},
    /** Used to stop a seller buying their own listing. */
    currentUserId: Int = 0
) {
    val scope = rememberCoroutineScope()
    var detailItem        by remember { mutableStateOf(item) }
    var isLoading         by remember { mutableStateOf(true) }
    var isFav             by remember { mutableStateOf(isFavorited) }
    var isToggling        by remember { mutableStateOf(false) }
    var showImageViewer   by remember { mutableStateOf(false) }
    var messageText       by remember { mutableStateOf("Available pa ba?") }
    var isSending         by remember { mutableStateOf(false) }
    var showSentDialog    by remember { mutableStateOf(false) }
    var sendError         by remember { mutableStateOf<String?>(null) }
    val accents = LocalMarketAccents.current

    LaunchedEffect(item.itemId) {
        val detail    = withContext(Dispatchers.IO) { fetchItemDetail(token, item.itemId) }
        val favStatus = withContext(Dispatchers.IO) { checkFavorite(token, item.itemId) }
        if (detail != null) {
            detailItem = detail
        }
        isFav     = favStatus
        isLoading = false
    }

    val photoPager = rememberPagerState(pageCount = { detailItem.photos.size.coerceAtLeast(1) })

    fun sendMessage() {
        if (messageText.isBlank()) {
            sendError = "Please enter a message first."
            return
        }
        scope.launch {
            isSending = true
            sendError = null
            val ok = withContext(Dispatchers.IO) {
                sendMessageToAdmin(token, detailItem.itemId, messageText.trim())
            }
            isSending = false
            if (ok) { showSentDialog = true; messageText = "Available pa ba?" }
            else sendError = "Failed to send. Please try again."
        }
    }

    // ── Success dialog ──────────────────────────────────────────────────────────
    if (showSentDialog) {
        AlertDialog(
            onDismissRequest = { showSentDialog = false },
            icon = {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title   = { Text("Message sent") },
            text    = { Text("Ofelia's Store will reply in your chat.") },
            confirmButton = {
                PrimaryButton(text = "Open chat", compact = true, onClick = { showSentDialog = false; onGoToChat() })
            },
            dismissButton = {
                TextButton(onClick = { showSentDialog = false }) { Text("Close") }
            }
        )
    }

    // Full-screen image viewer. Tapping the main image opens this view.
    if (showImageViewer && detailItem.photos.isNotEmpty()) {
        Dialog(
            onDismissRequest = { showImageViewer = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = detailItem.photos[photoPager.currentPage.coerceIn(0, detailItem.photos.lastIndex)],
                        contentDescription = "Full-screen item image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showImageViewer = false }
                    )
                    IconButton(
                        onClick = { showImageViewer = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .safeAreaTop()
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Filled.Close, "Close image", tint = Color.White)
                    }
                    if (detailItem.photos.size > 1) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .safeAreaBottom()
                                .padding(bottom = 24.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = CircleShape
                        ) {
                            Text(
                                "${photoPager.currentPage + 1} / ${detailItem.photos.size}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    BackHandler(onBack = onDismiss)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    MarketPageTopBar(title = "Item details", onBack = onDismiss) {
                        if (!isLoading) {
                            IconButton(
                                onClick = {
                                    if (!isToggling) {
                                        isToggling = true
                                        scope.launch {
                                            val nowFav = !isFav
                                            val ok = withContext(Dispatchers.IO) {
                                                if (nowFav) addFavorite(token, detailItem.itemId)
                                                else removeFavorite(token, detailItem.itemId)
                                            }
                                            if (ok) { isFav = nowFav; onFavoriteToggle(detailItem.itemId, nowFav) }
                                            isToggling = false
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = if (isFav) "Remove from favourites" else "Add to favourites",
                                    tint = if (isFav) FavoriteRed else accents.onBrand
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    if (!isLoading && detailItem.isPublic) {
                        ItemDetailPurchaseBar(
                            item = detailItem,
                            onBuyNow = { onBuyNow(detailItem) },
                        )
                    }
                },
                contentWindowInsets = WindowInsets(0),
                containerColor = MaterialTheme.colorScheme.background,
            ) { innerPadding ->
                if (isLoading) {
                    LoadingState(modifier = Modifier.padding(innerPadding))
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        // ── Images ─────────────────────────────────────────────
                        if (detailItem.photos.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                HorizontalPager(state = photoPager, modifier = Modifier.fillMaxSize()) { page ->
                                    AsyncImage(
                                        model              = detailItem.photos[page],
                                        contentDescription = null,
                                        contentScale       = ContentScale.Crop,
                                        modifier           = Modifier
                                            .fillMaxSize()
                                            .clickable { showImageViewer = true }
                                    )
                                }
                                if (detailItem.photos.size > 1) {
                                    PagerDots(
                                        count = detailItem.photos.size,
                                        selectedIndex = photoPager.currentPage,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = Spacing.md)
                                            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                                            .padding(horizontal = Spacing.sm, vertical = 5.dp),
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(Spacing.md),
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.45f),
                                ) {
                                    Icon(
                                        Icons.Filled.Fullscreen, "Open full screen",
                                        tint = Color.White,
                                        modifier = Modifier.padding(6.dp).size(18.dp),
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Image, null,
                                    tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(64.dp))
                            }
                        }

                        // ── Info ───────────────────────────────────────────────
                        Column(
                            modifier            = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                ItemStatusPill(detailItem.status)
                                RewardChip(points = detailItem.rewardPoints, compact = true)
                            }

                            Text(
                                text = detailItem.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )

                            PriceTag(detailItem.displayPrice, size = PriceSize.Large)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                            ) {
                                Icon(
                                    Icons.Filled.Storefront,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(17.dp),
                                )
                                Text(
                                    text = "Available for pickup at Ofelia's Store",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            // -- Availability ---------------------------------
                            if (detailItem.isReserved && detailItem.reservedByMe) {
                                InfoBanner(
                                    title = "You reserved this item",
                                    text  = "It is being held for you. Finish paying in " +
                                            "My Orders, then show your pickup code at the store.",
                                    tone = StatusTone.Success,
                                    icon = Icons.Filled.Lock
                                )
                            } else if (detailItem.isReserved) {
                                InfoBanner(
                                    text = "Someone is already checking this item out. " +
                                            "It becomes available again if their reservation expires.",
                                    tone = StatusTone.Warning,
                                    icon = Icons.Filled.Lock
                                )
                            } else if (detailItem.isSold) {
                                InfoBanner(
                                    text = "This item has been sold.",
                                    tone = StatusTone.Neutral,
                                    icon = Icons.Filled.Inventory
                                )
                            }

                            SoftDivider()

                            val storeOwned = detailItem.isAcquired || detailItem.isPublic ||
                                detailItem.isReserved || detailItem.isSold

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                                        RoundedCornerShape(18.dp),
                                    )
                                    .padding(Spacing.md),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                if (storeOwned) {
                                    StoreLogo(Modifier.size(40.dp), shape = CircleShape)
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Filled.Person,
                                            null,
                                            tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        if (storeOwned) "Ofelia's Store" else detailItem.sellerEmail,
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Text(
                                        if (storeOwned) "Pick up at the store on campus" else "Student seller",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            SoftDivider()

                            // Description
                            MarketPanel(containerColor = MaterialTheme.colorScheme.surface) {
                                Text("Product description", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    detailItem.description.ifBlank { "No description provided." },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            SoftDivider()

                            // ── Send message ──────────────────────────────────
                            Text("Ask Ofelia's Store", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Need more details before ordering? Send the store a quick message.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            sendError?.let { err ->
                                Text(
                                    text  = err,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                OutlinedTextField(
                                    value         = messageText,
                                    onValueChange = { messageText = it; sendError = null },
                                    placeholder   = { Text("Available pa ba?") },
                                    modifier      = Modifier.weight(1f),
                                    shape         = RoundedCornerShape(18.dp),
                                    maxLines      = 3,
                                    textStyle     = MaterialTheme.typography.bodyMedium,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { if (!isSending) sendMessage() }),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    ),
                                )
                                RoundIconButton(
                                    icon = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    onClick = { sendMessage() },
                                    loading = isSending,
                                    size = 48.dp,
                                )
                            }

                            Spacer(Modifier.height(Spacing.lg))
                        }
                    }
                }
            }
        }
    }

/**
 * Pinned purchase bar at the bottom of the Item Details screen.
 * Formatted identical to MarketBottomBar:
 * - Full-width Surface with rounded top corners (24.dp)
 * - 72.dp height content Row
 * - safeAreaBarBottom(floor = 12.dp) for system bar clearance
 */
@Composable
private fun ItemDetailPurchaseBar(
    item: Item,
    onBuyNow: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Elevation.flat,
        shadowElevation = 8.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .safeAreaBarBottom(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = Spacing.screen, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    Text(
                        text = "Total amount:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PriceTag(item.displayPrice, size = PriceSize.Medium)
                }
                Spacer(Modifier.width(Spacing.md))
                PrimaryButton(
                    text = "Buy now",
                    icon = LucideIcons.ShoppingBag,
                    onClick = onBuyNow,
                    compact = true,
                )
            }
        }
    }
}
