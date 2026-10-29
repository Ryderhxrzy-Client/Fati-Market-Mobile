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
    var extendingSchedule by remember { mutableStateOf(false) }
    var expanded by remember(itemId, buyerId) { mutableStateOf(false) }

    if (extendingSchedule) {
        order?.let { cur ->
            PickupScheduleDialog(
                transactionId = cur.transactionId,
                token = token,
                current = cur.pickupDate,
                isAdmin = true,
                onDismiss = { extendingSchedule = false },
                onSaved = {
                    extendingSchedule = false
                    refreshKey++
                },
            )
        }
    }

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

                    MarketPanel(
                        contentPadding = PaddingValues(horizontal = Spacing.sm, vertical = Spacing.xs),
                    ) {
                        CompactOrderRow("Order", current.receiptNo.ifBlank { "#${current.transactionId}" })
                        CompactOrderRow("Item", current.itemTitle)
                        CompactOrderRow("Order total", Money.format(current.amountDue), emphasized = true)
                        CompactOrderRow("Payment method", orderPaymentMethodLabel(current))
                        (current.pickupDateFormatted ?: current.pickupDate)?.let { sched ->
                            CompactOrderRow(
                                label = "Pick-up schedule",
                                value = sched,
                                emphasized = true,
                                valueColor = if (current.status == "overdue") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        if (current.status == "overdue") {
                            InfoBanner(
                                title = "Reservation Overdue",
                                text = if (isAdmin) {
                                    "The scheduled pick-up date has passed. Please extend pick-up date, forfeit DP and relist, or mark as sold."
                                } else {
                                    "Your scheduled pick-up date has passed. Please visit the store or contact admin to reschedule."
                                },
                                tone = StatusTone.Danger,
                                icon = Icons.Filled.EventBusy,
                            )
                        }

                        if (current.paymentMethod == "cash" && current.downpaymentRequired) {
                            val downpaymentPaid = current.paymentStatus in setOf(
                                "partially_paid",
                                "paid",
                                "verified",
                            )
                            val balancePaid = current.paymentStatus in setOf("paid", "verified")

                            CompactOrderRow(
                                label = "PayMongo downpayment",
                                value = Money.format(current.downpaymentAmount) +
                                    if (downpaymentPaid) " · Paid" else " · Unpaid",
                                valueColor = if (downpaymentPaid) {
                                    accents.success
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                                emphasized = true,
                            )
                            CompactOrderRow(
                                label = "Cash balance at pickup",
                                value = Money.format(current.remainingBalance) +
                                    if (balancePaid) " · Paid" else " · Unpaid",
                                valueColor = if (balancePaid) {
                                    accents.success
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                                emphasized = true,
                            )
                        }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .clickable { expanded = !expanded }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.ReceiptLong,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        current.receiptNo.ifBlank { "Order #${current.transactionId}" } +
                            " · " + current.itemTitle,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        if (current.paymentMethod == "cash" && current.downpaymentRequired) {
                            val paid = current.paymentStatus in setOf("partially_paid", "paid", "verified")
                            Money.format(current.downpaymentAmount) +
                                (if (paid) " paid · " else " unpaid · ") +
                                Money.format(current.remainingBalance) + " cash due"
                        } else {
                            Money.format(current.amountDue) + " · " +
                                orderPaymentMethodLabel(current)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                PaymentStatusPill(
                    current.paymentStatus,
                    current.isFullPointsCheckout,
                    paymentMethod = current.paymentMethod,
                    orderStatus = current.status,
                )

                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    if (expanded) "Collapse order" else "Expand order",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }

            if (expanded && current.paymentMethod == "cash" && current.downpaymentRequired) {
                val downpaymentPaid = current.paymentStatus in setOf(
                    "partially_paid",
                    "paid",
                    "verified",
                )
                val balancePaid = current.paymentStatus in setOf("paid", "verified")

                MarketPanel(
                    contentPadding = PaddingValues(horizontal = Spacing.sm, vertical = Spacing.xs),
                ) {
                    CompactOrderRow(
                        label = "PayMongo downpayment",
                        value = Money.format(current.downpaymentAmount) +
                            if (downpaymentPaid) " · Paid" else " · Unpaid",
                        valueColor = if (downpaymentPaid) {
                            accents.success
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        emphasized = true,
                    )
                    CompactOrderRow(
                        label = "Cash balance at pickup",
                        value = Money.format(current.remainingBalance) +
                            if (balancePaid) " · Paid" else " · Unpaid",
                        valueColor = if (balancePaid) {
                            accents.success
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        emphasized = true,
                    )
                }
            }

            if ((expanded || current.status == "overdue") && isAdmin) {
                // Admin: the decisions the server still allows, plus the two
                // things worth a glance - who is buying, and their receipt.
                if (!current.isTerminal) {
                    // Two different approvals: a GCash payment that has
                    // landed, or a cash order that is only being accepted.
                    // The server offers exactly one of them.
                    val approve = when {
                        current.canDo("verify_payment") -> "verify_payment"
                        current.canDo("approve_order") -> "approve_order"
                        else -> null
                    }
                    val decline = when {
                        current.canDo("reject_payment") -> "reject_payment"
                        current.canDo("cancel") -> "cancel"
                        else -> null
                    }

                    if (approve != null || decline != null ||
                        current.canDo("mark_ready_for_pickup") || current.canDo("complete") ||
                        current.status == "overdue"
                    ) {
                        OrderActionButtons(
                            order = current,
                            approve = approve,
                            decline = decline,
                            compact = true,
                            onAction = { pendingAction = it },
                            onExtend = { extendingSchedule = true },
                        )
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
            } else if (expanded && !current.isTerminal) {
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

                // Where to take that code. The buyer was handed the code and
                // left to work out where the store is; the pin lives on their
                // profile screen, which is not where they are when the order
                // becomes theirs to collect.
                if (current.isCollectable()) {
                    PickupLocationRow()
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
            if (transaction.downpaymentRequired) {
                val downpaymentState = if (
                    transaction.paymentStatus in setOf("partially_paid", "paid", "verified")
                ) "Paid" else "Unpaid"
                val balanceState = if (
                    transaction.paymentStatus in setOf("paid", "verified")
                ) "Paid" else "Unpaid"

                "Payment method: ${orderPaymentMethodLabel(transaction)}. " +
                    "PayMongo downpayment: ${Money.format(transaction.downpaymentAmount)} " +
                    "($downpaymentState). Remaining cash to collect at pickup: " +
                    "${Money.format(transaction.remainingBalance)} ($balanceState). " +
                    "Approve the order to hold \"${transaction.itemTitle}\" for the buyer."
            } else {
                "Accept ${Money.format(transaction.amountDue)} cash at the store for " +
                    "\"${transaction.itemTitle}\". The item is held and the buyer gets their " +
                    "pickup code - they pay when they collect it, and completing the order is " +
                    "what marks it paid."
            },
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
        "forfeit", "cancel_and_relist" -> {
            val isUnpaid = !transaction.hasPaidDownpayment
            Triple(
                if (isUnpaid) "Cancel & Relist Item" else "Forfeit DP & Relist",
                if (isUnpaid) {
                    "This overdue reservation was unpaid. Confirming will cancel this order and immediately return \"${transaction.itemTitle}\" to the public marketplace."
                } else {
                    "This will forfeit the buyer's downpayment, cancel this overdue reservation, and return \"${transaction.itemTitle}\" to the public marketplace."
                },
                if (isUnpaid) "Return to Marketplace" else "Forfeit & Relist",
            )
        }
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
                                "forfeit", "cancel_and_relist" ->
                                    MarketplaceApi.forfeitAdminTransaction(token, transaction.transactionId)
                                else ->
                                    MarketplaceApi.cancelTransaction(token, transaction.transactionId, reason)
                            }
                        }
                        working = false

                        when (result) {
                            is MarketplaceApi.Result.Ok -> {
                                val msg = if (action in setOf("forfeit", "cancel_and_relist")) {
                                    if (transaction.hasPaidDownpayment) "Downpayment forfeited and item returned to marketplace."
                                    else "Order cancelled and item returned to marketplace."
                                } else "Action completed successfully."
                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                onDone()
                            }
                            is MarketplaceApi.Result.Failure -> {
                                error = result.message
                                android.widget.Toast.makeText(context, "Error: ${result.message}", android.widget.Toast.LENGTH_LONG).show()
                            }
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
