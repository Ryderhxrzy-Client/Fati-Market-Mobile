package com.fati_market

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fati_market.ui.components.ChoiceChip
import com.fati_market.ui.components.ErrorState
import com.fati_market.ui.components.InfoBanner
import com.fati_market.ui.components.LoadingState
import com.fati_market.ui.components.MarketPageTopBar
import com.fati_market.ui.components.MarketTextField
import com.fati_market.ui.components.PrimaryButton
import com.fati_market.ui.components.RowDivider
import com.fati_market.ui.components.SettingsGroup
import com.fati_market.ui.components.SettingsRow
import com.fati_market.ui.components.StatusTone
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val ADMIN_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

/** Admin editor for the store hours served to every schedule picker. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun AdminStoreHoursSettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val token = remember { context.getSharedPreferences("fatimarket_prefs", 0).getString("auth_token", "").orEmpty() }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var loaded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    var reload by remember { mutableIntStateOf(0) }
    var openTime by remember { mutableStateOf(LocalTime.of(8, 0)) }
    var closeTime by remember { mutableStateOf(LocalTime.of(17, 0)) }
    var openDays by remember { mutableStateOf((1..6).toSet()) }
    var slotMinutes by remember { mutableStateOf("30") }
    var editingOpen by remember { mutableStateOf(false) }
    var editingClose by remember { mutableStateOf(false) }

    LaunchedEffect(reload) {
        loading = true
        error = null
        when (val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchAdminStoreHours(token) }) {
            is MarketplaceApi.Result.Ok -> {
                openTime = result.value.openTime
                closeTime = result.value.closeTime
                openDays = result.value.openDays
                slotMinutes = result.value.slotMinutes.toString()
                loaded = true
            }
            is MarketplaceApi.Result.Failure -> error = result.message
        }
        loading = false
    }

    fun save() {
        val minutes = slotMinutes.toIntOrNull()
        error = when {
            !openTime.isBefore(closeTime) -> "Closing time must be after opening time."
            openDays.isEmpty() -> "Choose at least one open day."
            minutes == null || minutes !in 5..240 -> "Slot length must be from 5 to 240 minutes."
            else -> null
        }
        if (error != null) return
        saving = true
        success = false
        scope.launch {
            try {
                when (val result = withContext(Dispatchers.IO) {
                    MarketplaceApi.saveStoreHours(token, openTime.toString(), closeTime.toString(), openDays, minutes!!)
                }) {
                    is MarketplaceApi.Result.Ok -> {
                        openTime = result.value.openTime
                        closeTime = result.value.closeTime
                        openDays = result.value.openDays
                        slotMinutes = result.value.slotMinutes.toString()
                        success = true
                    }
                    is MarketplaceApi.Result.Failure -> error = result.message
                }
            } finally { saving = false }
        }
    }

    if (editingOpen) StoreTimePicker("Opening time", openTime, { editingOpen = false }) { openTime = it; success = false }
    if (editingClose) StoreTimePicker("Closing time", closeTime, { editingClose = false }) { closeTime = it; success = false }

    Dialog(
        onDismissRequest = { if (!saving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column {
                MarketPageTopBar(title = "Store hours & booking slots", onBack = { if (!saving) onDismiss() })

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = Spacing.screen, vertical = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xl),
                ) {
                    InfoBanner(
                        text = "Shown to student sellers and used to validate every meet-up booking. " +
                            "The server's defaults apply only until you save here.",
                        icon = Icons.Outlined.Info,
                    )

                    when {
                        loading -> LoadingState(
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            message = "Loading store hours…",
                        )
                        !loaded -> ErrorState(
                            title = "Store hours unavailable",
                            message = error ?: "Could not load the store hours.",
                            onRetry = { reload++ },
                            modifier = Modifier.fillMaxWidth().height(280.dp),
                        )
                        else -> {
                            if (success) {
                                InfoBanner(
                                    text = "Store hours saved.",
                                    tone = StatusTone.Success,
                                    icon = Icons.Filled.CheckCircle,
                                )
                            }
                            error?.let {
                                InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                            }

                            SettingsGroup(title = "Opening times") {
                                SettingsRow(
                                    icon = Icons.Outlined.WbSunny,
                                    title = "Opening time",
                                    subtitle = openTime.format(ADMIN_TIME_FORMAT),
                                    onClick = if (saving) null else ({ editingOpen = true }),
                                )
                                RowDivider()
                                SettingsRow(
                                    icon = Icons.Outlined.NightsStay,
                                    title = "Closing time",
                                    subtitle = closeTime.format(ADMIN_TIME_FORMAT),
                                    onClick = if (saving) null else ({ editingClose = true }),
                                )
                            }

                            SettingsGroup(title = "Open days") {
                                FlowRow(
                                    modifier = Modifier.padding(Spacing.lg),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                                ) {
                                    (1..7).forEach { day ->
                                        ChoiceChip(
                                            label = DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                            selected = day in openDays,
                                            onClick = {
                                                if (!saving) {
                                                    openDays = if (day in openDays) openDays - day else openDays + day
                                                    success = false
                                                }
                                            },
                                        )
                                    }
                                }
                            }

                            SettingsGroup(title = "Booking slots") {
                                Column(modifier = Modifier.padding(Spacing.lg)) {
                                    MarketTextField(
                                        value = slotMinutes,
                                        onValueChange = { slotMinutes = it.filter(Char::isDigit); success = false },
                                        label = "Slot length (minutes)",
                                        supportingText = "5 to 240 minutes. A booking must finish before closing time.",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        enabled = !saving,
                                    )
                                }
                            }

                            PrimaryButton(
                                text = "Save store hours",
                                loading = saving,
                                onClick = { save() },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.sm))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreTimePicker(title: String, initial: LocalTime, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit) {
    val state = rememberTimePickerState(initial.hour, initial.minute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = state) },
        confirmButton = { TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)); onDismiss() }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
