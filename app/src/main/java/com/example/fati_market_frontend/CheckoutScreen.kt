package com.fati_market

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.fati_market.auth.utils.copyUriToCache
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Buyer checkout.
 *
 * Points act as a discount here, never as the currency. The buyer chooses how
 * many of their points to spend, sees the whole breakdown update as they do,
 * and settles whatever is left in cash or by GCash.
 *
 * The preview shown while sliding is computed locally from the shared loyalty
 * rules purely for responsiveness - the amount actually charged is recalculated
 * by the server, and the confirmation screen renders the server's numbers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CheckoutScreen(
    item: Item,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token = remember { prefs.getString("auth_token", "") ?: "" }
    val scope = rememberCoroutineScope()
    val accents = LocalMarketAccents.current

    var quote by remember { mutableStateOf<CheckoutQuote?>(null) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    var pointsToUse by remember { mutableStateOf(0) }
    var paymentMethod by remember { mutableStateOf("cash") }
    var submitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var placedOrder by remember { mutableStateOf<MarketTransaction?>(null) }

    BackHandler(onBack = onBack)

    // One authoritative call for the balance and the ceiling on spendable
    // points; the slider then previews locally against those.
    LaunchedEffect(item.itemId) {
        loading = true
        when (val result = withContext(Dispatchers.IO) {
            MarketplaceApi.fetchQuote(token, item.itemId, 0)
        }) {
            is MarketplaceApi.Result.Ok -> {
                quote = result.value
                loadError = null
            }
            is MarketplaceApi.Result.Failure -> loadError = result.message
        }
        loading = false
    }

    val itemPrice = quote?.itemPrice ?: item.publicPrice
    val available = quote?.availablePoints ?: 0
    val maxUsable = quote?.maxUsablePoints ?: 0
    val discount = LoyaltyRules.discountFor(pointsToUse)
    val amountDue = LoyaltyRules.amountDue(itemPrice, pointsToUse)
    val isFullPoints = amountDue.signum() == 0
    val rewardOnCompletion = quote?.rewardPointsOnCompletion ?: item.rewardPoints

    val isPastCutoff = quote?.isPastCutoff == true || runCatching {
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) >= 17
    }.getOrDefault(false)

    val isCash = paymentMethod == "cash" && !isFullPoints
    val downpayment = if (isCash) {
        amountDue.multiply(java.math.BigDecimal("0.50")).setScale(2, java.math.RoundingMode.HALF_UP)
    } else {
        java.math.BigDecimal.ZERO
    }
    val remainingBalance = if (isCash) {
        amountDue.subtract(downpayment)
    } else {
        java.math.BigDecimal.ZERO
    }

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        // Insets are handled explicitly below, so the Scaffold must not also
        // apply them or the bottom bar gets padded twice.
        contentWindowInsets = WindowInsets(0),
        topBar = { MarketPageTopBar("Checkout", onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            when {
                loading -> {
                    repeat(3) {
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            shape = MaterialTheme.shapes.medium,
                        )
                        Spacer(Modifier.height(Spacing.sm))
                    }
                }

                loadError != null -> {
                    InfoBanner(
                        text = loadError!!,
                        tone = StatusTone.Danger,
                        icon = Icons.Filled.ErrorOutline,
                        title = "This item cannot be bought right now",
                    )
                    SecondaryButton("Go back", onBack, Modifier.fillMaxWidth())
                }

                placedOrder != null -> {
                    OrderPlacedSection(order = placedOrder!!, token = token)
                }

                else -> {
                    // ── What is being bought ────────────────────────────
                    MarketCard(contentPadding = PaddingValues(Spacing.md)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (item.photos.isNotEmpty()) {
                                AsyncImage(
                                    model = item.photos.first(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(MaterialTheme.shapes.small),
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                            ) {
                                Text(
                                    item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2,
                                )
                                PriceTag(Money.format(itemPrice), size = PriceSize.Small)
                            }
                        }
                    }

                    if (isPastCutoff) {
                        InfoBanner(
                            title = "Store Cutoff Notice (Past 5:00 PM)",
                            text = quote?.pickupScheduleNote
                                ?: "Orders placed at or after 5:00 PM are automatically scheduled for next-day pickup/meet-up at Ofelia's store during opening hours.",
                            tone = StatusTone.Warning,
                            icon = Icons.Filled.Schedule,
                        )
                    }

                    // ── Points redemption ───────────────────────────────
                    MarketCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("Use your points", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "1 point = ${Money.PESO}${LoyaltyRules.PESOS_PER_REDEEMED_POINT} off",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            PointsBalanceChip(points = available)
                        }

                        if (available == 0) {
                            InfoBanner(
                                text = "You have no points yet. Complete this purchase and " +
                                    "you will earn $rewardOnCompletion point(s) to spend next time.",
                                tone = StatusTone.Neutral,
                                icon = Icons.Filled.Stars,
                            )
                        } else {
                            Spacer(Modifier.height(Spacing.xs))

                            // One decision, not a dial: spend the points here
                            // or keep them. Flipping it on applies every point
                            // this order can absorb.
                            val usingPoints = pointsToUse > 0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .background(
                                        if (usingPoints) accents.reward.copy(alpha = 0.08f)
                                        else MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (usingPoints) accents.reward else MaterialTheme.colorScheme.outlineVariant,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(Spacing.sm),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        if (usingPoints) {
                                            "Using $pointsToUse point(s)"
                                        } else {
                                            "Use my $maxUsable point(s)"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        if (usingPoints) {
                                            "Saves you " + Money.PESO +
                                                Money.formatPlain(discount.toPlainString()) +
                                                " on this order"
                                        } else {
                                            "Turn on to take " + Money.PESO +
                                                Money.formatPlain(
                                                    LoyaltyRules.discountFor(maxUsable).toPlainString(),
                                                ) + " off"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                Switch(
                                    checked = usingPoints,
                                    onCheckedChange = { on ->
                                        pointsToUse = if (on) maxUsable else 0
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = accents.reward,
                                        checkedThumbColor = Color.White,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                                        checkedBorderColor = accents.reward,
                                    ),
                                )
                            }
                        }
                    }

                    // ── The breakdown ───────────────────────────────────
                    MarketCard {
                        Overline("Order summary")
                        Spacer(Modifier.height(Spacing.xs))

                        SummaryRow("Item price", Money.format(itemPrice))

                        // A discount that is not being taken is not a line
                        // item. The rows appear only when points are in play.
                        if (pointsToUse > 0) {
                            SummaryRow("Points used", "$pointsToUse of $available")
                            SummaryRow(
                                label = "Points discount",
                                value = "-" + Money.PESO + Money.formatPlain(discount.toPlainString()),
                                valueColor = accents.reward,
                            )
                        }

                        SoftDivider(Modifier.padding(vertical = Spacing.xs))

                        SummaryRow(
                            label = "Amount due",
                            value = Money.PESO + Money.formatPlain(amountDue.toPlainString()),
                            emphasized = true,
                        )

                        if (isCash && amountDue.signum() > 0) {
                            SoftDivider(Modifier.padding(vertical = Spacing.xs))
                            SummaryRow(
                                label = "Mandatory 50% DP (via GCash)",
                                value = Money.PESO + Money.formatPlain(downpayment.toPlainString()),
                                valueColor = MaterialTheme.colorScheme.primary,
                                emphasized = true,
                            )
                            SummaryRow(
                                label = "Remaining 50% (Cash on pickup)",
                                value = Money.PESO + Money.formatPlain(remainingBalance.toPlainString()),
                            )
                        }
                    }

                    // ── How the balance is settled ──────────────────────
                    if (isFullPoints) {
                        InfoBanner(
                            title = "Covered entirely by points",
                            text = "Nothing left to pay. Your order goes straight to the " +
                                "admin for pickup - no cash or GCash proof needed.",
                            tone = StatusTone.Success,
                            icon = Icons.Filled.CheckCircle,
                        )
                    } else {
                        MarketCard {
                            Overline("Payment method")
                            Spacer(Modifier.height(Spacing.xs))

                            PaymentMethodOptions(
                                selected = paymentMethod,
                                onSelect = { paymentMethod = it },
                            )
                        }
                    }

                    // ── What they will earn back ────────────────────────
                    if (rewardOnCompletion > 0) {
                        InfoBanner(
                            text = "You will earn $rewardOnCompletion point" +
                                "${if (rewardOnCompletion == 1) "" else "s"} once the admin " +
                                "marks this order completed.",
                            tone = StatusTone.Neutral,
                            icon = Icons.Filled.Stars,
                        )
                    }

                    submitError?.let {
                        InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                    }

                    // ── Confirm ─────────────────────────────────────────
                    // The end of the page rather than a bar welded to the
                    // window, so the button never competes with the gesture
                    // bar for its last few pixels.
                    SoftDivider()

                    if (isCash && amountDue.signum() > 0) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        "Downpayment to pay now",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        "50% via GCash",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                PriceTag(
                                    Money.PESO + Money.formatPlain(downpayment.toPlainString()),
                                    size = PriceSize.Medium,
                                )
                            }
                            Text(
                                "Remaining ${Money.PESO}${Money.formatPlain(remainingBalance.toPlainString())} payable in cash upon pickup at Ofelia's store.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Total to pay",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            PriceTag(
                                Money.PESO + Money.formatPlain(amountDue.toPlainString()),
                                size = PriceSize.Medium,
                            )
                        }
                    }

                    PrimaryButton(
                        text = if (isFullPoints) "Confirm with points" else "Confirm order",
                        onClick = {
                            scope.launch {
                                submitting = true
                                submitError = null
                                val result = withContext(Dispatchers.IO) {
                                    MarketplaceApi.checkout(
                                        token,
                                        item.itemId,
                                        pointsToUse,
                                        paymentMethod,
                                    )
                                }
                                submitting = false
                                when (result) {
                                    is MarketplaceApi.Result.Ok -> placedOrder = result.value
                                    is MarketplaceApi.Result.Failure ->
                                        submitError = result.message
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        loading = submitting,
                    )

                    SafeAreaBottomSpacer()
                }
            }
        }
    }
    }
    }
}

