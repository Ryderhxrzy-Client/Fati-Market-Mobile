package com.fati_market

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * The counter screen behind a scanned *item* QR - the seller-side twin of
 * [AdminScanScreen].
 *
 * A seller whose offer was accepted walks in with the item and their turnover
 * code. Scanning it lands here, with the listing in full - photo, title,
 * description, asking price, the agreed acquisition price - and the handover
 * reads top to bottom:
 *
 *   1. The proof. Two photographs, taken right here: the item being received,
 *      and the seller being paid their cash.
 *   2. The price. Optionally set the public selling price on the spot; the
 *      markup and the buyer's reward points preview themselves from it.
 *   3. Mark acquired. Verifies the turnover with the photos attached, records
 *      the payout, and - when a price was entered - publishes to the catalog.
 */
@Composable
internal fun AdminAcquireScreen(
    scannedCode: String,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val accents = LocalMarketAccents.current
    val scope = rememberCoroutineScope()

    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE) }
    val token = remember { prefs.getString("auth_token", "") ?: "" }

    var item by remember(scannedCode) { mutableStateOf<Item?>(null) }
    var lookupError by remember(scannedCode) { mutableStateOf<String?>(null) }
    var loading by remember(scannedCode) { mutableStateOf(true) }

    var itemPhoto by remember(scannedCode) { mutableStateOf<Bitmap?>(null) }
    var payoutPhoto by remember(scannedCode) { mutableStateOf<Bitmap?>(null) }
    var publicPrice by remember(scannedCode) { mutableStateOf("") }
    var working by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var done by remember(scannedCode) { mutableStateOf(false) }
    var published by remember(scannedCode) { mutableStateOf(false) }

    LaunchedEffect(scannedCode) {
        when (val result = withContext(Dispatchers.IO) { MarketplaceApi.scanItem(token, scannedCode) }) {
            is MarketplaceApi.Result.Ok -> item = result.value
            is MarketplaceApi.Result.Failure -> lookupError = result.message
        }
        loading = false
    }

    // Which of the two slots the camera is filling.
    var capturing by remember { mutableStateOf("item") }

    val takePhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap != null) {
            if (capturing == "item") itemPhoto = bitmap else payoutPhoto = bitmap
        }
    }

    val askCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) takePhoto.launch(null)
        else actionError = "Camera permission is needed to photograph the turnover."
    }

    fun capture(slot: String) {
        capturing = slot
        askCameraPermission.launch(Manifest.permission.CAMERA)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ──────────────────────────────────────────────────
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                    Column {
                        Text("Item turnover", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Scanned listing",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                when {
                    loading -> Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.xxxl),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }

                    lookupError != null -> InfoBanner(
                        title = "Not a valid turnover code",
                        text = lookupError ?: "",
                        tone = StatusTone.Danger,
                        icon = Icons.Filled.ErrorOutline,
                    )

                    else -> item?.let { current ->
                        AcquireItemSummary(current)

                        when {
                            done -> {
                                InfoBanner(
                                    title = if (published) "Acquired and published" else "Item acquired",
                                    text = buildString {
                                        append("The item is in the store and the seller's payout is recorded.")
                                        if (published) append(" The listing is live in the catalog.")
                                    },
                                    tone = StatusTone.Success,
                                    icon = Icons.Filled.CheckCircle,
                                )

                                PrimaryButton(
                                    text = "Done",
                                    onClick = onClose,
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = Icons.Filled.Check,
                                )
                            }

                            current.isRejected -> InfoBanner(
                                title = "This offer was declined",
                                text = current.rejectedReason ?: "There is nothing to receive.",
                                tone = StatusTone.Danger,
                                icon = Icons.Filled.Block,
                            )

                            current.isTurnoverVerified || current.isAcquired || current.isPublic || current.isSold -> InfoBanner(
                                title = "Already received",
                                text = "This item is in the store already - nothing to do here.",
                                tone = StatusTone.Info,
                                icon = Icons.Filled.Inventory,
                            )

                            !current.offerAccepted -> InfoBanner(
                                title = "Offer not accepted yet",
                                text = "Agree an acquisition price with the seller first - " +
                                    "accept the offer in the conversation, then scan again.",
                                tone = StatusTone.Warning,
                                icon = Icons.Filled.HourglassTop,
                            )

                            else -> {
                                // ── 1. The proof ─────────────────────────
                                MarketCard {
                                    Overline("Turnover proof")
                                    Spacer(Modifier.height(Spacing.xs))
                                    Text(
                                        "Photograph the item you received, and the seller " +
                                            "receiving their " + Money.format(current.acquisitionPrice) + ".",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )

                                    Spacer(Modifier.height(Spacing.sm))

                                    ProofSlot(
                                        label = "Item received",
                                        bitmap = itemPhoto,
                                        onCapture = { capture("item") },
                                    )

                                    Spacer(Modifier.height(Spacing.sm))

                                    ProofSlot(
                                        label = "Seller paid",
                                        bitmap = payoutPhoto,
                                        onCapture = { capture("payout") },
                                    )
                                }

                                // ── 2. The price ─────────────────────────
                                MarketCard {
                                    Overline("Selling price (optional)")
                                    Spacer(Modifier.height(Spacing.xs))
                                    Text(
                                        "Set the public price now to publish straight to the " +
                                            "catalog, or leave it blank and price it later.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )

                                    Spacer(Modifier.height(Spacing.sm))

                                    OutlinedTextField(
                                        value = publicPrice,
                                        onValueChange = { publicPrice = it; actionError = null },
                                        label = { Text("Public selling price") },
                                        prefix = { Text(Money.PESO) },
                                        placeholder = { Text("e.g. 350.00") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = MaterialTheme.shapes.small,
                                    )

                                    // The same rules the server applies, previewed
                                    // locally: markup is derived, amber means points.
                                    Money.parse(Money.normalizeInput(publicPrice))?.let { price ->
                                        val acquisition = Money.parse(current.acquisitionPrice)

                                        Spacer(Modifier.height(Spacing.sm))

                                        acquisition?.let {
                                            SummaryRow(
                                                "Markup",
                                                Money.PESO + Money.formatPlain(price.subtract(it).toPlainString()),
                                            )
                                        }
                                        SummaryRow(
                                            label = "Buyer will earn",
                                            value = "${LoyaltyRules.rewardPointsFor(Money.normalizeInput(publicPrice))} point(s)",
                                            valueColor = accents.reward,
                                        )
                                    }
                                }

                                actionError?.let {
                                    InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                                }
                            }
                        }
                    }
                }
            }

            // ── 3. Mark acquired ─────────────────────────────────────────
            val current = item

            val actionable = current != null && !done && lookupError == null &&
                current.offerAccepted && !current.isRejected &&
                !(current.isTurnoverVerified || current.isAcquired || current.isPublic || current.isSold)

            if (actionable && current != null) {
                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                    ) {
                        val hasProof = itemPhoto != null && payoutPhoto != null
                        val priceGiven = publicPrice.isNotBlank()

                        PrimaryButton(
                            text = when {
                                !hasProof -> "Take both proof photos first"
                                priceGiven -> "Mark acquired & publish"
                                else -> "Mark acquired & seller paid"
                            },
                            enabled = hasProof && !working &&
                                (!priceGiven || Money.normalizeInput(publicPrice) != null),
                            loading = working,
                            onClick = {
                                val proofItem = itemPhoto ?: return@PrimaryButton
                                val proofPayout = payoutPhoto ?: return@PrimaryButton

                                scope.launch {
                                    working = true
                                    actionError = null

                                    val result = withContext(Dispatchers.IO) {
                                        fun Bitmap.toCacheFile(prefix: String): File {
                                            val file = File.createTempFile(prefix, ".jpg", context.cacheDir)
                                            file.outputStream().use { out ->
                                                compress(Bitmap.CompressFormat.JPEG, 85, out)
                                            }
                                            return file
                                        }

                                        val itemFile = proofItem.toCacheFile("turnover")
                                        val payoutFile = proofPayout.toCacheFile("payout")

                                        MarketplaceApi.verifyTurnoverWithPhotos(
                                            token,
                                            current.itemId,
                                            itemFile,
                                            payoutFile,
                                        ).also {
                                            itemFile.delete()
                                            payoutFile.delete()
                                        }
                                    }

                                    when (result) {
                                        is MarketplaceApi.Result.Failure -> {
                                            working = false
                                            actionError = result.message
                                            return@launch
                                        }
                                        is MarketplaceApi.Result.Ok -> item = result.value
                                    }

                                    // The payout is part of the counter moment: the
                                    // photo above is the cash changing hands.
                                    withContext(Dispatchers.IO) {
                                        MarketplaceApi.recordSellerPayout(token, current.itemId, null)
                                    }

                                    val normalized = Money.normalizeInput(publicPrice)

                                    if (normalized != null) {
                                        val publishResult = withContext(Dispatchers.IO) {
                                            MarketplaceApi.publish(token, current.itemId, normalized)
                                        }

                                        when (publishResult) {
                                            is MarketplaceApi.Result.Ok -> {
                                                item = publishResult.value
                                                published = true
                                            }
                                            is MarketplaceApi.Result.Failure ->
                                                actionError = "Acquired, but publishing failed: " +
                                                    publishResult.message
                                        }
                                    }

                                    working = false
                                    done = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Filled.DoneAll,
                            containerColor = accents.success,
                        )
                    }

                    // The clearance, laid out like the tab bar's.
                    SafeAreaBottomSpacer()
                    }
                }
            }
        }
    }
}

/** One of the two proof photographs, with its capture button. */
@Composable
private fun ProofSlot(label: String, bitmap: Bitmap?, onCapture: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
        }

        SecondaryButton(
            text = if (bitmap == null) "Take photo: $label" else "Retake: $label",
            onClick = onCapture,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Filled.PhotoCamera,
        )
    }
}

/** The listing, in full, as it matters at the counter. */
@Composable
private fun AcquireItemSummary(item: Item) {
    MarketCard {
        item.photos.firstOrNull()?.let { photo ->
            AsyncImage(
                model = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
            Spacer(Modifier.height(Spacing.sm))
        }

        Text(item.title, style = MaterialTheme.typography.titleMedium)

        if (item.description.isNotBlank()) {
            Text(
                item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SoftDivider()

        SummaryRow("Seller", item.sellerEmail.substringBefore("@"))
        SummaryRow("Asking price", Money.format(item.sellerAskingPrice))
        SummaryRow("Agreed price", Money.format(item.acquisitionPrice), emphasized = true)

        item.meetupSchedule?.let { schedule ->
            Dates.short(schedule)?.let { SummaryRow("Meet-up", it) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            ItemStatusPill(item.status, offerAccepted = item.offerAccepted)
        }
    }
}
