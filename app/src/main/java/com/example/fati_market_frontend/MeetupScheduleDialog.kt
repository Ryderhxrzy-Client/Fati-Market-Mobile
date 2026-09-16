// BOOKING/SCHEDULE DISABLED - no longer required
// package com.fati_market

// import androidx.compose.foundation.BorderStroke
// import androidx.compose.foundation.background
// import androidx.compose.foundation.border
// import androidx.compose.foundation.clickable
// import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.rememberScrollState
// import androidx.compose.foundation.shape.CircleShape
// import androidx.compose.foundation.verticalScroll
// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.ErrorOutline
// import androidx.compose.material.icons.filled.EventBusy
// import androidx.compose.material3.*
// import androidx.compose.runtime.*
// import androidx.compose.ui.Alignment
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.draw.clip
// import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.text.font.FontWeight
// import androidx.compose.ui.text.style.TextAlign
// import androidx.compose.ui.unit.dp
// import androidx.compose.ui.window.Dialog
// import com.fati_market.ui.components.*
// import com.fati_market.ui.theme.Spacing
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.launch
// import kotlinx.coroutines.withContext
// import java.time.DayOfWeek
// import java.time.LocalDate
// import java.time.LocalTime
// import java.time.YearMonth
// import java.time.ZonedDateTime
// import java.time.format.DateTimeFormatter
// import java.time.format.TextStyle
// import java.util.Locale

// private val MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
// private val DAY_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
// private val SLOT_FORMAT = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

// /** Sunday first, the way wall calendars here are laid out. */
// private val WEEK = listOf(DayOfWeek.SUNDAY) + DayOfWeek.values().filter { it != DayOfWeek.SUNDAY }

// /**
 // * Book the meet-up: a day in the current month, then a slot in store hours.
 // *
 // * Only this month is shown, and a day is greyed out when it has passed, the
 // * store is closed, or no slot is left in it. The hours come from the server -
 // * the same figures it checks the booking against - so every choice offered
 // * here is one it accepts. Success shows up when the chat poll repaints
 // * whatever opened this.
 // */
// @Composable
// internal fun MeetupScheduleDialog(
    // itemId: Int,
    // token: String,
    // current: String?,
    // onDismiss: () -> Unit,
    // onSaved: () -> Unit,
// ) {
    // val scope = rememberCoroutineScope()

    // var hours by remember { mutableStateOf<StoreHours?>(null) }
    // var loadError by remember { mutableStateOf<String?>(null) }
    // var reloadKey by remember { mutableStateOf(0) }

    // var date by remember { mutableStateOf<LocalDate?>(null) }
    // var time by remember { mutableStateOf<LocalTime?>(null) }
    // var saving by remember { mutableStateOf(false) }
    // var saveError by remember { mutableStateOf<String?>(null) }

    // LaunchedEffect(reloadKey) {
        // loadError = null

        // when (val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchStoreHours(token) }) {
            // is MarketplaceApi.Result.Ok -> {
                // val loaded = result.value
                // hours = loaded

                // // Start from the booked slot while it is still bookable.
                // val now = ZonedDateTime.now(loaded.zone)
                // val booked = Dates.parse(current)?.atZone(loaded.zone)

                // if (booked != null &&
                    // loaded.isBookableDay(booked.toLocalDate(), now) &&
                    // booked.toLocalTime() in loaded.slotsOn(booked.toLocalDate(), now)
                // ) {
                    // date = booked.toLocalDate()
                    // time = booked.toLocalTime()
                // }
            // }
            // is MarketplaceApi.Result.Failure -> loadError = result.message
        // }
    // }

    // Dialog(onDismissRequest = { if (!saving) onDismiss() }) {
        // Surface(
            // shape = MaterialTheme.shapes.large,
            // color = MaterialTheme.colorScheme.surface,
            // tonalElevation = 2.dp,
        // ) {
            // Column(
                // modifier = Modifier
                    // .verticalScroll(rememberScrollState())
                    // .padding(Spacing.xl),
                // verticalArrangement = Arrangement.spacedBy(Spacing.md),
            // ) {
                // Text("Schedule meet-up", style = MaterialTheme.typography.titleLarge)

                // val loaded = hours

                // when {
                    // loadError != null -> {
                        // InfoBanner(
                            // title = "Could not load the store hours",
                            // text = loadError!!,
                            // tone = StatusTone.Danger,
                            // icon = Icons.Filled.ErrorOutline,
                        // )
                        // SecondaryButton("Try again", { reloadKey++ }, Modifier.fillMaxWidth())
                    // }

                    // loaded == null -> Box(
                        // modifier = Modifier
                            // .fillMaxWidth()
                            // .padding(Spacing.xl),
                        // contentAlignment = Alignment.Center,
                    // ) {
                        // CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    // }

                    // else -> {
                        // val now = remember(loaded) { ZonedDateTime.now(loaded.zone) }

                        // Text(
                            // "Store hours: ${loaded.daysLabel()} · ${loaded.label}",
                            // style = MaterialTheme.typography.bodySmall,
                            // color = MaterialTheme.colorScheme.onSurfaceVariant,
                        // )

                        // MonthCalendar(
                            // hours = loaded,
                            // now = now,
                            // selected = date,
                            // onSelect = {
                                // date = it
                                // time = null
                                // saveError = null
                            // },
                        // )

                        // val day = date

                        // if (day != null) {
                            // Overline("Time · " + day.format(DAY_FORMAT))
                            // TimeSlots(
                                // slots = loaded.slotsOn(day, now),
                                // selected = time,
                                // onSelect = {
                                    // time = it
                                    // saveError = null
                                // },
                            // )
                        // } else if (!loaded.hasBookableDay(now)) {
                            // InfoBanner(
                                // title = "No slots left this month",
                                // text = "Every open day in ${YearMonth.from(now).format(MONTH_FORMAT)} " +
                                    // "has passed. Meet-ups can only be booked within the current month.",
                                // tone = StatusTone.Warning,
                                // icon = Icons.Filled.EventBusy,
                            // )
                        // }
                    // }
                // }

                // saveError?.let {
                    // InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                // }

                // Row(
                    // modifier = Modifier.fillMaxWidth(),
                    // horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                // ) {
                    // SecondaryButton("Cancel", { if (!saving) onDismiss() }, Modifier.weight(1f))
                    // PrimaryButton(
                        // text = "Save",
                        // onClick = {
                            // val day = date
                            // val slot = time

                            // if (day != null && slot != null) {
                                // scope.launch {
                                    // saving = true
                                    // saveError = null

                                    // // Wall-clock time in the store's own zone,
                                    // // which is how the server reads it.
                                    // val schedule = String.format(
                                        // Locale.US,
                                        // "%04d-%02d-%02d %02d:%02d:00",
                                        // day.year, day.monthValue, day.dayOfMonth, slot.hour, slot.minute,
                                    // )

                                    // when (val result = withContext(Dispatchers.IO) {
                                        // MarketplaceApi.setMeetupSchedule(token, itemId, schedule)
                                    // }) {
                                        // is MarketplaceApi.Result.Ok -> onSaved()
                                        // is MarketplaceApi.Result.Failure -> saveError = result.message
                                    // }

                                    // saving = false
                                // }
                            // }
                        // },
                        // modifier = Modifier.weight(1f),
                        // enabled = date != null && time != null,
                        // loading = saving,
                    // )
                // }
            // }
        // }
    // }
