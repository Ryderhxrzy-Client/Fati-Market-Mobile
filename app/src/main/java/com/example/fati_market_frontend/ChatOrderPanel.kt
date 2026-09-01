package com.fati_market

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
 * The order banner that sits above an item conversation for Admin.
 *
 * Conversations are per item and per buyer, so when a thread has a live order
 * behind it the admin can settle it without leaving the chat: verify or
 * decline the payment, mark it ready, complete it, or look the buyer up.
 *
 * Collapses to nothing when the thread is just an enquiry.
 */
@Composable
internal fun ChatOrderPanel(
    itemId: Int,
    buyerId: Int,
    token: String,
    modifier: Modifier = Modifier,
    onChanged: () -> Unit = {},
) {
    val context = LocalContext.current
    val accents = LocalMarketAccents.current
    val scope = rememberCoroutineScope()

    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val isAdmin = remember { (prefs.getString("user_role", "") ?: "").equals("admin", true) }

    var order by remember(itemId, buyerId) { mutableStateOf<MarketTransaction?>(null) }
    var refreshKey by remember(itemId, buyerId) { mutableStateOf(0) }
    var pendingAction by remember { mutableStateOf<String?>(null) }
    var showBuyer by remember { mutableStateOf(false) }
    var showProof by remember { mutableStateOf(false) }
    var payingNow by remember { mutableStateOf(false) }

    LaunchedEffect(itemId, buyerId, refreshKey) {
        if (itemId <= 0 || buyerId <= 0) return@LaunchedEffect

        // The admin reads the order through the admin endpoint; the buyer
        // through their own list, since /admin is closed to them.
        val result = withContext(Dispatchers.IO) {
            if (isAdmin) {
                MarketplaceApi.fetchOrderForConversation(token, itemId, buyerId)
            } else {
                when (val mine = MarketplaceApi.fetchMyTransactions(token)) {
                    is MarketplaceApi.Result.Ok -> MarketplaceApi.Result.Ok(
                        mine.value.filter { it.itemId == itemId }
                            .let { rows -> rows.firstOrNull { !it.isTerminal } ?: rows.firstOrNull() },
                    )
                    is MarketplaceApi.Result.Failure -> mine
                }
            }
        }
        if (result is MarketplaceApi.Result.Ok) order = result.value
    }

    // A decision made elsewhere should reach this strip without reopening
    // the thread.
    LaunchedEffect(itemId, buyerId) {
        while (true) {
            kotlinx.coroutines.delay(10_000)
            refreshKey++
        }
    }

    val current = order ?: return

    // ── Buyer profile ───────────────────────────────────────────────────
    if (showBuyer) {
        Dialog(onDismissRequest = { showBuyer = false }) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                Column(
                    modifier = Modifier.padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    if (current.buyerProfilePicture != null) {
                        AsyncImage(
                            model = current.buyerProfilePicture,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(40.dp),
                            )
                        }
                    }

                    Text(
                        current.buyerName ?: current.buyerEmail.substringBefore("@"),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        current.buyerEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    PointsBalanceChip(points = current.buyerWalletPoints)

                    SoftDivider()

                    MarketPanel {
                        SummaryRow("Order", current.receiptNo.ifBlank { "#${current.transactionId}" })
                        SummaryRow("Item", current.itemTitle)
                        SummaryRow("Amount due", Money.format(current.amountDue), emphasized = true)
                    }

                    SecondaryButton("Close", { showBuyer = false }, Modifier.fillMaxWidth())
                }
            }
        }
    }

    // ── Proof viewer ────────────────────────────────────────────────────
    if (showProof && current.paymentProof != null) {
        Dialog(
            onDismissRequest = { showProof = false },
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
                        Text("GCash proof", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        IconButton(onClick = { showProof = false }) {
                            Icon(Icons.Filled.Close, "Close", tint = Color.White)
                        }
                    }

                    current.paymentReference?.let {
                        Text(
                            "Reference: $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                        )
                    }

                    AsyncImage(
                        model = current.paymentProof,
                        contentDescription = "Payment proof",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            }
        }
    }

    if (payingNow) {
        PaymentProofDialog(
            transaction = current,
            token = token,
            onDismiss = { payingNow = false },
            onSubmitted = { payingNow = false; refreshKey++ },
        )
    }

    // ── Confirmation ────────────────────────────────────────────────────
    pendingAction?.let { action ->
        ChatOrderActionDialog(
            transaction = current,
            action = action,
            token = token,
            onDismiss = { pendingAction = null },
            onDone = {
                pendingAction = null
                refreshKey++
                onChanged()
            },
        )
    }

    // ── The banner ──────────────────────────────────────────────────────
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (current.awaitingProofReview) {
            accents.warningContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            // One line: which order, what is owed, where it stands. The card
            // in the thread carries the detail; this is the reminder.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    current.receiptNo.ifBlank { "Order #${current.transactionId}" } + " · " +
                        Money.format(current.amountDue) + " · " + paymentMethodLabel(current.paymentMethod),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                PaymentStatusPill(
                    current.paymentStatus,
                    current.isFullPointsCheckout,
                    paymentMethod = current.paymentMethod,
                    orderStatus = current.status,
                )
            }

            if (isAdmin) {
                // Admin: the decisions the server still allows, plus the two
                // things worth a glance - who is buying, and their receipt.
                if (!current.isTerminal) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        // Two different approvals: a GCash payment that has
                        // landed, or a cash order that is only being accepted.
                        // The server offers exactly one of them.
                        if (current.canDo("verify_payment")) {
                            PrimaryButton(
                                text = "Approve",
                                onClick = { pendingAction = "verify_payment" },
                                modifier = Modifier.weight(1f),
                                containerColor = accents.success,
                                compact = true,
                            )
                        }

                        if (current.canDo("approve_order")) {
                            PrimaryButton(
                                text = "Approve",
                                onClick = { pendingAction = "approve_order" },
                                modifier = Modifier.weight(1f),
                                containerColor = accents.success,
                                compact = true,
                            )
                        }

                        if (current.canDo("mark_ready_for_pickup")) {
                            SecondaryButton(
                                text = "Ready",
                                onClick = { pendingAction = "mark_ready_for_pickup" },
                                modifier = Modifier.weight(1f),
                                compact = true,
                            )
                        }

                        if (current.canDo("complete")) {
                            PrimaryButton(
                                text = "Complete",
                                onClick = { pendingAction = "complete" },
                                modifier = Modifier.weight(1f),
                                containerColor = accents.success,
                                compact = true,
                            )
                        }

                        val declineAction = when {
                            current.canDo("reject_payment") -> "reject_payment"
                            current.canDo("cancel") -> "cancel"
                            else -> null
                        }

                        declineAction?.let { action ->
                            SecondaryButton(
                                text = "Decline",
                                onClick = { pendingAction = action },
                                modifier = Modifier.weight(1f),
                                contentColor = MaterialTheme.colorScheme.error,
                                compact = true,
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Text(
                        "Buyer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraSmall)
                            .clickable { showBuyer = true },
                    )

                    if (current.paymentProof != null) {
                        Text(
                            "Receipt",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraSmall)
                                .clickable { showProof = true },
                        )
                    }
                }
            } else if (!current.isTerminal) {
                // Buyer: settle the bill, then carry the pickup code - both
                // without leaving the conversation.
                val owes = current.paymentMethod == "gcash" &&
                    (current.paymentStatus == "unpaid" || current.paymentStatus == "rejected")

                when {
                    owes -> PrimaryButton(
                        text = if (current.paymentStatus == "rejected") {
                            "Send payment again"
                        } else {
                            "Pay " + Money.format(current.amountDue)
                        },
                        onClick = { payingNow = true },
                        modifier = Modifier.fillMaxWidth(),
                        compact = true,
                    )

                    // Everything else falls through to the code. Gating this on
                    // a verified payment hid it from exactly the buyers who need
                    // it most - the ones paying cash, who are approved with the
                    // bill still open. The button decides for itself whether the
                    // order is approved enough to collect on.
                    else -> PickupQrButton(
                        order = current,
                        modifier = Modifier.fillMaxWidth(),
                        compact = true,
                    )
                }
            }
        }
    }
}

