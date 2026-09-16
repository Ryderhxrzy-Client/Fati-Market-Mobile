// BOOKING/SCHEDULE DISABLED - no longer required
// package com.fati_market

// import androidx.compose.foundation.clickable
// import androidx.compose.foundation.layout.*
// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.ChevronRight
// import androidx.compose.material3.*
// import androidx.compose.runtime.*
// import androidx.compose.ui.Alignment
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.draw.clip
// import androidx.compose.ui.text.style.TextOverflow
// import androidx.compose.ui.unit.dp
// import com.fati_market.ui.components.SectionHeader
// import com.fati_market.ui.components.SoftDivider
// import com.fati_market.ui.components.StatusPill
// import com.fati_market.ui.components.StatusTone
// import com.fati_market.ui.theme.Elevation
// import com.fati_market.ui.theme.Spacing
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.withContext
// import java.time.Instant
// import java.time.LocalDate
// import java.time.ZoneId
// import java.time.format.DateTimeFormatter
// import java.util.Locale

// private val WEEKDAY = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
// private val DAY_OF_MONTH = DateTimeFormatter.ofPattern("d", Locale.getDefault())
// private val MONTH_DAY = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
// private val CLOCK = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

// /**
 // * The meet-ups Ofelia has booked: sellers due at the store with an item.
 // *
 // * Read from the pending offers. A meet-up only matters until the item is
 // * received, and receiving it moves the offer out of pending, so the list
 // * clears itself as the counter does its work. One whose time has passed with
 // * the item still not in is flagged as missed. Tapping any of them opens the
 // * same picker the chat uses, to move it.
 // */
// @Composable
// internal fun AdminMeetupsCard(token: String, modifier: Modifier = Modifier) {
    // var meetups by remember { mutableStateOf<List<Pair<Item, Instant>>?>(null) }
    // var error by remember { mutableStateOf<String?>(null) }
    // var refreshKey by remember { mutableStateOf(0) }
    // var rescheduling by remember { mutableStateOf<Item?>(null) }

    // LaunchedEffect(refreshKey) {
        // when (val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchAdminItems(token, "pending") }) {
            // is MarketplaceApi.Result.Ok -> {
                // meetups = result.value
                    // .mapNotNull { item -> Dates.parse(item.meetupSchedule)?.let { item to it } }
                    // .sortedBy { it.second }
                // error = null
            // }
            // is MarketplaceApi.Result.Failure -> error = result.message
        // }
    // }

    // rescheduling?.let { item ->
        // MeetupScheduleDialog(
            // itemId = item.itemId,
            // token = token,
            // current = item.meetupSchedule,
            // onDismiss = { rescheduling = null },
            // onSaved = {
                // rescheduling = null
                // refreshKey++
            // },
        // )
    // }

    // val list = meetups

    // Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        // SectionHeader(
            // title = "Meet-ups",
            // subtitle = "Sellers bringing their items to the store",
            // actionLabel = if (error != null) "Retry" else null,
            // onAction = { refreshKey++ },
        // )

        // Surface(
            // shape = MaterialTheme.shapes.medium,
            // color = MaterialTheme.colorScheme.surface,
            // shadowElevation = Elevation.card,
        // ) {
            // Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)) {
                // when {
                    // error != null -> Text(
                        // error!!,
                        // style = MaterialTheme.typography.bodyMedium,
                        // color = MaterialTheme.colorScheme.error,
                        // modifier = Modifier.padding(vertical = Spacing.sm),
                    // )

                    // list == null -> LinearProgressIndicator(
                        // modifier = Modifier
                            // .fillMaxWidth()
                            // .padding(vertical = Spacing.md),
                    // )

                    // list.isEmpty() -> Text(
                        // "No meet-ups booked. Set one from an accepted offer in its chat.",
                        // style = MaterialTheme.typography.bodyMedium,
                        // color = MaterialTheme.colorScheme.onSurfaceVariant,
                        // modifier = Modifier.padding(vertical = Spacing.md),
                    // )

                    // else -> list.forEachIndexed { index, (item, at) ->
                        // if (index > 0) SoftDivider()
                        // MeetupRow(item = item, at = at, onClick = { rescheduling = item })
                    // }
                // }
            // }
        // }
    // }
// }

// /** One booking: a calendar-leaf date, the item, when, and who is bringing it. */
// @Composable
// private fun MeetupRow(item: Item, at: Instant, onClick: () -> Unit) {
    // val colors = MaterialTheme.colorScheme
    // val zone = ZoneId.systemDefault()
    // val local = at.atZone(zone)
    // val today = LocalDate.now(zone)
    // val missed = at.isBefore(Instant.now())

    // val dayLabel = when (local.toLocalDate()) {
        // today -> "Today"
        // today.plusDays(1) -> "Tomorrow"
        // else -> local.format(MONTH_DAY)
    // }

    // Row(
        // modifier = Modifier
            // .fillMaxWidth()
            // .clip(MaterialTheme.shapes.small)
            // .clickable(onClick = onClick)
            // .padding(vertical = Spacing.sm),
        // horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        // verticalAlignment = Alignment.CenterVertically,
    // ) {
        // val leafContent = if (missed) colors.onErrorContainer else colors.onPrimaryContainer

        // Surface(
            // shape = MaterialTheme.shapes.small,
            // color = if (missed) colors.errorContainer else colors.primaryContainer,
            // modifier = Modifier.size(48.dp),
        // ) {
            // Column(
                // modifier = Modifier.fillMaxSize(),
                // horizontalAlignment = Alignment.CenterHorizontally,
                // verticalArrangement = Arrangement.Center,
            // ) {
                // Text(
                    // local.format(WEEKDAY).uppercase(),
                    // style = MaterialTheme.typography.labelSmall,
                    // color = leafContent,
                // )
                // Text(
                    // local.format(DAY_OF_MONTH),
                    // style = MaterialTheme.typography.titleMedium,
                    // color = leafContent,
                // )
            // }
        // }

        // Column(modifier = Modifier.weight(1f)) {
            // Text(
                // item.title,
                // style = MaterialTheme.typography.titleSmall,
                // maxLines = 1,
                // overflow = TextOverflow.Ellipsis,
            // )
            // Text(
                // "$dayLabel · ${local.format(CLOCK)}",
                // style = MaterialTheme.typography.bodySmall,
                // color = if (missed) colors.error else colors.primary,
            // )
            // Text(
                // item.sellerEmail,
                // style = MaterialTheme.typography.labelSmall,
                // color = colors.onSurfaceVariant,
                // maxLines = 1,
                // overflow = TextOverflow.Ellipsis,
            // )
        // }

        // if (missed) StatusPill(label = "Missed", tone = StatusTone.Danger)

        // Icon(Icons.Filled.ChevronRight, contentDescription = "Reschedule", tint = colors.onSurfaceVariant)
    // }
// }
