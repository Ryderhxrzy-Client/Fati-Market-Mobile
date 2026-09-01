package com.fati_market

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
                                modifier = Modifier.fillMaxWidth(),
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

                            PaymentOption(
                                selected = paymentMethod == "cash",
                                icon = Icons.Filled.Storefront,
                                title = "Cash at the store",
                                subtitle = "Pay Ofelia in person when you collect the item.",
                                onClick = { paymentMethod = "cash" },
                            )
                            PaymentOption(
                                selected = paymentMethod == "gcash",
                                icon = Icons.Filled.PhoneAndroid,
                                title = "GCash",
                                subtitle = "Send the payment, then upload your receipt for " +
                                    "the admin to verify.",
                                onClick = { paymentMethod = "gcash" },
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
            Icon(
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

        MarketCard {
            Overline("What you paid")
            Spacer(Modifier.height(Spacing.xs))
            SummaryRow("Item price", Money.format(current.subtotal))
            SummaryRow("Points used", "${current.pointsUsed}")
            SummaryRow("Points discount", "-" + Money.format(current.pointsDiscountAmount))
            SoftDivider(Modifier.padding(vertical = Spacing.xs))
            SummaryRow("Amount due", Money.format(current.amountDue), emphasized = true)
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
                    text = "Bring ${Money.format(current.amountDue)} when you collect the item. " +
                        "The admin will confirm your payment on handover.",
                    tone = StatusTone.Info,
                    icon = Icons.Filled.Storefront,
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
