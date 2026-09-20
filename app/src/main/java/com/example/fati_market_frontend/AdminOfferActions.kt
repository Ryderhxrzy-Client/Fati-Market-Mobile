package com.fati_market

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The decisions an offer row carries, as the website's offers page carries
 * them.
 *
 * The app could receive an item and pay for it, but never accept the offer in
 * the first place - that lived only in the chat - and never delete one. An
 * acquired item could only be published by opening the whole edit page, with
 * no sight of the markup or the points the buyer would earn until afterwards.
 */

/**
 * Accepting an offer is agreeing the price the store will pay. There is no
 * separate "approved" flag: a priced offer is an accepted one.
 */
@Composable
internal fun AcceptOfferDialog(
    item: Item,
    token: String,
    onDismiss: () -> Unit,
    onAccepted: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var price by remember {
        mutableStateOf(Money.formatPlain(item.sellerAskingPrice).replace(",", "").takeIf { it != "—" } ?: "")
    }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val normalized = Money.normalizeInput(price)

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        icon = { Icon(Icons.Filled.Check, contentDescription = null) },
        title = { Text("Accept this offer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    "${item.title} · the seller is asking ${Money.format(item.sellerAskingPrice)}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it; error = null },
                    label = { Text("Acquisition price") },
                    prefix = { Text(Money.PESO) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    "What the store pays the seller. They are told in their chat, and their turnover QR starts working.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                error?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !saving && normalized != null,
                onClick = {
                    val agreed = normalized ?: return@Button

                    scope.launch {
                        saving = true
                        error = null

                        when (val result = withContext(Dispatchers.IO) {
                            MarketplaceApi.setAcquisitionPrice(token, item.itemId, agreed)
                        }) {
                            is MarketplaceApi.Result.Ok -> { saving = false; onAccepted(); onDismiss() }
                            is MarketplaceApi.Result.Failure -> { saving = false; error = result.message }
                        }
                    }
                },
            ) {
                Text(if (saving) "Accepting…" else "Accept offer")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") } },
    )
}

/**
 * Publishing, with the price and what it means before it goes live.
 *
 * The preview is the server's own, produced by the code that publishes, so
 * the markup and the reward points shown are the ones that get stored, and
 * anything blocking the sale is named before the tap.
 */
@Composable
internal fun PublishItemDialog(
    item: Item,
    token: String,
    onDismiss: () -> Unit,
    onPublished: () -> Unit,
    onEditDetails: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val accents = LocalMarketAccents.current

    var price by remember {
        mutableStateOf(Money.formatPlain(item.publicPrice).replace(",", "").takeIf { it != "—" } ?: "")
    }
    var preview by remember { mutableStateOf<PublishPreview?>(null) }
    var checking by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val normalized = Money.normalizeInput(price)

    // The preview follows the price, a moment behind the typing.
    LaunchedEffect(normalized) {
        val wanted = normalized

        if (wanted == null) {
            preview = null
            return@LaunchedEffect
        }

        kotlinx.coroutines.delay(400)
        checking = true

        when (val result = withContext(Dispatchers.IO) {
            MarketplaceApi.publishPreview(token, item.itemId, wanted)
        }) {
            is MarketplaceApi.Result.Ok -> { preview = result.value; error = null }
            is MarketplaceApi.Result.Failure -> { preview = null; error = result.message }
        }

        checking = false
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        icon = { Icon(Icons.Filled.Storefront, contentDescription = null) },
        title = { Text("Publish to the catalog") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    "${item.title} · the store paid ${Money.format(item.acquisitionPrice)} for it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it; error = null },
                    label = { Text("Public selling price") },
                    prefix = { Text(Money.PESO) },
                    placeholder = { Text("e.g. 350.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                when {
                    checking -> Text(
                        "Checking…",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    preview != null -> {
                        val data = preview!!

                        SummaryRow("Markup", Money.format(data.markup))
                        SummaryRow(
                            label = "Buyer earns",
                            value = "${data.rewardPoints} point(s)",
                            valueColor = accents.reward,
                        )

                        if (data.blockers.isNotEmpty()) {
                            Text(
                                data.blockers.joinToString("\n"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    else -> Text(
                        "Enter a price to see the markup and the points the buyer earns.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                error?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }

                // The last look before it goes on sale: a student's title and
                // snapshots are not always what the catalog should show.
                TextButton(onClick = { onDismiss(); onEditDetails() }) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(Spacing.xs))
                    Text("Edit title, description and photos")
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !saving && normalized != null && preview?.canPublish != false,
                onClick = {
                    val selling = normalized ?: return@Button

                    scope.launch {
                        saving = true
                        error = null

                        when (val result = withContext(Dispatchers.IO) {
                            MarketplaceApi.publish(token, item.itemId, selling)
                        }) {
                            is MarketplaceApi.Result.Ok -> { saving = false; onPublished(); onDismiss() }
                            is MarketplaceApi.Result.Failure -> { saving = false; error = result.message }
                        }
                    }
                },
            ) {
                Text(if (saving) "Publishing…" else "Publish")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") } },
    )
}

/**
 * Deleting an offer outright, which is never one stray tap.
 *
 * The server allows it only for a listing the store has not taken in, and the
 * chat about the item goes with it, so the dialog says both before asking.
 */
@Composable
internal fun DeleteOfferDialog(
    item: Item,
    token: String,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!working) onDismiss() },
        icon = { Icon(Icons.Filled.DeleteOutline, contentDescription = null) },
        title = { Text("Delete this offer?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    "\"${item.title}\" from ${item.sellerEmail} will be removed from the store for good.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Its photos go with it, and so does the conversation about this item - for both of you. This cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                error?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !working,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = {
                    scope.launch {
                        working = true
                        error = null

                        when (val result = withContext(Dispatchers.IO) {
                            MarketplaceApi.deleteAdminItem(token, item.itemId)
                        }) {
                            is MarketplaceApi.Result.Ok -> { working = false; onDeleted(); onDismiss() }
                            is MarketplaceApi.Result.Failure -> { working = false; error = result.message }
                        }
                    }
                },
            ) {
                Text(if (working) "Deleting…" else "Delete offer")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !working) { Text("Keep it") } },
    )
}
