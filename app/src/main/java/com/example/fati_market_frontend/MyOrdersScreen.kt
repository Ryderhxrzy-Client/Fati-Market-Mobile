package com.fati_market

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.PriceStyleLarge
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The buyer's order history.
 *
 * Doubles as their receipt drawer: every order opens a receipt they can read
 * on screen or download as a PDF to show at the store.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MyOrdersScreen(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token = remember { prefs.getString("auth_token", "") ?: "" }
    val currentUserId = remember { prefs.getInt("user_id", 0) }

    var orders by remember { mutableStateOf<List<MarketTransaction>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var refreshing by remember { mutableStateOf(false) }
    var openReceiptFor by remember { mutableStateOf<MarketTransaction?>(null) }
    var payingFor by remember { mutableStateOf<MarketTransaction?>(null) }
    var selectedTab by remember { mutableStateOf(OrderFilter.All) }

    BackHandler(onBack = onDismiss)

    LaunchedEffect(refreshKey) {
        // A pull already shows its own spinner, so do not also blank the list
        // out to skeletons underneath it.
        if (!refreshing) loading = true
        when (val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchMyTransactions(token) }) {
            is MarketplaceApi.Result.Ok -> {
                // Orders where this user is the buyer; the same endpoint also
                // returns rows where they were the seller. If the payload
                // carries no buyer id, show everything rather than silently
                // filtering the list down to nothing.
                orders = result.value.filter {
                    it.buyerId == 0 || currentUserId == 0 || it.buyerId == currentUserId
                }
                error = null
            }
            is MarketplaceApi.Result.Failure -> error = result.message
        }
        loading = false
        refreshing = false
    }

    openReceiptFor?.let { order ->
        ReceiptDialog(
            transactionId = order.transactionId,
            token = token,
            onDismiss = { openReceiptFor = null },
        )
    }

    payingFor?.let { order ->
        PaymentProofDialog(
            transaction = order,
            token = token,
            onDismiss = { payingFor = null },
            onSubmitted = {
                payingFor = null
                refreshKey++
            },
        )
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0),
                topBar = { MarketPageTopBar("My Orders", onDismiss) },
            ) { padding ->
                val visible = remember(orders, selectedTab) {
                    orders.filter { selectedTab.matches(it) }
                }

                Column(modifier = Modifier.padding(padding)) {

                // Tracking tabs: where each order stands, at a glance.
                ScrollableTabRow(
                    selectedTabIndex = OrderFilter.entries.indexOf(selectedTab),
                    edgePadding = Spacing.md,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {},
                ) {
                    OrderFilter.entries.forEach { filter ->
                        val count = orders.count { filter.matches(it) }

                        Tab(
                            selected = selectedTab == filter,
                            onClick = { selectedTab = filter },
                            text = {
                                Text(
                                    if (count > 0) "${filter.label} ($count)" else filter.label,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            },
                        )
                    }
                }

                when {
                    loading -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.screen),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        repeat(4) {
                            ShimmerBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                shape = MaterialTheme.shapes.medium,
                            )
                        }
                    }

                    error != null -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyState(
                            icon = Icons.Filled.CloudOff,
                            title = "Could not load your orders",
                            message = error!!,
                            actionLabel = "Try again",
                            onAction = { refreshKey++ },
                        )
                    }

                    visible.isEmpty() -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyState(
                            icon = Icons.Filled.ReceiptLong,
                            title = if (selectedTab == OrderFilter.All) {
                                "No orders yet"
                            } else {
                                "Nothing ${selectedTab.label.lowercase()}"
                            },
                            message = if (selectedTab == OrderFilter.All) {
                                "Items you buy will appear here, along with a receipt " +
                                    "you can download."
                            } else {
                                "No orders are at this stage right now."
                            },
                        )
                    }

                    else -> PullToRefreshBox(
                        isRefreshing = refreshing,
                        onRefresh = { refreshing = true; refreshKey++ },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.screen),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        ) {
                            items(visible, key = { it.transactionId }) { order ->
                                OrderHistoryCard(
                                    order = order,
                                    onViewReceipt = { openReceiptFor = order },
                                    onPay = { payingFor = order },
                                )
                            }

                            item { Spacer(Modifier.safeAreaBottom().height(Spacing.xl)) }
                        }
                    }
                }
                }
            }
        }
    }
}

/**
 * The tracking stages a buyer thinks in.
 *
 * Deliberately coarser than the eight-state lifecycle: a buyer cares whether
 * they still owe money, whether someone is checking, and whether it is done.
 */
private enum class OrderFilter(val label: String) {
    All("All"),
    ToPay("To pay"),
    Pending("Pending"),
    Paid("Paid"),
    Completed("Completed"),
    Cancelled("Cancelled");

