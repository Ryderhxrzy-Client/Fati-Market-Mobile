package com.fati_market

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.fati_market.auth.utils.copyUriToCache
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pay for an order and submit the proof.
 *
 * Shared by the checkout screen and the order history, so a buyer who backed
 * out before paying can finish from their orders list later - the flow and the
 * validation are identical either way.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PaymentProofDialog(
    transaction: MarketTransaction,
    token: String,
    onDismiss: () -> Unit,
    onSubmitted: (MarketTransaction) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var gcash by remember { mutableStateOf<GcashDetails?>(null) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var referenceNumber by remember { mutableStateOf("") }
    var uploading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val isGcash = transaction.paymentMethod == "gcash"

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) { pickedUri = uri; error = null } }

    LaunchedEffect(transaction.transactionId) {
        if (isGcash) {
            val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchPaymentDetails(token) }
            if (result is MarketplaceApi.Result.Ok) gcash = result.value
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeAreaTop(),
            ) {
                TopAppBar(
                    title = {
                        Text(
                            if (isGcash) "Pay with GCash" else "Payment",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    // The container is already inset by safeAreaTop;
                    // without this the bar adds the status bar twice.
                    windowInsets = WindowInsets(0),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.screen),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    // -- What is owed --------------------------------------
                    MarketCard {
                        Overline("Amount to pay")
                        PriceTag(Money.format(transaction.amountDue), size = PriceSize.Large)
                        Text(
                            transaction.itemTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            transaction.receiptNo.ifBlank { "Order #${transaction.transactionId}" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (!isGcash) {
                        // Cash orders have nothing to upload - the admin
                        // confirms the handover in person.
                        InfoBanner(
                            title = "Pay at the store",
                            text = "Bring ${Money.format(transaction.amountDue)} when you " +
                                "collect the item. The admin confirms your payment on handover, " +
                                "and your receipt becomes official then.",
                            tone = StatusTone.Info,
                            icon = Icons.Filled.Storefront,
                        )
                    } else {
                        // -- Step 1: send it ------------------------------
                        MarketCard {
                            Overline("Step 1 - Send the payment")
                            Text(
                                "Scan this QR with your GCash app and send exactly " +
                                    Money.format(transaction.amountDue) + ".",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(230.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center,
                            ) {
                                val qr = gcash?.qrImageUrl

                                if (qr != null) {
                                    AsyncImage(
                                        model = qr,
                                        contentDescription = "GCash QR code",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize().padding(Spacing.md),
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                                    ) {
                                        Icon(
                                            Icons.Filled.QrCode2,
                                            null,
                                            tint = Color(0xFF9A9A92),
                                            modifier = Modifier.size(54.dp),
                                        )
                                        Text(
                                            "QR code not set up yet",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF6B6B64),
                                        )
                                    }
                                }
                            }

                            gcash?.accountNumber?.let { number ->
                                MarketPanel {
                                    SummaryRow("GCash name", gcash?.accountName ?: "-")
                                    SummaryRow("GCash number", number)
                                    SummaryRow(
                                        label = "Amount to send",
                                        value = Money.format(transaction.amountDue),
                                        emphasized = true,
                                    )
                                }
                            }
                        }

                        // -- Step 2: prove it -----------------------------
                        MarketCard {
                            Overline("Step 2 - Submit your proof")
                            Text(
                                "Upload the receipt screenshot and enter the GCash reference " +
                                    "number. The admin reviews it before your order is confirmed.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            OutlinedTextField(
                                value = referenceNumber,
                                onValueChange = { referenceNumber = it; error = null },
                                label = { Text("GCash reference number") },
                                placeholder = { Text("e.g. 1234 567 890123") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.small,
                            )

                            pickedUri?.let { uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected receipt",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(MaterialTheme.colorScheme.surfaceContainer),
                                )
                            }

                            SecondaryButton(
                                text = if (pickedUri == null) "Choose receipt" else "Choose a different file",
                                onClick = { picker.launch("image/*") },
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Filled.Image,
                            )
                        }
                    }

                    error?.let {
                        InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                    }

                    // -- Submit --------------------------------------------
                    // At the end of the page, not welded to the window.
                    SoftDivider()

                    if (isGcash) {
                        PrimaryButton(
                            text = "Submit proof",
                            modifier = Modifier.fillMaxWidth(),
                            enabled = pickedUri != null,
                            loading = uploading,
                            onClick = {
                                val uri = pickedUri ?: return@PrimaryButton

                                scope.launch {
                                    uploading = true
                                    error = null

                                    val prepared = withContext(Dispatchers.IO) {
                                        copyUriToCache(context, uri)
                                    }

                                    if (prepared == null) {
                                        error = "That file could not be read, or it is over 5 MB. " +
                                            "Try another image."
                                        uploading = false
                                        return@launch
                                    }

                                    val (file, mime) = prepared
                                    val result = withContext(Dispatchers.IO) {
                                        MarketplaceApi.uploadPaymentProof(
                                            token,
                                            transaction.transactionId,
                                            file,
                                            mime,
                                            referenceNumber.trim(),
                                        )
                                    }
                                    uploading = false

                                    when (result) {
                                        is MarketplaceApi.Result.Ok -> onSubmitted(result.value)
                                        is MarketplaceApi.Result.Failure -> error = result.message
                                    }
                                }
                            },
                        )
                    } else {
                        PrimaryButton(
                            text = "Got it",
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    SafeAreaBottomSpacer()
                }

            }
        }
    }
}
