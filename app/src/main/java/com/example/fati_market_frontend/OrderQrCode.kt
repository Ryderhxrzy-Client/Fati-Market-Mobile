package com.fati_market

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.Spacing
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The buyer's walk-in pickup code.
 *
 * Every order carries a signed code from the server; rendered as a QR it is
 * what the buyer shows at the counter. Admin scans it and lands on this exact
 * order - no receipt numbers read aloud, no searching by name.
 */

/** Render a code as a QR bitmap. Pure zxing, done off the main thread. */
internal suspend fun renderQrBitmap(content: String, sizePx: Int = 720): Bitmap =
    withContext(Dispatchers.Default) {
        val matrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            sizePx,
            sizePx,
            mapOf(
                EncodeHintType.MARGIN to 1,
                // Medium correction: the code is short, screens are clean.
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            ),
        )

        val pixels = IntArray(sizePx * sizePx) { i ->
            if (matrix.get(i % sizePx, i / sizePx)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }

        Bitmap.createBitmap(pixels, sizePx, sizePx, Bitmap.Config.ARGB_8888)
    }

/**
 * The full-screen code the buyer holds up.
 *
 * Always drawn black-on-white regardless of theme - scanners want contrast,
 * not dark mode.
 */
@Composable
internal fun PickupQrDialog(order: MarketTransaction, onDismiss: () -> Unit) {
    val code = order.qrCode ?: return
    var qr by remember(code) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(code) { qr = renderQrBitmap(code) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Text("Pickup code", style = MaterialTheme.typography.titleLarge)
                Text(
                    order.receiptNo.ifBlank { "Order #${order.transactionId}" } +
                        " · " + order.itemTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(Spacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    val bitmap = qr

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Pickup QR code",
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }

                Text(
                    "Show this at the store. Ofelia scans it to pull up your " +
                        "order and hand your item over.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                SecondaryButton("Close", onDismiss, Modifier.fillMaxWidth())
            }
        }
    }
}

/** The row-level entry point: a button that opens [PickupQrDialog]. */
@Composable
internal fun PickupQrButton(order: MarketTransaction, modifier: Modifier = Modifier) {
    if (order.qrCode == null || order.isTerminal) return

    var showing by remember { mutableStateOf(false) }

    if (showing) {
        PickupQrDialog(order = order, onDismiss = { showing = false })
    }

    SecondaryButton(
        text = "Show pickup code",
        onClick = { showing = true },
        modifier = modifier,
        icon = Icons.Filled.QrCode2,
    )
}


/**
 * The seller-side twin of [PickupQrDialog]: the turnover code an accepted
 * offer carries, shown when the seller brings the item in.
 */
@Composable
internal fun ItemQrDialog(item: Item, onDismiss: () -> Unit) {
    val code = item.qrCode ?: return
    var qr by remember(code) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(code) { qr = renderQrBitmap(code) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Text("Turnover code", style = MaterialTheme.typography.titleLarge)
                Text(
                    item.title + " · agreed at " + Money.format(item.acquisitionPrice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(Spacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    val bitmap = qr

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Turnover QR code",
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }

                Text(
                    "Show this when you bring the item to Ofelia Store. " +
                        "Scanning it marks the hand-over and your cash payout.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                SecondaryButton("Close", onDismiss, Modifier.fillMaxWidth())
            }
        }
    }
}

/** The button that opens [ItemQrDialog], shown only while the code exists. */
@Composable
internal fun ItemQrButton(item: Item, modifier: Modifier = Modifier) {
    if (item.qrCode == null) return

    var showing by remember { mutableStateOf(false) }

    if (showing) {
        ItemQrDialog(item = item, onDismiss = { showing = false })
    }

    SecondaryButton(
        text = "Show turnover code",
        onClick = { showing = true },
        modifier = modifier,
        icon = Icons.Filled.QrCode2,
    )
}
