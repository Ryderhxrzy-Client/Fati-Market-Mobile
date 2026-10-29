package com.fati_market

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.AsyncImage
import com.fati_market.auth.utils.copyUriToCache
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.Elevation
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.style.TextAlign
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Buyer checkout.
 *
 * Points act as a discount here, never as the currency. The buyer chooses how
 * many of their points to spend, sees the whole breakdown update as they do,
 * and settles whatever is left through PayMongo or with a 50% PayMongo
 * downpayment followed by the remaining cash balance at pickup.
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
    onOrderPlaced: () -> Unit = {},
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token = remember { prefs.getString("auth_token", "") ?: "" }
    val scope = rememberCoroutineScope()
    val accents = LocalMarketAccents.current

    var quote by remember { mutableStateOf<CheckoutQuote?>(null) }
    var storeHours by remember { mutableStateOf<StoreHours?>(null) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    var pointsToUse by remember { mutableStateOf(0) }
    var paymentMethod by remember { mutableStateOf("cash") }
    var submitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }

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
        val hoursRes = withContext(Dispatchers.IO) { MarketplaceApi.fetchStoreHours(token) }
        if (hoursRes is MarketplaceApi.Result.Ok) {
            storeHours = hoursRes.value
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

    val now = remember(storeHours) {
        val zone = storeHours?.zone ?: ZoneId.of("Asia/Manila")
        ZonedDateTime.now(zone)
    }
    val openDays = storeHours?.openDays ?: setOf(1, 2, 3, 4, 5, 6)
    val isPastCutoff = quote?.isPastCutoff == true || runCatching {
        val cal = java.util.Calendar.getInstance()
        cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY ||
        cal.get(java.util.Calendar.HOUR_OF_DAY) >= 18
    }.getOrDefault(false)

    val earliestDate: LocalDate = remember(now, openDays, isPastCutoff) {
        if (!isPastCutoff && openDays.contains(now.dayOfWeek.value)) {
            now.toLocalDate()
        } else {
            var cand = now.toLocalDate().plusDays(1)
            while (!openDays.contains(cand.dayOfWeek.value)) {
                cand = cand.plusDays(1)
            }
            cand
        }
    }

    var selectedPickupDate by remember(earliestDate) { mutableStateOf(earliestDate) }
    var displayedMonth by remember(earliestDate) { mutableStateOf(YearMonth.from(earliestDate)) }
    var showDatePickerModal by remember { mutableStateOf(false) }

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

    val submitOrder: () -> Unit = {
        scope.launch {
            submitting = true
            submitError = null
            val result = withContext(Dispatchers.IO) {
                val pickupDateStr = selectedPickupDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                MarketplaceApi.checkout(token, item.itemId, pointsToUse, paymentMethod, pickupDateStr)
            }
            when (result) {
                is MarketplaceApi.Result.Ok -> {
                    onOrderPlaced()
                    if (!result.value.isFullPointsCheckout) {
                        when (val payment = withContext(Dispatchers.IO) {
                            MarketplaceApi.createPayMongoCheckoutSession(
                                token,
                                result.value.transactionId,
                            )
                        }) {
                            is MarketplaceApi.Result.Ok -> {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse(payment.value.checkoutUrl),
                                )
                                context.startActivity(intent)
                                onBack()
                            }
                            is MarketplaceApi.Result.Failure -> {
                                android.widget.Toast.makeText(
                                    context,
                                    "Order saved. Continue payment from My Orders. ${payment.message}",
                                    android.widget.Toast.LENGTH_LONG,
                                ).show()
                                onBack()
                            }
                        }
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            "Order placed successfully.",
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                        onBack()
                    }
                }
                is MarketplaceApi.Result.Failure -> submitError = result.message
            }
            submitting = false
        }
    }

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
        bottomBar = {
            if (!loading && loadError == null) {
                CheckoutConfirmBar(
                    amountDue = Money.PESO + Money.formatPlain(amountDue.toPlainString()),
                    downpayment = Money.PESO + Money.formatPlain(downpayment.toPlainString()),
                    remainingBalance = Money.PESO + Money.formatPlain(remainingBalance.toPlainString()),
                    isCash = isCash,
                    isFullPoints = isFullPoints,
                    submitting = submitting,
                    onConfirm = submitOrder,
                )
            }
        },
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

                    val nextOpenDayFormatted = earliestDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))

                    if (isPastCutoff) {
                        InfoBanner(
                            title = "Store is closed",
                            text = "Store is closed. Choose a pick-up/meet-up date from $nextOpenDayFormatted.",
                            tone = StatusTone.Warning,
                            icon = Icons.Filled.Storefront,
                        )
                    } else {
                        InfoBanner(
                            title = "Store is open (until 6:00 PM)",
                            text = "Store hours: Monday to Saturday, 8:00 AM – 6:00 PM. Choose your preferred pick-up date (today or an upcoming open day).",
                            tone = StatusTone.Info,
                            icon = Icons.Filled.Schedule,
                        )
                    }

                    // ── Pick-up Schedule Date Selector (Modal Trigger) ───
                    if (showDatePickerModal) {
                        PickupDatePickerModal(
                            storeHours = storeHours,
                            current = selectedPickupDate,
                            isAdmin = false,
                            title = "Choose pick-up date",
                            onDismiss = { showDatePickerModal = false },
                            onConfirm = { picked ->
                                selectedPickupDate = picked
                                showDatePickerModal = false
                            }
                        )
                    }

                    MarketCard(onClick = { showDatePickerModal = true }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pick-up schedule date", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(Spacing.xxs))
                                Text(
                                    text = selectedPickupDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = "Tap to change date · Store open: Mon-Sat (8AM – 6PM)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Filled.DateRange,
                                contentDescription = "Select pick-up date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
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
                                    "1 point = 1 peso (₱1.00 discount)",
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

                            val usingPoints = pointsToUse > 0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(
                                        if (usingPoints) accents.reward.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                    .border(
                                        width = if (usingPoints) 2.dp else 1.dp,
                                        color = if (usingPoints) accents.reward else MaterialTheme.colorScheme.outlineVariant,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clickable {
                                        pointsToUse = if (usingPoints) 0 else maxUsable
                                    }
                                    .padding(Spacing.sm),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Noticeable Points Icon with distinct border
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (usingPoints) accents.rewardContainer else MaterialTheme.colorScheme.surfaceContainerHighest)
                                        .border(
                                            width = if (usingPoints) 2.dp else 1.dp,
                                            color = if (usingPoints) accents.reward else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.Stars,
                                        contentDescription = "Points",
                                        tint = if (usingPoints) accents.reward else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        if (usingPoints) {
                                            "Using $pointsToUse point(s) (₱${pointsToUse}.00 discount)"
                                        } else {
                                            "Use my $maxUsable point(s) (1 point = 1 peso)"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (usingPoints) FontWeight.Bold else FontWeight.Medium,
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
                                                ) + " off (1 pt = ₱1)"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (usingPoints) accents.reward else MaterialTheme.colorScheme.onSurfaceVariant,
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
                                label = "50% downpayment (via PayMongo)",
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
                }
            }
        }
    }
    }
}

