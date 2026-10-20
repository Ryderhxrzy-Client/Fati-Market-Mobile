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
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventBusy
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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
private val DAY_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

/** Sunday first, the way wall calendars here are laid out. */
private val WEEK = listOf(DayOfWeek.SUNDAY) + DayOfWeek.values().filter { it != DayOfWeek.SUNDAY }

/**
 * Schedule turnover date: the seller picks a day in the current month to turn over the item to Ofelia.
 * No time slot required - the seller can drop it off anytime during store hours on the chosen day.
 */
@Composable
internal fun MeetupScheduleDialog(
    itemId: Int,
    token: String,
    current: String?,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var hours by remember { mutableStateOf<StoreHours?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    var date by remember { mutableStateOf<LocalDate?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(reloadKey) {
        loadError = null

        when (val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchStoreHours(token) }) {
            is MarketplaceApi.Result.Ok -> {
                val loaded = result.value
                hours = loaded

                val now = ZonedDateTime.now(loaded.zone)
                val booked = Dates.parse(current)?.atZone(loaded.zone)

                if (booked != null && loaded.isBookableDay(booked.toLocalDate(), now)) {
                    date = booked.toLocalDate()
                }
            }
            is MarketplaceApi.Result.Failure -> loadError = result.message
        }
    }

    Dialog(onDismissRequest = { if (!saving) onDismiss() }) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Text("Schedule turnover date", style = MaterialTheme.typography.titleLarge)

                val loaded = hours

                when {
                    loadError != null -> {
                        InfoBanner(
                            title = "Could not load store schedule",
                            text = loadError!!,
                            tone = StatusTone.Danger,
                            icon = Icons.Filled.ErrorOutline,
                        )
                        SecondaryButton("Try again", { reloadKey++ }, Modifier.fillMaxWidth())
                    }

                    loaded == null -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.xl),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }

                    else -> {
                        val now = remember(loaded) { ZonedDateTime.now(loaded.zone) }

                        Text(
                            "Store hours: ${loaded.daysLabel()} · ${loaded.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        MonthCalendar(
                            hours = loaded,
                            now = now,
                            selected = date,
                            onSelect = {
                                date = it
                                saveError = null
                            },
                        )

                        val day = date
                        if (day != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            ) {
                                Column(modifier = Modifier.padding(Spacing.sm)) {
                                    Text(
                                        "Selected date: ${day.format(DAY_FORMAT)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                    Text(
                                        "Bring the item to Ofelia's store anytime during store hours on this day.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    )
                                }
                            }
                        } else if (!loaded.hasBookableDay(now)) {
                            InfoBanner(
                                title = "No dates left this month",
                                text = "Every open day in ${YearMonth.from(now).format(MONTH_FORMAT)} has passed.",
                                tone = StatusTone.Warning,
                                icon = Icons.Filled.EventBusy,
                            )
                        }
                    }
                }

                saveError?.let {
                    InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    SecondaryButton("Cancel", { if (!saving) onDismiss() }, Modifier.weight(1f))
                    PrimaryButton(
                        text = "Save date",
                        onClick = {
                            val day = date
                            if (day != null) {
                                scope.launch {
                                    saving = true
                                    saveError = null

                                    val schedule = String.format(
                                        Locale.US,
                                        "%04d-%02d-%02d 09:00:00",
                                        day.year, day.monthValue, day.dayOfMonth,
                                    )

                                    when (val result = withContext(Dispatchers.IO) {
                                        MarketplaceApi.setMeetupSchedule(token, itemId, schedule)
                                    }) {
                                        is MarketplaceApi.Result.Ok -> onSaved()
                                        is MarketplaceApi.Result.Failure -> saveError = result.message
                                    }

                                    saving = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = date != null,
                        loading = saving,
                    )
                }
            }
        }
    }
}

/** The current month as a grid, Sunday first; days that cannot be booked are dimmed. */
@Composable
private fun MonthCalendar(
    hours: StoreHours,
    now: ZonedDateTime,
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
) {
    val month = YearMonth.from(now)
    val today = now.toLocalDate()

    val leadingBlanks = month.atDay(1).dayOfWeek.value % 7
    val cells: List<LocalDate?> =
        List(leadingBlanks) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Text(month.format(MONTH_FORMAT), style = MaterialTheme.typography.titleMedium)

        Row(modifier = Modifier.fillMaxWidth()) {
            WEEK.forEach { dow ->
                Text(
                    dow.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        cells.chunked(7).forEach { week ->
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
                            DayCell(
                                day = day,
                                bookable = hours.isBookableDay(day, now),
                                isToday = day == today,
                                isSelected = day == selected,
                                onClick = { onSelect(day) },
                            )
                        }
                    }
                }
                repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: LocalDate,
    bookable: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(if (isSelected) colors.primary else Color.Transparent)
            .then(if (isToday && !isSelected) Modifier.border(1.dp, colors.primary, CircleShape) else Modifier)
            .clickable(enabled = bookable, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            day.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isSelected -> colors.onPrimary
                bookable -> colors.onSurface
                else -> colors.onSurface.copy(alpha = 0.3f)
            },
        )
    }
}
