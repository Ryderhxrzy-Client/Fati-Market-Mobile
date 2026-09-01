package com.fati_market

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Admin transaction management.
 *
 * One row per buyer order, carrying the whole picture: who is buying, what,
 * the cash price, the points spent, the peso discount, what is still owed, how
 * it is being paid, the GCash proof, and both the payment and pickup states.
 *
 * Which actions are offered comes from the server's `available_actions`, so
 * the lifecycle rules live in one place rather than being re-derived here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTransactionsContent(
    onMenuClick: () -> Unit = {},
    onOpenChat: (Int) -> Unit = {},
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token = remember { prefs.getString("auth_token", "") ?: "" }
    val scope = rememberCoroutineScope()

    var transactions by remember { mutableStateOf<List<MarketTransaction>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    var actionTarget by remember { mutableStateOf<Pair<MarketTransaction, String>?>(null) }

    // Completing opens the counter screen; the order it settled is stale in
    // this list by the time the admin comes back, so returning refreshes.
    LaunchedEffect(AdminCounter.openCode) {
        if (AdminCounter.openCode == null) refreshKey++
    }
    var proofPreview by remember { mutableStateOf<String?>(null) }

    val filters = listOf(
        null to "All",
        "payment_proof_submitted" to "Needs review",
        "pending_payment" to "Awaiting payment",
        "ready_for_pickup" to "For pickup",
        "completed" to "Completed",
    )

    LaunchedEffect(selectedFilter, refreshKey) {
        loading = true
        when (val result = withContext(Dispatchers.IO) {
            MarketplaceApi.fetchAdminTransactions(token, selectedFilter)
        }) {
            is MarketplaceApi.Result.Ok -> {
                transactions = result.value
                error = null
            }
            is MarketplaceApi.Result.Failure -> error = result.message
        }
        loading = false
    }

    // ── Confirmation, with a reason where the rules require one ──────────
    actionTarget?.let { (transaction, action) ->
        // The same confirmation the conversation and the scan screen use,
        // so completing an order asks for the handover photo wherever it is
        // done - there is one decision here, not three.
        ChatOrderActionDialog(
            transaction = transaction,
            action = action,
            token = token,
            onDismiss = { actionTarget = null },
            onDone = {
                actionTarget = null
                refreshKey++
            },
        )
    }

    proofPreview?.let { url ->
        Dialog(onDismissRequest = { proofPreview = null }) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    Text("GCash proof", style = MaterialTheme.typography.titleLarge)
                    AsyncImage(
                        model = url,
                        contentDescription = "Payment proof",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                    )
                    SecondaryButton("Close", { proofPreview = null }, Modifier.fillMaxWidth())
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AdminPageHeader(title = "Transactions", onMenuClick = onMenuClick)

        // ── Filters ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            filters.forEach { (value, label) ->
                FilterChip(
                    selected = selectedFilter == value,
                    onClick = { selectedFilter = value },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    shape = CircleShape,
                )
            }
        }

        when {
            loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.screen),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    repeat(4) {
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            shape = MaterialTheme.shapes.medium,
                        )
                    }
                }
            }

            error != null -> {
                EmptyState(
                    icon = Icons.Filled.CloudOff,
                    title = "Could not load transactions",
                    message = error!!,
                    actionLabel = "Try again",
                    onAction = { refreshKey++ },
                )
            }

            transactions.isEmpty() -> {
                EmptyState(
                    icon = Icons.Filled.ReceiptLong,
                    title = "No transactions here",
                    message = if (selectedFilter == null) {
                        "Buyer orders will appear here as soon as someone checks out."
                    } else {
                        "Nothing matches this filter right now."
                    },
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Spacing.screen),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    items(transactions, key = { it.transactionId }) { transaction ->
                        AdminTransactionCard(
                            transaction = transaction,
                            onAction = { action -> actionTarget = transaction to action },
                            onViewProof = { proofPreview = it },
                            onOpenChat = { onOpenChat(transaction.itemId) },
                        )
                    }

                    item { Spacer(Modifier.height(Spacing.xxxl)) }
                }
            }
        }
    }
}