/** Fixed checkout action bar matching the Item Details purchase bar. */
@Composable
private fun CheckoutConfirmBar(
    amountDue: String,
    downpayment: String,
    remainingBalance: String,
    isCash: Boolean,
    isFullPoints: Boolean,
    submitting: Boolean,
    onConfirm: () -> Unit,
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
                    .heightIn(min = 72.dp)
                    .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = when {
                        isFullPoints -> "Total after points"
                        isCash -> "Downpayment due (50%)"
                        else -> "Total via PayMongo"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PriceTag(
                    price = if (isCash) downpayment else amountDue,
                    size = PriceSize.Medium,
                )
                if (isCash) {
                    Text(
                        text = "Remaining $remainingBalance cash at pickup",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }

            PrimaryButton(
                text = when {
                    isFullPoints -> "Confirm with points"
                    isCash -> "Pay 50% via PayMongo"
                    else -> "Pay via PayMongo"
                },
                onClick = onConfirm,
                compact = true,
                loading = submitting,
            )
            }
        }
    }
}

/**
 * Cash pickup or full PayMongo payment - the two ways to settle what is left after points. Shared
 * with the order history, where an unpaid order can still switch methods, so
 * the choice reads the same in both places.
 */
@Composable
internal fun PaymentMethodOptions(selected: String, onSelect: (String) -> Unit) {
    PaymentOption(
        selected = selected == "cash",
        icon = StoreLogoIcon,
        title = "Cash on pickup (50% Downpayment required)",
        subtitle = "A mandatory 50% downpayment via PayMongo is required to reserve the item. Pay the remaining 50% cash when picking up at Ofelia's store.",
        onClick = { onSelect("cash") },
    )
    PaymentOption(
        selected = selected == "paymongo" || selected == "gcash",
        icon = GcashLogoIcon,
        title = "Full payment via PayMongo",
        subtitle = "Pay 100% online now securely via PayMongo using GCash.",
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
 * Online payments continue through PayMongo. If the buyer leaves the gateway,
 * the same action remains available from My Orders.
 */
@Composable
private fun OrderPlacedSection(
    order: MarketTransaction,
    token: String,
    paymentError: String? = null,
) {
    // The QR, receipt picker and upload all live in PaymentProofDialog now, so
    // the same flow serves this screen and the order history.
    var current by remember { mutableStateOf(order) }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        paymentError?.let { error ->
            InfoBanner(
                title = "Order saved, payment not opened",
                text = "$error Use the payment button below or continue later from My Orders.",
                tone = StatusTone.Danger,
                icon = Icons.Filled.ErrorOutline,
            )
        }

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
                    Text("Order reserved", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Order #${current.transactionId} was created once",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            TransactionStatusPill(current.status, paymentMethod = current.paymentMethod)
        }

        InfoBanner(
            title = "Next: complete your payment",
            text = if (current.paymentMethod == "cash" && current.downpaymentRequired) {
                "Pay the 50% downpayment in PayMongo now. The remaining 50% is paid in cash when you collect ${current.itemTitle}."
            } else if (current.isFullPointsCheckout) {
                "No payment gateway is needed because your points covered the order."
            } else {
                "Complete the payment in PayMongo. This does not create another order."
            },
            tone = if (current.isFullPointsCheckout) StatusTone.Success else StatusTone.Info,
            icon = if (current.isFullPointsCheckout) Icons.Filled.CheckCircle else Icons.Filled.Payments,
        )

        if (current.isPastCutoff || current.pickupScheduleNote != null) {
            InfoBanner(
                title = "Cut off time: 6pm and Monday to Saturday",
                text = current.pickupScheduleNote
                    ?: "This order was placed after the 6:00 PM cutoff and is scheduled for pickup/meet-up tomorrow during opening hours at Ofelia's store.",
                tone = StatusTone.Warning,
                icon = Icons.Filled.Schedule,
            )
        }

        MarketCard {
            Overline("Payment breakdown")
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
                    label = "50% downpayment (via PayMongo)",
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

            current.paymentMethod == "cash" && current.downpaymentRequired && current.paymentStatus == "unpaid" -> {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                var launchingPayMongo by remember { mutableStateOf(false) }
                var payMongoError by remember { mutableStateOf<String?>(null) }

                InfoBanner(
                    title = "Downpayment still pending",
                    text = "This is the same order #${current.transactionId}. Continue the ${current.downpaymentAmountFormatted.ifBlank { Money.format(current.downpaymentAmount) }} PayMongo payment; pay the remaining ${current.remainingBalanceFormatted.ifBlank { Money.format(current.remainingBalance) }} in cash at pickup.",
                    tone = StatusTone.Warning,
                    icon = GcashLogoIcon,
                )

                payMongoError?.let { err ->
                    InfoBanner(text = err, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                }

                PrimaryButton(
                    text = "Continue 50% payment",
                    onClick = {
                        scope.launch {
                            launchingPayMongo = true
                            payMongoError = null
                            when (val result = withContext(Dispatchers.IO) {
                                MarketplaceApi.createPayMongoCheckoutSession(token, current.transactionId)
                            }) {
                                is MarketplaceApi.Result.Ok -> {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(result.value.checkoutUrl),
                                    )
                                    context.startActivity(intent)
                                }
                                is MarketplaceApi.Result.Failure -> payMongoError = result.message
                            }
                            launchingPayMongo = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    loading = launchingPayMongo,
                    icon = GcashLogoIcon,
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
                    title = "PayMongo payment required",
                    text = "Pay ${Money.format(current.amountDue)} securely through PayMongo using GCash.",
                    tone = StatusTone.Warning,
                    icon = GcashLogoIcon,
                )

                payMongoError?.let { err ->
                    InfoBanner(text = err, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                }

                PrimaryButton(
                    text = "Continue with PayMongo",
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
