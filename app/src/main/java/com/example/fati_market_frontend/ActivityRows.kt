package com.fati_market

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.Spacing

/**
 * The store's history, one line at a time.
 *
 * Every row used to be the same grey icon and a sentence, which made a
 * student registering look exactly like the store handing an item over, and
 * left nothing to tap. A row now carries the face of whoever it belongs to -
 * the student's own photo on their registration, the admin's on a handover,
 * the other party's beside it - and opens on the facts behind the sentence.
 */

/** The action behind a line, as an icon and a colour. */
@Composable
private fun activityBadge(action: String): Pair<ImageVector, Color> {
    val accents = LocalMarketAccents.current

    return when (action) {
        "create" -> Icons.Filled.Add to accents.info
        "purchase" -> Icons.Filled.ShoppingCart to accents.success
        "delete" -> Icons.Filled.Close to MaterialTheme.colorScheme.error
        else -> Icons.Filled.Edit to accents.reward
    }
}

@Composable
internal fun ActivityRow(entry: ActivityEntry, onClick: () -> Unit) {
    val (icon, tint) = activityBadge(entry.action)

    MarketCard(onClick = onClick) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Avatar(
                    url = entry.actor.photo,
                    initial = entry.actor.initial,
                    size = 44.dp,
                )

                // The action itself, small, on the corner of the face - so a
                // row says who and what without two competing icons.
                Surface(
                    color = tint,
                    shape = CircleShape,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomEnd),
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(3.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(entry.description, style = MaterialTheme.typography.bodyMedium)
                Text(
                    entry.user + (Dates.short(entry.timestamp)?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // The other person in the event, when there is one and it is not
            // the same face over again.
            entry.subject
                ?.takeIf { it.userId != 0 && it.userId != entry.actor.userId }
                ?.let { subject ->
                    Avatar(url = subject.photo, initial = subject.initial, size = 26.dp)
                }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** A line of the feed, opened: who it was, and the facts behind the sentence. */
@Composable
internal fun ActivityDetailDialog(entry: ActivityEntry, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Activity") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(url = entry.actor.photo, initial = entry.actor.initial, size = 48.dp)

                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.user, style = MaterialTheme.typography.titleSmall)

                        val line = listOf(entry.actor.email, entry.actor.role)
                            .filter { it.isNotBlank() }
                            .joinToString(" · ")

                        if (line.isNotBlank()) {
                            Text(
                                line,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Text(entry.description, style = MaterialTheme.typography.bodyMedium)

                entry.subject
                    ?.takeIf { it.userId != 0 && it.userId != entry.actor.userId }
                    ?.let { subject ->
                        SoftDivider()

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(url = subject.photo, initial = subject.initial, size = 32.dp)

                            Column {
                                Text(
                                    "The other person",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(subject.name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                SoftDivider()

                SummaryRow("Kind", entry.resourceType.replaceFirstChar { it.uppercaseChar() })
                SummaryRow("Reference", "#" + entry.resourceId)

                Dates.full(entry.timestamp)?.let { SummaryRow("When", it) }

                entry.details.forEach { detail ->
                    SummaryRow(detail.label, detail.value)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}