/** Confirms an in-chat decision, asking for a reason where one is required. */
@Composable
internal fun ChatOrderActionDialog(
    transaction: MarketTransaction,
    action: String,
    token: String,
    onDismiss: () -> Unit,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var reason by remember { mutableStateOf("") }
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val needsReason = action == "reject_payment" || action == "cancel"

    // Completing is a handover, not a yes/no question: the buyer has to be
    // photographed receiving the item. That is what the counter screen does
    // after a scan, so Complete opens it with this order's own code instead
    // of asking here.
    if (action == "complete") {
        LaunchedEffect(transaction.transactionId) {
            val code = transaction.qrCode

            if (code != null) {
                AdminCounter.open(code)
            } else {
                android.widget.Toast.makeText(
                    context,
                    "Scan the buyer's pickup code to complete this order.",
                    android.widget.Toast.LENGTH_LONG,
                ).show()
            }

            onDismiss()
        }

        return
    }

    val (title, body, confirm) = when (action) {
        "verify_payment" -> Triple(
            "Approve payment",
            "Confirm the ${Money.format(transaction.amountDue)} GCash payment for " +
                "\"${transaction.itemTitle}\" has landed. The buyer is told in this chat.",
            "Approve",
        )
        // Approving a cash order settles nothing: the buyer has not handed
        // anything over yet, and saying "paid" here would put it on a receipt.
        "approve_order" -> Triple(
            "Approve this order",
            "Accept ${Money.format(transaction.amountDue)} cash at the store for " +
                "\"${transaction.itemTitle}\". The item is held and the buyer gets their " +
                "pickup code - they pay when they collect it, and completing the order is " +
                "what marks it paid.",
            "Approve",
        )
        "reject_payment" -> Triple(
            "Decline payment",
            "The order closes and the ${transaction.pointsUsed} point(s) the buyer spent " +
                "go back to their wallet. Your reason is posted in this chat.",
            "Decline",
        )
        "mark_ready_for_pickup" -> Triple(
            "Ready for pickup",
            "Tell the buyer the item is staged and waiting at the store.",
            "Mark ready",
        )
        else -> Triple(
            "Cancel order",
            "The item returns to the catalog and any points spent are refunded.",
            "Cancel order",
        )
    }

    AlertDialog(
        onDismissRequest = { if (!working) onDismiss() },
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Text(body, style = MaterialTheme.typography.bodyMedium)

                if (needsReason) {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it; error = null },
                        label = { Text("Reason") },
                        placeholder = { Text("Shown to the buyer in this chat") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                    )
                }

                error?.let {
                    InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (needsReason && reason.isBlank()) {
                        error = "Please give a reason."
                        return@Button
                    }

                    scope.launch {
                        working = true
                        error = null

                        val result = withContext(Dispatchers.IO) {
                            when (action) {
                                "verify_payment" ->
                                    MarketplaceApi.verifyPayment(token, transaction.transactionId)
                                "approve_order" ->
                                    MarketplaceApi.approveOrder(token, transaction.transactionId)
                                "reject_payment" ->
                                    MarketplaceApi.rejectPayment(token, transaction.transactionId, reason)
                                "mark_ready_for_pickup" ->
                                    MarketplaceApi.markReadyForPickup(token, transaction.transactionId)
                                else ->
                                    MarketplaceApi.cancelTransaction(token, transaction.transactionId, reason)
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
                    Text(confirm)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !working) { Text("Back") } },
    )
}
