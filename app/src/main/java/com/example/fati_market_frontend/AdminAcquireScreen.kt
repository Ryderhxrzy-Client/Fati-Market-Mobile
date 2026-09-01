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
import androidx.compose.ui.text.font.FontWeight
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
 *   2. The price, and where the item lands. Optionally set the public selling
 *      price on the spot - the markup and the buyer's reward points preview
 *      themselves from it - and pick the status it takes once received. The
 *      status follows the price unless it is chosen: priced means published.
 *   3. Mark acquired. Verifies the turnover with the photos attached, records
 *      the payout, and applies the chosen status - through the publish
 *      endpoint when that status is public, so the catalog keeps its rules.
 *
 * Scanning an item the store already holds lands here too, and is not a dead
 * end: the selling price and the status are editable on the spot, so a
 * mispriced or misfiled item is fixed at the counter instead of hunting it
 * down in the inventory lists afterwards.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    // Asked for only when the chat never fixed one: receiving an item nobody
    // priced is not a turnover anybody can account for.
    var acquisitionInput by remember(scannedCode) { mutableStateOf("") }
    var working by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var done by remember(scannedCode) { mutableStateOf(false) }
    var published by remember(scannedCode) { mutableStateOf(false) }

    // The status the item lands in once the turnover is recorded. Left alone it
    // follows the price - typing a public price publishes, the way the field
    // has always promised - and picking one from the dropdown overrides that.
    var chosenStatus by remember(scannedCode) { mutableStateOf<String?>(null) }
    val turnoverStatus = chosenStatus
        ?: if (Money.normalizeInput(publicPrice) != null) "public" else "acquired"

    // For an item the store already holds: the price and status, editable here.
    var editPrice by remember(scannedCode) { mutableStateOf("") }
    var editStatus by remember(scannedCode) { mutableStateOf("") }
    var savedMessage by remember(scannedCode) { mutableStateOf<String?>(null) }

    fun adopt(loaded: Item) {
        item = loaded

        if (loaded.acquisitionPrice == null) {
            acquisitionInput = Money.formatPlain(loaded.sellerAskingPrice)
                .replace(",", "")
                .takeIf { it != "—" }
                ?: ""
        }

        editPrice = Money.formatPlain(loaded.publicPrice)
            .replace(",", "")
            .takeIf { it != "—" }
            ?: ""
        editStatus = loaded.status
    }

    LaunchedEffect(scannedCode) {
        when (val result = withContext(Dispatchers.IO) { MarketplaceApi.scanItem(token, scannedCode) }) {
            is MarketplaceApi.Result.Ok -> adopt(result.value)
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

                                        if (published) {
                                            append(" The listing is live in the catalog.")
                                        } else if (current.status != "acquired") {
                                            append(" It is marked ${itemStatusLabel(current.status)}.")
                                        }
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

                            current.isTurnoverVerified || current.isAcquired || current.isPublic || current.isSold -> {
                                InfoBanner(
                                    title = "Already in the store",
                                    text = "This item was received already. Its price and its " +
                                        "status can be corrected here.",
                                    tone = StatusTone.Info,
                                    icon = Icons.Filled.Inventory,
                                )

                                MarketCard {
                                    Overline("Selling price")
                                    Spacer(Modifier.height(Spacing.xs))

                                    OutlinedTextField(
                                        value = editPrice,
                                        onValueChange = {
                                            editPrice = it
                                            actionError = null
                                            savedMessage = null
                                        },
                                        label = { Text("Public selling price") },
                                        prefix = { Text(Money.PESO) },
                                        placeholder = { Text("e.g. 350.00") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = MaterialTheme.shapes.small,
                                    )

                                    // The same preview the turnover shows, so a
                                    // price typed here is checked against what
                                    // the store paid before it is saved.
                                    Money.parse(Money.normalizeInput(editPrice))?.let { price ->
                                        Spacer(Modifier.height(Spacing.sm))

                                        Money.parse(current.acquisitionPrice)?.let { paid ->
                                            SummaryRow(
                                                "Markup",
                                                Money.PESO + Money.formatPlain(
                                                    price.subtract(paid).toPlainString(),
                                                ),
                                            )
                                        }
                                        SummaryRow(
                                            label = "Buyer will earn",
                                            value = "${LoyaltyRules.rewardPointsFor(Money.normalizeInput(editPrice))} point(s)",
                                            valueColor = accents.reward,
                                        )
                                    }

                                    Spacer(Modifier.height(Spacing.md))

                                    Overline("Status")
                                    Spacer(Modifier.height(Spacing.xs))

                                    StatusDropdown(
                                        value = editStatus,
                                        onSelect = {
                                            editStatus = it
                                            actionError = null
                                            savedMessage = null
                                        },
                                    )
                                }

                                savedMessage?.let {
                                    InfoBanner(
                                        text = it,
                                        tone = StatusTone.Success,
                                        icon = Icons.Filled.CheckCircle,
                                    )
                                }

                                actionError?.let {
                                    InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                                }

                                val priceGiven = editPrice.isNotBlank()
                                val normalizedPrice = Money.normalizeInput(editPrice)
                                val unchanged = editStatus == current.status &&
                                    normalizedPrice == Money.normalizeInput(
                                        Money.formatPlain(current.publicPrice).replace(",", ""),
                                    )

                                PrimaryButton(
                                    text = when {
                                        priceGiven && normalizedPrice == null -> "Enter a valid price"
                                        editStatus == "public" && normalizedPrice == null ->
                                            "A public item needs a price"
                                        else -> "Save changes"
                                    },
                                    enabled = !working && !unchanged &&
                                        (!priceGiven || normalizedPrice != null) &&
                                        (editStatus != "public" || normalizedPrice != null),
                                    loading = working,
                                    onClick = {
                                        scope.launch {
                                            working = true
                                            actionError = null
                                            savedMessage = null

                                            val (ok, message) = withContext(Dispatchers.IO) {
                                                updateAdminItem(
                                                    token,
                                                    current.itemId,
                                                    status = editStatus,
                                                    publicPrice = normalizedPrice,
                                                )
                                            }

                                            if (ok) {
                                                // Read the item back rather than
                                                // guessing: the server decides
                                                // what the status became.
                                                val reloaded = withContext(Dispatchers.IO) {
                                                    MarketplaceApi.scanItem(token, scannedCode)
                                                }

                                                if (reloaded is MarketplaceApi.Result.Ok) adopt(reloaded.value)

                                                savedMessage = "Saved."
                                            } else {
                                                actionError = message.ifBlank {
                                                    "Could not save the changes. Try again."
                                                }
                                            }

                                            working = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = Icons.Filled.Check,
                                )
                            }

                            !current.offerAccepted -> InfoBanner(
                                title = "Offer not accepted yet",
                                text = "Agree an acquisition price with the seller first - " +
                                    "accept the offer in the conversation, then scan again.",
                                tone = StatusTone.Warning,
                                icon = Icons.Filled.HourglassTop,
                            )

                            else -> {
                                // What the store pays the seller: settled in
                                // chat when the offer was accepted, asked for
                                // here when it never was.
                                val needsAgreedPrice = current.acquisitionPrice == null
                                val agreedPrice = current.acquisitionPrice
                                    ?: Money.normalizeInput(acquisitionInput)

                                if (needsAgreedPrice) {
                                    MarketCard {
                                        Overline("Agreed price")
                                        Spacer(Modifier.height(Spacing.xs))
                                        Text(
                                            "No price was agreed in the conversation. Enter what " +
                                                "the store is paying the seller - the asking price " +
                                                "was " + Money.format(current.sellerAskingPrice) + ".",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )

                                        Spacer(Modifier.height(Spacing.sm))

                                        OutlinedTextField(
                                            value = acquisitionInput,
                                            onValueChange = { acquisitionInput = it; actionError = null },
                                            label = { Text("Agreed price") },
                                            prefix = { Text(Money.PESO) },
                                            placeholder = { Text("What the store pays the seller") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = MaterialTheme.shapes.small,
                                        )
                                    }
                                }

                                // ── 1. The proof ─────────────────────────
                                MarketCard {
                                    Overline("Turnover proof")
                                    Spacer(Modifier.height(Spacing.xs))
                                    Text(
                                        "Photograph the item you received, and the seller " +
                                            "receiving their " + Money.format(agreedPrice) + ".",
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
                                        val acquisition = Money.parse(agreedPrice)

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

                                    Spacer(Modifier.height(Spacing.md))

                                    // Where the item lands once it is received.
                                    // Left alone it follows the price - a public
                                    // price means it goes live, which is what the
                                    // field above promises - but the counter is
                                    // not the only place a status may be chosen,
                                    // so any of them can be picked here.
                                    Overline("Status after turnover")
                                    Spacer(Modifier.height(Spacing.xs))

                                    StatusDropdown(
                                        value = turnoverStatus,
                                        onSelect = {
                                            chosenStatus = it
                                            actionError = null
                                        },
                                    )
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

                        // Settled in chat, or typed above - either way the
                        // turnover cannot be recorded without one.
                        val agreedPrice = current.acquisitionPrice
                            ?: Money.normalizeInput(acquisitionInput)

                        val sellingPrice = Money.normalizeInput(publicPrice)

                        PrimaryButton(
                            text = when {
                                agreedPrice == null -> "Enter the agreed price first"
                                !hasProof -> "Take both proof photos first"
                                turnoverStatus == "public" && sellingPrice == null ->
                                    "A public item needs a price"
                                turnoverStatus == "public" -> "Mark acquired & publish"
                                turnoverStatus == "acquired" -> "Mark acquired & seller paid"
                                else -> "Mark acquired as ${itemStatusLabel(turnoverStatus)}"
                            },
                            enabled = hasProof && !working && agreedPrice != null &&
                                (!priceGiven || sellingPrice != null) &&
                                (turnoverStatus != "public" || sellingPrice != null),
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
                                            // Only when it was missing - an
                                            // agreed price already on record
                                            // is not for this screen to rewrite.
                                            acquisitionPrice = if (current.acquisitionPrice == null) {
                                                Money.normalizeInput(acquisitionInput)
                                            } else {
                                                null
                                            },
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

                                    // Publishing goes through its own endpoint,
                                    // which is where the catalog rules live. Any
                                    // other status - and a price recorded without
                                    // going live - is the plain item update.
                                    if (turnoverStatus == "public" && normalized != null) {
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
                                    } else if (normalized != null || turnoverStatus != "acquired") {
                                        val (ok, message) = withContext(Dispatchers.IO) {
                                            updateAdminItem(
                                                token,
                                                current.itemId,
                                                status = turnoverStatus,
                                                publicPrice = normalized,
                                            )
                                        }

                                        if (ok) {
                                            val reloaded = withContext(Dispatchers.IO) {
                                                MarketplaceApi.scanItem(token, scannedCode)
                                            }

                                            if (reloaded is MarketplaceApi.Result.Ok) item = reloaded.value
                                        } else {
                                            actionError = "Acquired, but the status could not be " +
                                                "set to ${itemStatusLabel(turnoverStatus)}: " + message
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

/**
 * The item status picker.
 *
 * The inventory pages are not the only place a status is decided - the counter
 * decides one on every turnover - so both use this, over the one shared
 * [ITEM_STATUS_OPTIONS] list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(
    value: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = value.replaceFirstChar { it.uppercaseChar() },
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ITEM_STATUS_OPTIONS.forEach { status ->
                DropdownMenuItem(
                    text = {
                        Text(
                            status.replaceFirstChar { it.uppercaseChar() },
                            fontWeight = if (status == value) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onSelect(status)
                        expanded = false
                    },
                )
            }
        }
    }
}