/**
 * Cash or GCash - the two ways to settle what is left after points. Shared
 * with the order history, where an unpaid order can still switch methods, so
 * the choice reads the same in both places.
 */
@Composable
internal fun PaymentMethodOptions(selected: String, onSelect: (String) -> Unit) {
    PaymentOption(
        selected = selected == "cash",
        icon = StoreLogoIcon,
        title = "Cash at the store (50% DP via GCash)",
        subtitle = "Mandatory 50% downpayment via GCash to reserve item. Remaining 50% paid in cash upon pickup at Ofelia's store.",
        onClick = { onSelect("cash") },
    )
    PaymentOption(
        selected = selected == "paymongo" || selected == "gcash",
        icon = GcashLogoIcon,
        title = "Full Payment via GCash",
        subtitle = "Pay 100% instantly using your GCash account.",
        onClick = { onSelect("paymongo") },
    )
}

/** A selectable payment method row. */
@Composable
private fun PaymentOption(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            MarketIcon(
                icon,
                null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(22.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

/**
 * What the buyer sees once the order exists.
 *
 * For GCash this is also where the receipt is uploaded - there is no live
 * payment gateway, so a proof image goes to the admin for manual verification.
 */
@Composable
private fun OrderPlacedSection(
    order: MarketTransaction,
    token: String,
) {
    // The QR, receipt picker and upload all live in PaymentProofDialog now, so
    // the same flow serves this screen and the order history.
    var current by remember { mutableStateOf(order) }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        MarketCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Order placed", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Order #${current.transactionId} - ${current.itemTitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            TransactionStatusPill(current.status, paymentMethod = current.paymentMethod)
        }

        if (current.isPastCutoff || current.pickupScheduleNote != null) {
            InfoBanner(
                title = "Pickup Scheduled (Past 5:00 PM Cutoff)",
                text = current.pickupScheduleNote
                    ?: "This order was placed after the 5:00 PM cutoff and is scheduled for pickup/meet-up tomorrow during opening hours at Ofelia's store.",
                tone = StatusTone.Warning,
                icon = Icons.Filled.Schedule,
            )
        }

        MarketCard {
            Overline("What you paid")
            Spacer(Modifier.height(Spacing.xs))
            SummaryRow("Item price", Money.format(current.subtotal))
            if (current.pointsUsed > 0) {
                SummaryRow("Points used", "${current.pointsUsed}")
                SummaryRow("Points discount", "-" + Money.format(current.pointsDiscountAmount))
            }
            SoftDivider(Modifier.padding(vertical = Spacing.xs))
            SummaryRow("Amount due", Money.format(current.amountDue), emphasized = true)

            if (current.paymentMethod == "cash" && current.downpaymentRequired) {
                SoftDivider(Modifier.padding(vertical = Spacing.xs))
                SummaryRow(
                    label = "Mandatory 50% DP (via GCash)",
                    value = current.downpaymentAmountFormatted.ifBlank { Money.format(current.downpaymentAmount) },
                    valueColor = MaterialTheme.colorScheme.primary,
                    emphasized = true,
                )
                SummaryRow(
                    label = "Remaining 50% (Cash on pickup)",
                    value = current.remainingBalanceFormatted.ifBlank { Money.format(current.remainingBalance) },
                )
            }
        }

        when {
            current.isFullPointsCheckout -> {
                InfoBanner(
                    title = "Nothing left to pay",
                    text = "Your points covered the whole amount. The admin will contact " +
                        "you when the item is ready for pickup.",
                    tone = StatusTone.Success,
                    icon = Icons.Filled.CheckCircle,
                )
            }

            current.paymentMethod == "cash" && current.downpaymentRequired && current.paymentStatus != "verified" && current.paymentProof == null -> {
                var payingDp by remember { mutableStateOf(false) }

                if (payingDp) {
                    PaymentProofDialog(
                        transaction = current,
                        token = token,
                        onDismiss = { payingDp = false },
                        onSubmitted = { updated -> current = updated; payingDp = false },
                    )
                }

                InfoBanner(
                    title = "Mandatory 50% Downpayment via GCash",
                    text = "Send your 50% downpayment (${current.downpaymentAmountFormatted.ifBlank { Money.format(current.downpaymentAmount) }}) through GCash to confirm reservation. Pay remaining balance (${current.remainingBalanceFormatted.ifBlank { Money.format(current.remainingBalance) }}) in cash at store.",
                    tone = StatusTone.Warning,
                    icon = GcashLogoIcon,
                )

                PrimaryButton(
                    text = "Upload GCash DP Proof",
                    onClick = { payingDp = true },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Filled.Payments,
                )
            }

            current.paymentMethod == "paymongo" && current.paymentStatus != "verified" -> {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                var launchingPayMongo by remember { mutableStateOf(false) }
                var payMongoError by remember { mutableStateOf<String?>(null) }

                fun launchPayMongo() {
                    scope.launch {
                        launchingPayMongo = true
                        payMongoError = null
                        val result = withContext(Dispatchers.IO) {
                            MarketplaceApi.createPayMongoCheckoutSession(token, current.transactionId)
                        }
                        launchingPayMongo = false
                        when (result) {
                            is MarketplaceApi.Result.Ok -> {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(result.value.checkoutUrl))
                                context.startActivity(intent)
                            }
                            is MarketplaceApi.Result.Failure -> {
                                payMongoError = result.message
                            }
                        }
                    }
                }

                InfoBanner(
                    title = "GCash Payment Required",
                    text = "Pay ${Money.format(current.amountDue)} using your GCash account.",
                    tone = StatusTone.Warning,
                    icon = GcashLogoIcon,
                )

                payMongoError?.let { err ->
                    InfoBanner(text = err, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                }

                PrimaryButton(
                    text = "Pay with GCash",
                    onClick = { launchPayMongo() },
                    modifier = Modifier.fillMaxWidth(),
                    loading = launchingPayMongo,
                    icon = GcashLogoIcon,
                )
            }

            current.paymentMethod == "gcash" && current.paymentProof == null -> {
                // The same dialog the order history uses, so a buyer who pays
                // later gets an identical flow.
                var paying by remember { mutableStateOf(false) }

                if (paying) {
                    PaymentProofDialog(
                        transaction = current,
                        token = token,
                        onDismiss = { paying = false },
                        onSubmitted = { updated -> current = updated; paying = false },
                    )
                }

                InfoBanner(
                    title = "Payment needed",
                    text = "Send ${Money.format(current.amountDue)} through GCash and upload " +
                        "your receipt so the admin can verify it.",
                    tone = StatusTone.Warning,
                    icon = Icons.Filled.PhoneAndroid,
                )

                PrimaryButton(
                    text = "Pay with GCash",
                    onClick = { paying = true },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Filled.Payments,
                )
            }

            current.awaitingProofReview -> {
                InfoBanner(
                    title = "Proof submitted",
                    text = "The admin is reviewing your GCash receipt. You will be notified " +
                        "once it is verified.",
                    tone = StatusTone.Info,
                    icon = Icons.Filled.HourglassTop,
                )
            }

            else -> {
                InfoBanner(
                    title = "Pay at the store",
                    text = if (current.paymentMethod == "cash" && current.downpaymentRequired) {
                        "Your 50% downpayment is recorded. Bring ${current.remainingBalanceFormatted.ifBlank { Money.format(current.remainingBalance) }} cash when you collect the item at Ofelia's store."
                    } else {
                        "Bring ${Money.format(current.amountDue)} when you collect the item. The admin will confirm your payment on handover."
                    },
                    tone = StatusTone.Info,
                    icon = StoreLogoIcon,
                )
            }
        }

        // Spell out the hold rather than alluding to it - this is the buyer's
        // reassurance that nobody else can take the item while they pay.
        val heldUntil = Dates.short(current.reservedUntil)
        val heldFor = Dates.timeUntil(current.reservedUntil)

        InfoBanner(
            title = "Reserved for you",
            text = if (heldUntil != null && heldFor != null) {
                "No one else can buy this item. It is held until $heldUntil " +
                    "($heldFor left). Reward points are credited once the admin " +
                    "completes the order."
            } else {
                "This item is held for you. Reward points are credited once the " +
                    "admin completes the order."
            },
            tone = StatusTone.Success,
            icon = Icons.Filled.Lock,
        )

        Spacer(Modifier.height(Spacing.xl))
    }
}
