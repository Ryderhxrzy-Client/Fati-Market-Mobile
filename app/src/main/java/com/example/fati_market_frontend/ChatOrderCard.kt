package com.fati_market

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * An order, drawn inside the conversation it belongs to.
 *
 * A checkout used to arrive as a paragraph addressed to the store, which read
 * as nonsense in the buyer's own outgoing bubble. This is the card instead:
 * the item and a photo that opens the listing, what is owed, how it is being
 * paid, and - the part the buyer actually came to check - whether the payment
 * has gone through yet.
 *
 * The order travels with the message and is refreshed by the chat's own poll,
 * so a card posted at checkout starts out unpaid and turns paid on its own the
 * moment Admin verifies it. Admin sees the same card with the decisions
 * attached, taken from the server's `available_actions`, so settling an order
 * here and settling it on the orders screen are the same act.
 */
@Composable
internal fun ChatOrderCard(msg: ChatMessage, isMe: Boolean) {
    val order = msg.order ?: return
    val context = LocalContext.current
    val accents = LocalMarketAccents.current

    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE) }
    val token = remember { prefs.getString("auth_token", "") ?: "" }
    val isAdmin = remember { (prefs.getString("user_role", "") ?: "").equals("admin", true) }

    var showItem by remember { mutableStateOf(false) }
    var showProof by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<String?>(null) }
    var payingNow by remember { mutableStateOf(false) }

    if (showItem) {
        ChatItemPreviewDialog(
            itemId = order.itemId,
            token = token,
            fallbackTitle = order.itemTitle,
            fallbackPhoto = order.itemPhoto,
            onDismiss = { showItem = false },
        )
    }

    if (showProof && order.paymentProof != null) {
        PaymentProofViewer(
            imageUrl = order.paymentProof,
            reference = order.paymentReference,
            onDismiss = { showProof = false },
        )
    }

    if (payingNow) {
        PaymentProofDialog(
            transaction = order,
            token = token,
            onDismiss = { payingNow = false },
            // The thread re-polls every few seconds, so the card flips to
            // "Checking payment" on its own once the receipt is up.
            onSubmitted = { payingNow = false },
        )
    }

    pendingAction?.let { action ->
        ChatOrderActionDialog(
            transaction = order,
            action = action,
            token = token,
            onDismiss = { pendingAction = null },
            // The thread re-polls every few seconds, so the card redraws itself
            // with the new status without anything being pushed back up here.
            onDone = { pendingAction = null },
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isMe) 18.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 18.dp,
            ),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
        ) {
            Column {
                // The heading sits on its own band, so the card announces
                // itself as an order before any detail is read.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                ) {
                    OrderCardHeading(msg.kind, order)
                }

                Column(
                    modifier = Modifier.padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {

                // ── The item, and the way through to it ──────────────────
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable { showItem = true }
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ItemThumbnail(order.itemPhoto)

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            order.itemTitle,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            Money.format(order.amountDue) + " due",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "Tap to view item",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                SoftDivider()

                // ── The money ────────────────────────────────────────────
                SummaryRow("Price", Money.format(order.subtotal))

                if (order.pointsUsed > 0) {
                    SummaryRow(
                        label = "${order.pointsUsed} point(s) used",
                        value = "−" + Money.format(order.pointsDiscountAmount),
                        valueColor = accents.reward,
                    )
                }

                SummaryRow("Amount due", Money.format(order.amountDue), emphasized = true)
                SummaryRow("Payment", paymentMethodLabel(order.paymentMethod))

                // ── Paid, or not ─────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PaymentStatusPill(order.paymentStatus, order.isFullPointsCheckout)
                    TransactionStatusPill(order.status)
                }

                // ── The GCash receipt, when one has been sent ────────────
                if (msg.kind == "payment_submitted" || order.paymentProof != null) {
                    order.paymentReference?.let { reference ->
                        SummaryRow("Reference", reference)
                    }

                    order.paymentProof?.let { proof ->
                        AsyncImage(
                            model = proof,
                            contentDescription = "Payment receipt",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(MaterialTheme.shapes.small)
                                .clickable { showProof = true },
                        )
                        Text(
                            "Tap the receipt to see it in full",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // ── An admin decision, in the buyer's own thread ─────────
                if (msg.kind == "order_update") {
                    InfoBanner(
                        text = msg.message,
                        tone = when (order.status) {
                            "cancelled", "rejected" -> StatusTone.Danger
                            "completed" -> StatusTone.Success
                            else -> StatusTone.Info
                        },
                    )
                }

                // Every card in a thread points at the same order, so only one
                // of them offers the decisions: the receipt if the buyer sent
                // one, otherwise the order itself. Repeating the buttons on
                // each card would only make Ofelia wonder which is live.
                val carriesActions = msg.kind == "payment_submitted" ||
                    (msg.kind == "order_placed" && order.paymentProof == null)

                // The buyer's side of the same card: a GCash bill still open
                // is payable right here, receipt upload and all.
                val buyerCanPay = !isAdmin &&
                    order.paymentMethod == "gcash" &&
                    !order.isTerminal &&
                    (order.paymentStatus == "unpaid" || order.paymentStatus == "rejected")

                if (buyerCanPay && carriesActions) {
                    SoftDivider()
                    PrimaryButton(
                        text = if (order.paymentStatus == "rejected") "Send payment again" else "Pay with GCash",
                        onClick = { payingNow = true },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.QrCode2,
                    )
                }

                if (isAdmin && carriesActions) {
                    OrderCardActions(order) { pendingAction = it }
                }

                Text(
                    timeAgo(msg.sentAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
                }
            }
        }
    }
}

/**
 * A fresh listing, drawn as an offer inside the seller's conversation.
 *
 * Posting an item opens this thread automatically, so the seller sees their
 * offer sitting with Ofelia's store the moment it is up, and Admin can settle
 * it without leaving the chat: agree an acquisition price, or turn it down
 * with a reason. The card reads the live item, so the seller's copy flips to
 * "In stock" or "Rejected" as soon as Admin decides.
 */
@Composable
internal fun ItemOfferCard(msg: ChatMessage, isMe: Boolean) {
    val listing = msg.listedItem ?: return
    val context = LocalContext.current
    val accents = LocalMarketAccents.current

    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE) }
    val token = remember { prefs.getString("auth_token", "") ?: "" }
    val isAdmin = remember { (prefs.getString("user_role", "") ?: "").equals("admin", true) }

    val scope = rememberCoroutineScope()
    var showItem by remember { mutableStateOf(false) }
    var offerAction by remember { mutableStateOf<String?>(null) }
    var confirmAcquire by remember { mutableStateOf(false) }
    var acquireError by remember { mutableStateOf<String?>(null) }

    fun pickSchedule() = pickMeetupSchedule(context, scope, token, listing.itemId)

    if (showItem) {
        ChatItemPreviewDialog(
            itemId = listing.itemId,
            token = token,
            fallbackTitle = listing.title,
            fallbackPhoto = listing.photos.firstOrNull(),
            onDismiss = { showItem = false },
        )
    }

    if (confirmAcquire) {
        AcquireOfferDialog(
            listing = listing,
            token = token,
            onDismiss = { confirmAcquire = false },
            // The chat's poll repaints the card as acquired.
            onFinished = { error ->
                confirmAcquire = false
                acquireError = error
            },
        )
    }

    offerAction?.let { action ->
        ItemOfferActionDialog(
            listing = listing,
            action = action,
            token = token,
            onDismiss = { offerAction = null },
            // The chat's own poll redraws the card with the new status.
            onDone = { offerAction = null },
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isMe) 18.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 18.dp,
            ),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Sell,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text("Item offer", style = MaterialTheme.typography.labelLarge)
                        }

                        Text(
                            "Item #${listing.itemId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Row(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable { showItem = true }
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ItemThumbnail(listing.photos.firstOrNull())

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                listing.title,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "Asking " + Money.format(listing.sellerAskingPrice),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "Tap to view item",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    SoftDivider()

                    SummaryRow("Asking price", Money.format(listing.sellerAskingPrice), emphasized = true)

                    listing.acquisitionPrice?.let { agreed ->
                        SummaryRow(
                            label = "Store offer",
                            value = Money.format(agreed),
                            valueColor = accents.success,
                        )
                    }

                    listing.meetupSchedule?.let { schedule ->
                        Dates.short(schedule)?.let { SummaryRow("Meet-up", it) }
                    }

                    ItemStatusPill(listing.status, offerAccepted = listing.offerAccepted)

                    listing.rejectedReason?.let { reason ->
                        InfoBanner(
                            title = "Offer declined",
                            text = reason,
                            tone = StatusTone.Danger,
                            icon = Icons.Filled.ErrorOutline,
                        )
                    }

                    // Accepted: the seller carries the QR from here.
                    if (!isAdmin && listing.qrCode != null) {
                        InfoBanner(
                            title = "Offer accepted",
                            text = "Bring the item to the store and show your turnover code.",
                            tone = StatusTone.Success,
                            icon = Icons.Filled.CheckCircle,
                        )
                        ItemQrButton(item = listing, modifier = Modifier.fillMaxWidth())
                    }

                    // The review is Admin's, and only while the offer is open.
                    if (isAdmin && listing.isPending) {
                        SoftDivider()

                        if (listing.offerAccepted) {
                            // Accepted already; what is left is when the
                            // seller comes in - and, when they are already
                            // here, receiving the item. The scan flow does
                            // this with photographs; this button is the
                            // manual twin, like Complete has on an order.
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                SecondaryButton(
                                    text = if (listing.meetupSchedule == null) "Set schedule" else "Change schedule",
                                    onClick = { pickSchedule() },
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Filled.Event,
                                )

                                PrimaryButton(
                                    text = "Mark acquired",
                                    onClick = { confirmAcquire = true },
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Filled.Inventory,
                                    containerColor = accents.success,
                                )
                            }

                            acquireError?.let {
                                InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                PrimaryButton(
                                    text = "Accept",
                                    onClick = { offerAction = "accept" },
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Filled.Check,
                                    containerColor = accents.success,
                                )
                                SecondaryButton(
                                    text = "Reject",
                                    onClick = { offerAction = "reject" },
                                    modifier = Modifier.weight(1f),
                                    contentColor = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }

                    Text(
                        timeAgo(msg.sentAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

/**
 * Date then time via the platform pickers, posted as the meet-up the
 * 6h/1h/30m reminders count down from. Failures surface as a toast; success
 * shows up when the chat poll repaints whatever invoked this.
 */
internal fun pickMeetupSchedule(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    token: String,
    itemId: Int,
) {
    val calendar = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.DAY_OF_YEAR, 1)
    }

    android.app.DatePickerDialog(
        context,
        { _, year, month, day ->
            android.app.TimePickerDialog(
                context,
                { _, hour, minute ->
                    val schedule = String.format(
                        java.util.Locale.US,
                        "%04d-%02d-%02d %02d:%02d:00",
                        year, month + 1, day, hour, minute,
                    )

                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            MarketplaceApi.setMeetupSchedule(token, itemId, schedule)
                        }

                        if (result is MarketplaceApi.Result.Failure) {
                            android.widget.Toast
                                .makeText(context, result.message, android.widget.Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                },
                10,
                0,
                false,
            ).show()
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH),
    ).show()
}

/**
 * The acquire, confirmed with proof - one dialog for the conversation card,
 * the pinned strip and the Private Offers screen, so "Mark Acquired" is the
 * same act everywhere.
 *
 * Mirrors the QR turnover screen: the two proof photographs (the item being
 * received, the seller being paid) are captured right here, optional but
 * offered. When no price was ever agreed in chat, the agreed amount is asked
 * for in the same breath, since verifying a turnover without a price would be
 * receiving an item nobody priced.
 */
@Composable
internal fun AcquireOfferDialog(
    listing: Item,
    token: String,
    onDismiss: () -> Unit,
    onFinished: (errorMessage: String?) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val needsPrice = listing.acquisitionPrice == null
    var priceInput by remember {
        mutableStateOf(
            if (needsPrice) Money.formatPlain(listing.sellerAskingPrice).replace(",", "").takeIf { it != "—" } ?: ""
            else ""
        )
    }

    var itemPhoto by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var payoutPhoto by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var capturing by remember { mutableStateOf("item") }
    var acquiring by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val takePhoto = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap != null) {
            if (capturing == "item") itemPhoto = bitmap else payoutPhoto = bitmap
        }
    }

    val askCameraPermission = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) takePhoto.launch(null)
        else error = "Camera permission is needed to photograph the proof."
    }

    fun capture(slot: String) {
        capturing = slot
        askCameraPermission.launch(android.Manifest.permission.CAMERA)
    }

    AlertDialog(
        onDismissRequest = { if (!acquiring) onDismiss() },
        title = { Text("Mark as acquired", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Text(
                    if (needsPrice) {
                        "Confirm \"${listing.title}\" is physically in the store and " +
                            "the seller is being paid. The seller asked " +
                            Money.format(listing.sellerAskingPrice) + "."
                    } else {
                        "Confirm the item is physically in the store and the seller was " +
                            "handed " + Money.format(listing.acquisitionPrice) + "."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )

                if (needsPrice) {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it; error = null },
                        label = { Text("Agreed price") },
                        prefix = { Text(Money.PESO) },
                        placeholder = { Text("What the store pays the seller") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                    )
                }

                // The same proof the QR turnover takes, captured in place.
                AcquireProofRow("Item received", itemPhoto) { capture("item") }
                AcquireProofRow("Seller paid", payoutPhoto) { capture("payout") }

                error?.let {
                    InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = if (needsPrice) Money.normalizeInput(priceInput) else null

                    if (needsPrice && price == null) {
                        error = "Enter a valid peso amount, e.g. 180 or 179.50."
                        return@Button
                    }

                    scope.launch {
                        acquiring = true
                        error = null

                        val result = withContext(Dispatchers.IO) {
                            fun android.graphics.Bitmap.toCacheFile(prefix: String): java.io.File {
                                val file = java.io.File.createTempFile(prefix, ".jpg", context.cacheDir)
                                file.outputStream().use { out ->
                                    compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
                                }
                                return file
                            }

                            val itemFile = itemPhoto?.toCacheFile("turnover")
                            val payoutFile = payoutPhoto?.toCacheFile("payout")

                            try {
                                if (itemFile != null || payoutFile != null || price != null) {
                                    MarketplaceApi.verifyTurnoverWithPhotos(
                                        token,
                                        listing.itemId,
                                        itemFile,
                                        payoutFile,
                                        acquisitionPrice = price,
                                    )
                                } else {
                                    MarketplaceApi.verifyTurnover(token, listing.itemId, null, null, null)
                                }
                            } finally {
                                itemFile?.delete()
                                payoutFile?.delete()
                            }
                        }

                        if (result is MarketplaceApi.Result.Ok) {
                            withContext(Dispatchers.IO) {
                                MarketplaceApi.recordSellerPayout(token, listing.itemId, null)
                            }
                        }

                        acquiring = false
                        onFinished((result as? MarketplaceApi.Result.Failure)?.message)
                    }
                },
                enabled = !acquiring,
                shape = MaterialTheme.shapes.small,
            ) {
                if (acquiring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Confirm & proof")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !acquiring) { Text("Back") }
        },
    )
}

/** One proof photograph inside the dialog: a thumbnail and its capture button. */
@Composable
private fun AcquireProofRow(
    label: String,
    bitmap: android.graphics.Bitmap?,
    onCapture: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(MaterialTheme.shapes.small),
            )
        }

        SecondaryButton(
            text = if (bitmap == null) "Photo: $label" else "Retake: $label",
            onClick = onCapture,
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.PhotoCamera,
        )
    }
}

