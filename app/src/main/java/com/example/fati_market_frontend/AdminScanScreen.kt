package com.fati_market

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
 * The counter screen behind the Scan button.
 *
 * A buyer walks in and shows their pickup QR; scanning it lands here with the
 * exact order on screen. The handover then reads top to bottom:
 *
 *   1. The money. An unpaid cash bill is approved here (Ofelia has just been
 *      handed the cash); an unverified GCash proof likewise. Completion stays
 *      locked until the payment is verified - the server enforces it, this
 *      screen just makes the order of events visible.
 *   2. The proof. A photo of the buyer receiving the item, taken right here.
 *   3. Complete. Credits the buyer's reward points, exactly once.
 *
 * Manual completion in the conversation still exists; this flow only adds the
 * scan and the photograph.
 */
@Composable
internal fun AdminScanScreen(
    scannedCode: String,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val accents = LocalMarketAccents.current
    val scope = rememberCoroutineScope()

    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE) }
    val token = remember { prefs.getString("auth_token", "") ?: "" }

    var order by remember(scannedCode) { mutableStateOf<MarketTransaction?>(null) }
    var lookupError by remember(scannedCode) { mutableStateOf<String?>(null) }
    var loading by remember(scannedCode) { mutableStateOf(true) }
    var refreshKey by remember(scannedCode) { mutableStateOf(0) }

    var handoverPhoto by remember(scannedCode) { mutableStateOf<Bitmap?>(null) }
    var working by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var completedPoints by remember(scannedCode) { mutableStateOf<Int?>(null) }
    var pendingAction by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(scannedCode, refreshKey) {
        loading = order == null

        when (val result = withContext(Dispatchers.IO) { MarketplaceApi.scanOrder(token, scannedCode) }) {
            is MarketplaceApi.Result.Ok -> { order = result.value; lookupError = null }
            is MarketplaceApi.Result.Failure -> lookupError = result.message
        }

        loading = false
    }

    // The camera. TakePicturePreview hands back a bitmap directly - enough
    // for a handover proof, with no file provider ceremony.
    val takePhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview(),
    ) { bitmap -> if (bitmap != null) handoverPhoto = bitmap }

    val askCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) takePhoto.launch(null)
        else actionError = "Camera permission is needed to photograph the handover."
    }

    // Approve / decline reuse the same confirmations the conversation uses,
    // so the wording of a decision never depends on where it was made.
    pendingAction?.let { action ->
        order?.let { current ->
            ChatOrderActionDialog(
                transaction = current,
                action = action,
                token = token,
                onDismiss = { pendingAction = null },
                onDone = { pendingAction = null; refreshKey++ },
            )
        }
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
                        Text("Walk-in pickup", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Scanned order",
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
                        title = "Not a valid pickup code",
                        text = lookupError ?: "",
                        tone = StatusTone.Danger,
                        icon = Icons.Filled.ErrorOutline,
                    )

                    else -> order?.let { current ->
                        ScanOrderSummary(current)

                        val donePoints = completedPoints

                        if (donePoints != null || current.status == "completed") {
                            InfoBanner(
                                title = "Order completed",
                                text = if ((donePoints ?: 0) > 0) {
                                    "The buyer just earned $donePoints reward point(s). " +
                                        "They are told in the chat and by notification."
                                } else {
                                    "This order is already completed."
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
                        } else if (current.isTerminal) {
                            InfoBanner(
                                title = "This order is closed",
                                text = "It was cancelled or rejected, so there is nothing to hand over.",
                                tone = StatusTone.Danger,
                                icon = Icons.Filled.Block,
                            )
                        } else {
                            // ── 1. The money ─────────────────────────────
                            if (current.paymentStatus != "verified") {
                                InfoBanner(
                                    title = "Payment first",
                                    text = when {
                                        current.paymentMethod == "cash" ->
                                            "Take the cash, then approve the payment here. " +
                                                "The order cannot be completed before that."
                                        current.paymentStatus == "proof_submitted" ->
                                            "A GCash receipt is waiting for review. Approve it " +
                                                "before handing the item over."
                                        else ->
                                            "This order is unpaid. It cannot be completed until " +
                                                "the payment is verified."
                                    },
                                    tone = StatusTone.Warning,
                                    icon = Icons.Filled.Payments,
                                )

                                if (current.canDo("verify_payment")) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                        PrimaryButton(
                                            text = "Approve payment",
                                            onClick = { pendingAction = "verify_payment" },
                                            modifier = Modifier.weight(1f),
                                            icon = Icons.Filled.Check,
                                            containerColor = accents.success,
                                        )

                                        if (current.canDo("reject_payment")) {
                                            SecondaryButton(
                                                text = "Decline",
                                                onClick = { pendingAction = "reject_payment" },
                                                modifier = Modifier.weight(1f),
                                                contentColor = MaterialTheme.colorScheme.error,
                                            )
                                        }
                                    }
                                }
                            }

                            // ── 2. The proof ─────────────────────────────
                            MarketCard {
                                Overline("Handover photo")
                                Spacer(Modifier.height(Spacing.xs))
                                Text(
                                    "Photograph the buyer receiving the item. This is the " +
                                        "proof the item really changed hands.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                Spacer(Modifier.height(Spacing.sm))

                                val photo = handoverPhoto

                                if (photo != null) {
                                    Image(
                                        bitmap = photo.asImageBitmap(),
                                        contentDescription = "Handover photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                    )
                                    Spacer(Modifier.height(Spacing.sm))
                                }

                                SecondaryButton(
                                    text = if (photo == null) "Take photo" else "Retake photo",
                                    onClick = { askCameraPermission.launch(Manifest.permission.CAMERA) },
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = Icons.Filled.PhotoCamera,
                                )
                            }

                            actionError?.let {
                                InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                            }
                        }
                    }
                }
            }

            // ── 3. Complete ──────────────────────────────────────────────
            val current = order

            if (current != null && !current.isTerminal && completedPoints == null && lookupError == null) {
                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        val paid = current.paymentStatus == "verified"
                        val hasPhoto = handoverPhoto != null

                        if (current.canDo("mark_ready_for_pickup")) {
                            SecondaryButton(
                                text = "Ready for pickup",
                                onClick = { pendingAction = "mark_ready_for_pickup" },
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Filled.Inventory,
                            )
                        }

                        PrimaryButton(
                            text = when {
                                !paid -> "Verify the payment first"
                                !hasPhoto -> "Take the handover photo first"
                                else -> "Complete transaction"
                            },
                            enabled = paid && hasPhoto && !working,
                            loading = working,
                            onClick = {
                                val photo = handoverPhoto ?: return@PrimaryButton

                                scope.launch {
                                    working = true
                                    actionError = null

                                    val result = withContext(Dispatchers.IO) {
                                        val file = File.createTempFile("handover", ".jpg", context.cacheDir)

                                        file.outputStream().use { out ->
                                            photo.compress(Bitmap.CompressFormat.JPEG, 85, out)
                                        }

                                        MarketplaceApi.completeTransactionWithPhoto(
                                            token,
                                            current.transactionId,
                                            file,
                                            "image/jpeg",
                                        ).also { file.delete() }
                                    }

                                    working = false

                                    when (result) {
                                        is MarketplaceApi.Result.Ok -> {
                                            order = result.value
                                            completedPoints = result.value.rewardPointsToCredit
                                        }
                                        is MarketplaceApi.Result.Failure -> actionError = result.message
                                    }
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

/** The order, as it matters at the counter. */
@Composable
private fun ScanOrderSummary(order: MarketTransaction) {
    val accents = LocalMarketAccents.current

    MarketCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (order.itemPhoto != null) {
                AsyncImage(
                    model = order.itemPhoto,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(order.itemTitle, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Text(
                    order.receiptNo.ifBlank { "Order #${order.transactionId}" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SoftDivider()

        SummaryRow(
            "Buyer",
            order.buyerName ?: order.buyerEmail.substringBefore("@"),
        )
        SummaryRow("Amount due", Money.format(order.amountDue), emphasized = true)

        if (order.pointsUsed > 0) {
            SummaryRow(
                label = "${order.pointsUsed} point(s) used",
                value = "-" + Money.format(order.pointsDiscountAmount),
                valueColor = accents.reward,
            )
        }

        SummaryRow("Payment", paymentMethodLabel(order.paymentMethod))

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            PaymentStatusPill(order.paymentStatus, order.isFullPointsCheckout)
            TransactionStatusPill(order.status)
        }
    }
}