    fun matches(order: MarketTransaction): Boolean = when (this) {
        All -> true
        // Still owes money, or the proof was declined and must be redone.
        ToPay -> order.paymentStatus == "unpaid" && !order.isTerminal
        // Submitted, waiting on the admin.
        Pending -> order.paymentStatus == "proof_submitted"
        // Payment settled but not yet handed over.
        Paid -> order.paymentStatus == "verified" && order.status != "completed"
        Completed -> order.status == "completed"
        Cancelled -> order.status == "cancelled" || order.status == "rejected"
    }
}

/** One row in the history. */
@Composable
private fun OrderHistoryCard(
    order: MarketTransaction,
    onViewReceipt: () -> Unit,
    onPay: () -> Unit,
) {
    val accents = LocalMarketAccents.current

    val hasReceipt = order.paymentStatus == "verified"

    MarketCard(onClick = if (hasReceipt) onViewReceipt else null) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            if (order.itemPhoto != null) {
                AsyncImage(
                    model = order.itemPhoto,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(MaterialTheme.shapes.small),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                Text(order.itemTitle, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    order.receiptNo.ifBlank { "Order #${order.transactionId}" } +
                        (Dates.short(order.createdAt)?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TransactionStatusPill(order.status)
            }

            Column(horizontalAlignment = Alignment.End) {
                PriceTag(Money.format(order.amountDue), size = PriceSize.Small)
                if (order.pointsUsed > 0) {
                    Text(
                        "-${order.pointsUsed} pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = accents.reward,
                    )
                }
            }
        }

        // The reward only lands once Admin completes the order, so say which.
        if (order.rewardPointsToCredit > 0) {
            Text(
                if (order.rewardPointsCredited) {
                    "You earned ${order.rewardPointsToCredit} point(s) from this order."
                } else {
                    "You will earn ${order.rewardPointsToCredit} point(s) once this is completed."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SoftDivider()

        // How it is being paid, and where that payment stands. A buyer
        // scanning this list wants both without opening anything.
        MarketPanel {
            SummaryRow(
                label = "Payment method",
                value = when (order.paymentMethod) {
                    "gcash" -> "GCash"
                    "points_full" -> "Paid with points"
                    else -> "Cash at store"
                },
            )
            SummaryRow(
                label = "Payment status",
                value = when (order.paymentStatus) {
                    "unpaid" -> "Not yet paid"
                    "proof_submitted" -> "Waiting for admin"
                    "verified" -> "Paid"
                    "rejected" -> "Declined"
                    else -> order.paymentStatus.replace('_', ' ')
                },
                valueColor = when (order.paymentStatus) {
                    "verified" -> accents.success
                    "rejected" -> MaterialTheme.colorScheme.error
                    "unpaid" -> accents.warning
                    else -> null
                },
            )
        }

        // The backend reserves the item the moment a checkout is created, but
        // nothing said so - the buyer had no way to know it was being held.
        if (!order.isTerminal && order.reservedUntil != null) {
            val remaining = Dates.timeUntil(order.reservedUntil)

            if (remaining != null) {
                InfoBanner(
                    title = "Reserved for you",
                    text = "No one else can buy this item. Held until " +
                        (Dates.short(order.reservedUntil) ?: "-") +
                        " · $remaining left",
                    tone = if (order.paymentStatus == "unpaid") StatusTone.Warning else StatusTone.Success,
                    icon = Icons.Filled.Lock,
                )
            } else {
                InfoBanner(
                    title = "Reservation expired",
                    text = "This hold has lapsed, so the item may go back on sale.",
                    tone = StatusTone.Danger,
                    icon = Icons.Filled.LockOpen,
                )
            }
        }

        val stillOwes = order.paymentStatus == "unpaid" && !order.isTerminal

        if (stillOwes) {
            PrimaryButton(
                text = if (order.paymentMethod == "gcash") {
                    "Pay " + Money.format(order.amountDue)
                } else {
                    "Payment details"
                },
                onClick = onPay,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Filled.Payments,
            )
        }

        if (order.paymentStatus == "rejected") {
            InfoBanner(
                title = "Payment declined",
                text = order.cancelReason ?: "The admin declined this payment.",
                tone = StatusTone.Danger,
                icon = Icons.Filled.ErrorOutline,
            )
        }

        // A receipt for money that has not changed hands would be a false
        // record, so it only appears once the admin has verified the payment.
        if (order.paymentStatus == "verified") {
            SecondaryButton(
                text = "View receipt",
                onClick = onViewReceipt,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Filled.ReceiptLong,
            )
        }

        // The walk-in code. Shown for any open order: Ofelia scans it at the
        // counter to pull this order up, take the handover photo, and
        // complete it - and for a cash order, to take the payment first.
        PickupQrButton(order = order, modifier = Modifier.fillMaxWidth())
    }
}

/**
 * The receipt itself, with a download.
 *
 * Fetched fresh rather than built from the list row, so what is shown - and
 * what gets exported - is the server's record, not a local approximation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReceiptDialog(
    transactionId: Int,
    token: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accents = LocalMarketAccents.current

    var receipt by remember { mutableStateOf<Receipt?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        loading = true
        when (val result = withContext(Dispatchers.IO) {
            MarketplaceApi.fetchReceipt(token, transactionId)
        }) {
            is MarketplaceApi.Result.Ok -> { receipt = result.value; error = null }
            is MarketplaceApi.Result.Failure -> error = result.message
        }
        loading = false
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                MarketPageTopBar(
                    title = "Receipt",
                    onBack = onDismiss,
                    navigationIcon = Icons.Filled.Close,
                    navigationContentDescription = "Close",
                )

                when {
                    loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }

                    error != null || receipt == null -> Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyState(
                            icon = Icons.Filled.ErrorOutline,
                            title = "Receipt unavailable",
                            message = error ?: "This order has no receipt yet.",
                        )
                    }

                    else -> {
                        val r = receipt!!

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(Spacing.screen),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        ) {
                            if (!r.isOfficial) {
                                InfoBanner(
                                    title = "Provisional receipt",
                                    text = "The admin has not verified this payment yet. " +
                                        "It becomes official once they do.",
                                    tone = StatusTone.Warning,
                                    icon = Icons.Filled.HourglassTop,
                                )
                            }

                            MarketCard(contentPadding = PaddingValues(Spacing.xl)) {
                                // -- Header --------------------------------
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                                ) {
                                    Text(r.storeName, style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        "Official Receipt",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(Modifier.height(Spacing.xs))
                                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                                        Text(
                                            r.receiptNo,
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier.padding(
                                                horizontal = Spacing.md,
                                                vertical = Spacing.xs,
                                            ),
                                        )
                                    }
                                }

                                Spacer(Modifier.height(Spacing.sm))
                                SoftDivider()

                                // -- Who / when ----------------------------
                                Overline("Issued to")
                                Text(
                                    r.buyerName.ifBlank { r.buyerEmail },
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                if (r.buyerName.isNotBlank()) {
                                    Text(
                                        r.buyerEmail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Dates.full(r.issuedAt)?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                SoftDivider()

                                // -- Item ----------------------------------
                                Overline("Item")
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                ) {
                                    r.itemPhoto?.let { photo ->
                                        AsyncImage(
                                            model = photo,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(MaterialTheme.shapes.small),
                                        )
                                    }
                                    Text(r.itemTitle, style = MaterialTheme.typography.titleMedium)
                                }

                                SoftDivider()

                                // -- Money ---------------------------------
                                SummaryRow("Item price", Money.format(r.subtotal))
                                SummaryRow("Points used", "${r.pointsUsed}")
                                SummaryRow(
                                    label = "Points discount",
                                    value = "-" + Money.format(r.pointsDiscountAmount),
                                    valueColor = accents.reward,
                                )

                                SoftDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("AMOUNT PAID", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        Money.format(r.amountPaid),
                                        style = PriceStyleLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }

                                SoftDivider()

                                SummaryRow("Payment method", r.paymentMethodLabel)
                                r.paymentReference?.let { SummaryRow("GCash reference", it) }
                                if (r.rewardPointsEarned > 0) {
                                    SummaryRow(
                                        label = "Reward points earned",
                                        value = "+${r.rewardPointsEarned}",
                                        valueColor = accents.reward,
                                    )
                                }

                                Spacer(Modifier.height(Spacing.xs))
                                Text(
                                    "Keep this receipt as your proof of transaction.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            // -- Download --------------------------------------
                            // Inside the page, so it scrolls with the receipt
                            // instead of hanging over the gesture bar.
                            PrimaryButton(
                            text = "Download receipt (PDF)",
                            icon = Icons.Filled.Download,
                            loading = saving,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                scope.launch {
                                    saving = true
                                    val result = withContext(Dispatchers.IO) {
                                        ReceiptPdf.save(context, r)
                                    }
                                    saving = false

                                    when (result) {
                                        is ReceiptPdf.Result.Saved -> {
                                            Toast.makeText(
                                                context,
                                                "Saved to Downloads: ${result.displayName}",
                                                Toast.LENGTH_LONG,
                                            ).show()

                                            context.startActivity(
                                                Intent.createChooser(
                                                    ReceiptPdf.shareIntent(result.uri),
                                                    "Share receipt",
                                                )
                                            )
                                        }

                                        is ReceiptPdf.Result.Failed -> Toast.makeText(
                                            context,
                                            result.message,
                                            Toast.LENGTH_LONG,
                                        ).show()
                                    }
                                }
                            },
                            )

                            SafeAreaBottomSpacer()
                        }
                    }
                }
            }
        }
    }
}