/**
 * The offer's action buttons, pinned in the conversation header.
 *
 * Nothing but the buttons: the card in the thread tells the story, but a
 * long chat buries it, and the decisions must stay reachable. Admin gets
 * Accept/Reject while negotiating and Set schedule / Mark acquired once
 * accepted; the seller gets their turnover QR. Any other stage renders
 * nothing at all.
 */
@Composable
internal fun ItemOfferPanel(
    itemId: Int,
    expectedSellerId: Int,
    token: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val accents = LocalMarketAccents.current
    val scope = rememberCoroutineScope()

    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE) }
    val isAdmin = remember { (prefs.getString("user_role", "") ?: "").equals("admin", true) }

    var listing by remember(itemId) { mutableStateOf<Item?>(null) }
    var refreshKey by remember(itemId) { mutableStateOf(0) }
    var offerAction by remember { mutableStateOf<String?>(null) }
    var confirmAcquire by remember { mutableStateOf(false) }

    LaunchedEffect(itemId, refreshKey) {
        if (itemId <= 0) return@LaunchedEffect

        val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchItem(token, itemId) }
        if (result is MarketplaceApi.Result.Ok) listing = result.value
    }

    // A decision made elsewhere - the card, the website, the scan screen -
    // should retire these buttons without reopening the thread.
    LaunchedEffect(itemId) {
        while (true) {
            kotlinx.coroutines.delay(10_000)
            refreshKey++
        }
    }

    val item = listing ?: return

    // Only the seller's own thread carries offer decisions, and only while
    // the listing is still on its way into the store. A seller who is still
    // waiting on the decision has no button to pin, so nothing renders.
    if (item.sellerId != expectedSellerId || !item.isPending) return
    if (!isAdmin && !item.offerAccepted) return

    offerAction?.let { action ->
        ItemOfferActionDialog(
            listing = item,
            action = action,
            token = token,
            onDismiss = { offerAction = null },
            onDone = { offerAction = null; refreshKey++ },
        )
    }

    if (confirmAcquire) {
        AcquireOfferDialog(
            listing = item,
            token = token,
            onDismiss = { confirmAcquire = false },
            onFinished = { error ->
                confirmAcquire = false
                refreshKey++

                if (error != null) {
                    android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                }
            },
        )
    }

    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            when {
                isAdmin && !item.offerAccepted -> {
                    PrimaryButton(
                        text = "Accept",
                        onClick = { offerAction = "accept" },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Check,
                        containerColor = accents.success,
                    )
                    SecondaryButton(
                        text = "Reject",
                        onClick = { offerAction = "reject" },
                        modifier = Modifier.weight(1f),
                        contentColor = MaterialTheme.colorScheme.error,
                    )
                }

                isAdmin -> {
                    SecondaryButton(
                        text = if (item.meetupSchedule == null) "Set schedule" else "Change schedule",
                        onClick = { pickMeetupSchedule(context, scope, token, item.itemId) },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Event,
                    )
                    PrimaryButton(
                        text = "Mark acquired",
                        onClick = { confirmAcquire = true },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Inventory,
                        containerColor = accents.success,
                    )
                }

                else -> ItemQrButton(item = item, modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Accepting sets the acquisition price - what the store will pay the seller -
 * which is the same first step the inventory screen takes. Rejecting wants a
 * reason the seller will read.
 */
@Composable
private fun ItemOfferActionDialog(
    listing: Item,
    action: String,
    token: String,
    onDismiss: () -> Unit,
    onDone: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val accepting = action == "accept"

    var input by remember {
        mutableStateOf(if (accepting) Money.formatPlain(listing.sellerAskingPrice).replace(",", "") else "")
    }
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!working) onDismiss() },
        title = {
            Text(
                if (accepting) "Accept this offer" else "Reject this offer",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    if (accepting) {
                        "Set the acquisition price - what the store pays the seller " +
                            "for \"${listing.title}\". The asking price is " +
                            Money.format(listing.sellerAskingPrice) + "."
                    } else {
                        "The seller is told in this chat why their offer was turned down."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )

                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it; error = null },
                    label = { Text(if (accepting) "Acquisition price" else "Reason") },
                    prefix = if (accepting) ({ Text(Money.PESO) }) else null,
                    placeholder = { Text(if (accepting) "e.g. 300.00" else "Shown to the seller") },
                    singleLine = accepting,
                    minLines = if (accepting) 1 else 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                )

                error?.let {
                    InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (accepting && Money.normalizeInput(input) == null) {
                        error = "Enter a valid peso amount, e.g. 300 or 299.50."
                        return@Button
                    }
                    if (!accepting && input.isBlank()) {
                        error = "Please give the seller a reason."
                        return@Button
                    }

                    scope.launch {
                        working = true
                        error = null

                        val result = withContext(Dispatchers.IO) {
                            if (accepting) {
                                MarketplaceApi.setAcquisitionPrice(
                                    token,
                                    listing.itemId,
                                    Money.normalizeInput(input)!!,
                                )
                            } else {
                                MarketplaceApi.rejectItem(token, listing.itemId, input.trim())
                            }
                        }
                        working = false

                        when (result) {
                            is MarketplaceApi.Result.Ok -> onDone()
                            is MarketplaceApi.Result.Failure -> error = result.message
                        }
                    }
                },
                enabled = !working,
                shape = MaterialTheme.shapes.small,
            ) {
                if (working) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(if (accepting) "Accept offer" else "Reject offer")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !working) { Text("Back") } },
    )
}

