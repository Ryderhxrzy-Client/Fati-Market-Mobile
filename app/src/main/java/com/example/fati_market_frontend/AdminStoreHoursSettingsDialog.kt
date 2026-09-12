package com.fati_market

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fati_market.ui.components.MarketPageTopBar
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

    if (editingOpen) StoreTimePicker("Opening time", openTime, { editingOpen = false }) { openTime = it; success = false }
    if (editingClose) StoreTimePicker("Closing time", closeTime, { editingClose = false }) { closeTime = it; success = false }

    Dialog(onDismissRequest = { if (!saving) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column {
                MarketPageTopBar(title = "Store hours & booking slots", onBack = { if (!saving) onDismiss() })
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("These hours are shown to student sellers and used to validate every meet-up booking. ENV values are only used until you save here.")
                    if (loading) CircularProgressIndicator()
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (!loading && !loaded) Button(onClick = { reload++ }) { Text("Retry") }
                    if (success) Text("Store hours saved.", color = MaterialTheme.colorScheme.primary)

                    if (loaded) {
                        TimeSettingButton("Opening time", openTime, !saving) { editingOpen = true }
                        TimeSettingButton("Closing time", closeTime, !saving) { editingClose = true }
                        Text("Open days", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..7).forEach { day ->
                                val label = DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                FilterChip(
                                    selected = day in openDays,
                                    onClick = {
                                        openDays = if (day in openDays) openDays - day else openDays + day
                                        success = false
                                    },
                                    enabled = !saving,
                                    label = { Text(label) },
                                )
                            }
                        }
                        OutlinedTextField(
                            value = slotMinutes,
                            onValueChange = { slotMinutes = it.filter(Char::isDigit); success = false },
                            label = { Text("Booking slot length (minutes)") },
                            supportingText = { Text("5 to 240 minutes. A booking must finish before closing time.") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = !saving,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            enabled = !saving,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val minutes = slotMinutes.toIntOrNull()
                                error = when {
                                    !openTime.isBefore(closeTime) -> "Closing time must be after opening time."
                                    openDays.isEmpty() -> "Choose at least one open day."
                                    minutes == null || minutes !in 5..240 -> "Slot length must be from 5 to 240 minutes."
                                    else -> null
                                }
                                if (error == null) {
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
                            },
                        ) { Text(if (saving) "Saving..." else "Save store hours") }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSettingButton(label: String, value: LocalTime, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value.format(ADMIN_TIME_FORMAT), style = MaterialTheme.typography.titleMedium)
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
