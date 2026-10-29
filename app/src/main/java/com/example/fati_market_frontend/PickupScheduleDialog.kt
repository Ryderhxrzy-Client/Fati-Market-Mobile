package com.fati_market

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val MONTH_HEADER_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
private val DATE_FULL_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
private val CALENDAR_WEEK = listOf(DayOfWeek.SUNDAY) + DayOfWeek.values().filter { it != DayOfWeek.SUNDAY }

/**
 * Standalone Date-Only Modal Picker for Pickup/Meetup (No Time Slots).
 * Matches the modal styling of the app.
 */
@Composable
internal fun PickupDatePickerModal(
    storeHours: StoreHours?,
    current: LocalDate?,
    isAdmin: Boolean = false,
    title: String = "Select pick-up date",
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val loaded = storeHours
    val now = remember(loaded) {
        if (loaded != null) ZonedDateTime.now(loaded.zone) else ZonedDateTime.now()
    }
    val openDays = loaded?.openDays ?: setOf(1, 2, 3, 4, 5, 6)
    val isClosedOrPastCutoff = loaded?.isPastCutoff(now) ?: false

    // Earliest selectable date calculation
    val earliestDate: LocalDate = remember(now, openDays, isClosedOrPastCutoff, isAdmin) {
        if (isAdmin) {
            var cand = now.toLocalDate()
            while (!openDays.contains(cand.dayOfWeek.value)) {
                cand = cand.plusDays(1)
            }
            cand
        } else if (!isClosedOrPastCutoff && openDays.contains(now.dayOfWeek.value)) {
            now.toLocalDate()
        } else {
            var cand = now.toLocalDate().plusDays(1)
            while (!openDays.contains(cand.dayOfWeek.value)) {
                cand = cand.plusDays(1)
            }
            cand
        }
    }

    // Default to earliestDate if current is null or in the past
    val initialDate = remember(current, earliestDate) {
        if (current != null && !current.isBefore(earliestDate)) current else earliestDate
    }
    var selectedDate by remember(initialDate) { mutableStateOf<LocalDate?>(initialDate) }
    var displayedMonth by remember(initialDate) {
        mutableStateOf(YearMonth.from(initialDate))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                if (loaded == null) {
                    Box(modifier = Modifier.fillMaxWidth().padding(Spacing.xl), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    val openDays = loaded.openDays
                    val isClosedOrPastCutoff = loaded.isPastCutoff(now)

                    // Earliest selectable date is calculated above

                    if (!isAdmin && isClosedOrPastCutoff) {
                        InfoBanner(
                            title = "Store is closed",
                            text = "Store is closed. Choose a pick-up/meet-up date from ${earliestDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))}.",
                            tone = StatusTone.Warning,
                            icon = Icons.Filled.Storefront,
                        )
                    }

                    Text(
                        text = "Store open: Monday to Saturday · 8:00 AM – 6:00 PM",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Month Navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { displayedMonth = displayedMonth.minusMonths(1) },
                            enabled = displayedMonth.isAfter(YearMonth.from(now))
                        ) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Month")
                        }

                        Text(
                            text = displayedMonth.format(MONTH_HEADER_FORMAT),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        IconButton(
                            onClick = { displayedMonth = displayedMonth.plusMonths(1) },
                            enabled = displayedMonth.isBefore(YearMonth.from(now).plusMonths(3))
                        ) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Next Month")
                        }
                    }

                    // Day of week headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CALENDAR_WEEK.forEach { dow ->
                            Text(
                                text = dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Days grid
                    val firstOfMonth = displayedMonth.atDay(1)
                    val leadingBlanks = firstOfMonth.dayOfWeek.value % 7
                    val daysInMonth = (1..displayedMonth.lengthOfMonth()).map { displayedMonth.atDay(it) }
                    val allCells: List<LocalDate?> = List(leadingBlanks) { null } + daysInMonth

                    allCells.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            week.forEach { day ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (day != null) {
                                        val isSelectable = !day.isBefore(earliestDate) && openDays.contains(day.dayOfWeek.value)
                                        val isSelected = day == selectedDate
                                        val isToday = day == now.toLocalDate()
                                        val colors = MaterialTheme.colorScheme

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(if (isSelected) colors.primary else Color.Transparent)
                                                .then(if (isToday && !isSelected) Modifier.border(1.dp, colors.primary, CircleShape) else Modifier)
                                                .clickable(enabled = isSelectable) {
                                                    selectedDate = day
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = day.dayOfMonth.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> colors.onPrimary
                                                    isSelectable -> colors.onSurface
                                                    else -> colors.onSurface.copy(alpha = 0.25f)
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                            repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }

                    // Selected Date Preview
                    val picked = selectedDate
                    if (picked != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        ) {
                            Row(
                                modifier = Modifier.padding(Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                Icon(
                                    Icons.Filled.EventAvailable,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Selected date: ${picked.format(DATE_FULL_FORMAT)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                    Text(
                                        text = "Pick up at Ofelia's store during store hours (8AM-6PM) on this day.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    SecondaryButton("Cancel", onDismiss, Modifier.weight(1f))
                    PrimaryButton(
                        text = "Confirm date",
                        onClick = {
                            selectedDate?.let(onConfirm)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedDate != null,
                    )
                }
            }
        }
    }
}

/**
 * Dialog for Admin to set or extend an order's pickup date.
 * Directly wraps [PickupDatePickerModal] and saves to the server.
 */
@Composable
internal fun PickupScheduleDialog(
    transactionId: Int,
    token: String,
    current: String?,
    isAdmin: Boolean = false,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var storeHours by remember { mutableStateOf<StoreHours?>(null) }
    var saving by remember { mutableStateOf(false) }

    val currentLocalDate = remember(current) {
        current?.takeIf { it.isNotBlank() }?.let {
            runCatching { LocalDate.parse(it.substringBefore(" ")) }.getOrNull()
        }
    }

    LaunchedEffect(transactionId) {
        val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchStoreHours(token) }
        if (result is MarketplaceApi.Result.Ok) {
            storeHours = result.value
        }
    }

    PickupDatePickerModal(
        storeHours = storeHours,
        current = currentLocalDate,
        isAdmin = isAdmin,
        title = if (isAdmin) "Set / Extend pick-up date" else "Select pick-up date",
        onDismiss = { if (!saving) onDismiss() },
        onConfirm = { picked ->
            scope.launch {
                saving = true
                val dateStr = picked.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val result = withContext(Dispatchers.IO) {
                    MarketplaceApi.setPickupSchedule(token, transactionId, dateStr, isAdmin)
                }
                saving = false
                if (result is MarketplaceApi.Result.Ok) {
                    android.widget.Toast.makeText(
                        context,
                        "Pick-up date extended to ${picked.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    onSaved()
                } else if (result is MarketplaceApi.Result.Failure) {
                    android.widget.Toast.makeText(
                        context,
                        "Failed: ${result.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )
}