/** What happened, in one line, so the card announces itself before the detail. */
@Composable
private fun OrderCardHeading(kind: String, order: MarketTransaction) {
    val (icon, label) = when (kind) {
        "order_placed" -> Icons.Filled.ShoppingCart to "Order placed"
        "payment_submitted" -> Icons.Filled.Receipt to "Payment sent"
        else -> Icons.Filled.Notifications to "Order update"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(label, style = MaterialTheme.typography.labelLarge)
        }

        Text(
            order.receiptNo.ifBlank { "#${order.transactionId}" },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The decisions the server says Admin may still take on this order. */
@Composable
private fun OrderCardActions(order: MarketTransaction, onAction: (String) -> Unit) {
    val accents = LocalMarketAccents.current

    if (order.isTerminal) return

    val decline = when {
        order.canDo("reject_payment") -> "reject_payment"
        order.canDo("cancel") -> "cancel"
        else -> null
    }

    if (!order.canDo("verify_payment") && !order.canDo("complete") &&
        !order.canDo("mark_ready_for_pickup") && decline == null
    ) {
        return
    }

    SoftDivider()

    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        if (order.canDo("verify_payment")) {
            PrimaryButton(
                text = "Approve",
                onClick = { onAction("verify_payment") },
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Check,
                containerColor = accents.success,
            )
        }

        decline?.let { action ->
            SecondaryButton(
                text = "Decline",
                onClick = { onAction(action) },
                modifier = Modifier.weight(1f),
                contentColor = MaterialTheme.colorScheme.error,
            )
        }
    }

    // Once the money is settled these two are the whole handover, so they
    // always sit together: stage it, then hand it over.
    if (order.canDo("mark_ready_for_pickup") || order.canDo("complete")) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            if (order.canDo("mark_ready_for_pickup")) {
                SecondaryButton(
                    text = "Ready for pickup",
                    onClick = { onAction("mark_ready_for_pickup") },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Inventory,
                )
            }

            if (order.canDo("complete")) {
                PrimaryButton(
                    text = "Complete",
                    onClick = { onAction("complete") },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.DoneAll,
                    containerColor = accents.success,
                )
            }
        }
    }
}