/** One order, with its full financial breakdown and the actions available. */
@Composable
private fun AdminTransactionCard(
    transaction: MarketTransaction,
    onAction: (String) -> Unit,
    onViewProof: (String) -> Unit,
    onOpenChat: () -> Unit,
) {
    val accents = LocalMarketAccents.current
    var expanded by remember { mutableStateOf(false) }

    MarketCard {
        // ── Header: item, buyer, status ─────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            if (transaction.itemPhoto != null) {
                AsyncImage(
                    model = transaction.itemPhoto,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.small),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Inventory2,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                Text(
                    transaction.itemTitle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Text(
                    "#${transaction.transactionId} · ${transaction.buyerEmail.substringBefore("@")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TransactionStatusPill(transaction.status, paymentMethod = transaction.paymentMethod)
            }

            Column(horizontalAlignment = Alignment.End) {
                PriceTag(Money.format(transaction.amountDue), size = PriceSize.Small)
                Text(
                    if (transaction.isFullPointsCheckout) "fully by points" else "due",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── The thing an admin most often needs to act on ───────────────
        if (transaction.awaitingProofReview) {
            InfoBanner(
                title = "GCash proof awaiting review",
                text = "Check the receipt matches ${Money.format(transaction.amountDue)} " +
                    "before verifying.",
                tone = StatusTone.Warning,
                icon = Icons.Filled.HourglassTop,
            )
        }

        SoftDivider()

        // ── Breakdown ───────────────────────────────────────────────────
        MarketPanel {
            SummaryRow("Item cash price", Money.format(transaction.subtotal))
            SummaryRow(
                label = "Points used",
                value = "${transaction.pointsUsed}",
                supporting = if (transaction.pointsUsed > 0) {
                    "1 point = ${Money.PESO}${LoyaltyRules.PESOS_PER_REDEEMED_POINT}"
                } else null,
            )
            SummaryRow(
                label = "Peso discount",
                value = "-" + Money.format(transaction.pointsDiscountAmount),
                valueColor = accents.reward,
            )
            SummaryRow(
                label = if (transaction.paymentMethod == "gcash") "GCash amount" else "Cash amount",
                value = Money.format(transaction.amountDue),
                emphasized = true,
            )
        }

        if (expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                SummaryRow(
                    "Payment method",
                    when (transaction.paymentMethod) {
                        "gcash" -> "GCash"
                        "points_full" -> "Points only"
                        else -> "Cash at store"
                    },
                )
                SummaryRow("Payment status", transaction.paymentStatus.replace('_', ' '))
                SummaryRow("Pickup status", transaction.pickupStatus.replace('_', ' '))
                SummaryRow(
                    label = "Reward points to credit",
                    value = "${transaction.rewardPointsToCredit}",
                    supporting = if (transaction.rewardPointsCredited) {
                        "Already credited on completion"
                    } else {
                        "Credited when you complete this order"
                    },
                    valueColor = accents.reward,
                )
                transaction.cancelReason?.let {
                    InfoBanner(text = it, tone = StatusTone.Neutral, icon = Icons.Filled.Info)
                }

                transaction.paymentReference?.let { reference ->
                    SummaryRow("GCash reference", reference)
                }

                transaction.paymentProof?.let { proof ->
                    SecondaryButton(
                        text = "View GCash proof",
                        onClick = { onViewProof(proof) },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.Receipt,
                    )
                }
            }
        }

        Text(
            if (expanded) "Show less" else "Show details",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable { expanded = !expanded }
                .padding(vertical = Spacing.xs),
        )

        // ── Actions, driven by the server's lifecycle ───────────────────
        if (transaction.availableActions.isNotEmpty()) {
            SoftDivider()

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                if (transaction.canDo("verify_payment")) {
                    PrimaryButton(
                        text = "Verify payment",
                        onClick = { onAction("verify_payment") },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.CheckCircle,
                    )
                }

                // A pay-at-the-store order is approved, not verified: the cash
                // arrives at the counter, and completing the order is what
                // records it as paid.
                if (transaction.canDo("approve_order")) {
                    PrimaryButton(
                        text = "Approve order",
                        onClick = { onAction("approve_order") },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.CheckCircle,
                    )
                }

                if (transaction.canDo("mark_ready_for_pickup")) {
                    PrimaryButton(
                        text = "Mark ready for pickup",
                        onClick = { onAction("mark_ready_for_pickup") },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.Inventory,
                        containerColor = accents.info,
                    )
                }

                if (transaction.canDo("complete")) {
                    PrimaryButton(
                        text = "Complete transaction",
                        onClick = { onAction("complete") },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.DoneAll,
                        containerColor = accents.success,
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    if (transaction.canDo("reject_payment")) {
                        SecondaryButton(
                            text = "Reject proof",
                            onClick = { onAction("reject_payment") },
                            modifier = Modifier.weight(1f),
                            contentColor = MaterialTheme.colorScheme.error,
                        )
                    }
                    if (transaction.canDo("cancel")) {
                        SecondaryButton(
                            text = "Cancel",
                            onClick = { onAction("cancel") },
                            modifier = Modifier.weight(1f),
                            contentColor = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                SecondaryButton(
                    text = "Message buyer",
                    onClick = onOpenChat,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Filled.Chat,
                )
            }
        }
    }
}