// }

// /** The current month as a grid, Sunday first; days that cannot be booked are dimmed. */
// @Composable
// private fun MonthCalendar(
    // hours: StoreHours,
    // now: ZonedDateTime,
    // selected: LocalDate?,
    // onSelect: (LocalDate) -> Unit,
// ) {
    // val month = YearMonth.from(now)
    // val today = now.toLocalDate()

    // // DayOfWeek.value runs 1 (Monday) to 7 (Sunday); Sunday leads the grid.
    // val leadingBlanks = month.atDay(1).dayOfWeek.value % 7
    // val cells: List<LocalDate?> =
        // List(leadingBlanks) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }

    // Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        // Text(month.format(MONTH_FORMAT), style = MaterialTheme.typography.titleMedium)

        // Row(modifier = Modifier.fillMaxWidth()) {
            // WEEK.forEach { dow ->
                // Text(
                    // dow.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    // modifier = Modifier.weight(1f),
                    // textAlign = TextAlign.Center,
                    // style = MaterialTheme.typography.labelSmall,
                    // color = MaterialTheme.colorScheme.onSurfaceVariant,
                // )
            // }
        // }

        // cells.chunked(7).forEach { week ->
            // Row(modifier = Modifier.fillMaxWidth()) {
                // week.forEach { day ->
                    // Box(
                        // modifier = Modifier
                            // .weight(1f)
                            // .aspectRatio(1f)
                            // .padding(2.dp),
                        // contentAlignment = Alignment.Center,
                    // ) {
                        // if (day != null) {
                            // DayCell(
                                // day = day,
                                // bookable = hours.isBookableDay(day, now),
                                // isToday = day == today,
                                // isSelected = day == selected,
                                // onClick = { onSelect(day) },
                            // )
                        // }
                    // }
                // }
                // repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
            // }
        // }
    // }
// }

// @Composable
// private fun DayCell(
    // day: LocalDate,
    // bookable: Boolean,
    // isToday: Boolean,
    // isSelected: Boolean,
    // onClick: () -> Unit,
// ) {
    // val colors = MaterialTheme.colorScheme

    // Box(
        // modifier = Modifier
            // .fillMaxSize()
            // .clip(CircleShape)
            // .background(if (isSelected) colors.primary else Color.Transparent)
            // .then(if (isToday && !isSelected) Modifier.border(1.dp, colors.primary, CircleShape) else Modifier)
            // .clickable(enabled = bookable, onClick = onClick),
        // contentAlignment = Alignment.Center,
    // ) {
        // Text(
            // day.dayOfMonth.toString(),
            // style = MaterialTheme.typography.bodyMedium,
            // fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            // color = when {
                // isSelected -> colors.onPrimary
                // bookable -> colors.onSurface
                // else -> colors.onSurface.copy(alpha = 0.3f)
            // },
        // )
    // }
// }

// /** The day's remaining slots, three to a row. */
// @Composable
// private fun TimeSlots(
    // slots: List<LocalTime>,
    // selected: LocalTime?,
    // onSelect: (LocalTime) -> Unit,
// ) {
    // val colors = MaterialTheme.colorScheme

    // Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        // slots.chunked(3).forEach { row ->
            // Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                // row.forEach { slot ->
                    // val isSelected = slot == selected

                    // Surface(
                        // onClick = { onSelect(slot) },
                        // modifier = Modifier
                            // .weight(1f)
                            // .height(40.dp),
                        // shape = MaterialTheme.shapes.small,
                        // color = if (isSelected) colors.primary else colors.surfaceContainerLow,
                        // border = if (isSelected) null else BorderStroke(1.dp, colors.outlineVariant),
                    // ) {
                        // Box(contentAlignment = Alignment.Center) {
                            // Text(
                                // slot.format(SLOT_FORMAT),
                                // style = MaterialTheme.typography.labelLarge,
                                // color = if (isSelected) colors.onPrimary else colors.onSurface,
                            // )
                        // }
                    // }
                // }
                // repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            // }
        // }
    // }
// }