/** The item photo, or a placeholder when a listing has none. */
@Composable
private fun ItemThumbnail(photo: String?) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                Icons.Filled.Image,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/**
 * Whether the money has actually arrived.
 *
 * Kept separate from the order status pill: an order can be "reserved" while
 * the payment is still unverified, and it is the payment the buyer is asking
 * about.
 */
@Composable
internal fun PaymentStatusPill(
    paymentStatus: String,
    isFullPointsCheckout: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val (label, tone) = when {
        isFullPointsCheckout && paymentStatus == "verified" -> "Paid with points" to StatusTone.Success
        paymentStatus == "verified" -> "Paid" to StatusTone.Success
        paymentStatus == "proof_submitted" -> "Checking payment" to StatusTone.Warning
        paymentStatus == "rejected" -> "Payment declined" to StatusTone.Danger
        else -> "Not paid yet" to StatusTone.Warning
    }

    StatusPill(label = label, tone = tone, modifier = modifier)
}

/** Everyday wording for a payment method. */
internal fun paymentMethodLabel(method: String): String = when (method) {
    "gcash" -> "GCash"
    "points_full" -> "Points only"
    "cash" -> "Cash at store"
    else -> method.replaceFirstChar { it.uppercase() }
}

/** The receipt, full-screen, because a thumbnail is not enough to check one. */
@Composable
private fun PaymentProofViewer(imageUrl: String, reference: String?, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.92f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeAreaTop()
                    .safeAreaBottom()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Payment receipt", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Close", tint = Color.White)
                    }
                }

                reference?.let {
                    Text(
                        "Reference: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                    )
                }

                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Payment receipt",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}

/**
 * The listing behind the order, opened by tapping its photo.
 *
 * The card already carries a title and a thumbnail, so those are shown at once
 * and the full record fills in when it arrives - the dialog is never blank.
 */
@Composable
private fun ChatItemPreviewDialog(
    itemId: Int,
    token: String,
    fallbackTitle: String,
    fallbackPhoto: String?,
    onDismiss: () -> Unit,
) {
    var item by remember(itemId) { mutableStateOf<Item?>(null) }
    var loading by remember(itemId) { mutableStateOf(true) }
    var error by remember(itemId) { mutableStateOf<String?>(null) }

    LaunchedEffect(itemId) {
        val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchItem(token, itemId) }

        when (result) {
            is MarketplaceApi.Result.Ok -> item = result.value
            is MarketplaceApi.Result.Failure -> error = result.message
        }
        loading = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .padding(Spacing.lg)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                val photo = item?.photos?.firstOrNull() ?: fallbackPhoto

                if (photo != null) {
                    AsyncImage(
                        model = photo,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(MaterialTheme.shapes.small),
                    )
                }

                Text(
                    item?.title?.takeIf { it.isNotBlank() } ?: fallbackTitle,
                    style = MaterialTheme.typography.titleLarge,
                )

                item?.let { loaded ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PriceTag(Money.format(loaded.publicPrice ?: loaded.sellerAskingPrice))
                        ItemStatusPill(loaded.status, offerAccepted = loaded.offerAccepted)
                    }

                    if (loaded.rewardPoints > 0) {
                        RewardChip(points = loaded.rewardPoints)
                    }

                    if (loaded.description.isNotBlank()) {
                        Text(loaded.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (loading) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth().height(18.dp))
                }

                error?.let {
                    InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                }

                SecondaryButton("Close", onDismiss, Modifier.fillMaxWidth())
            }
        }
    }
}
