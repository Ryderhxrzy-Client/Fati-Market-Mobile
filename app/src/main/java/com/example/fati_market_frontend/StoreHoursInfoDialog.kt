package com.fati_market

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fati_market.ui.components.MarketPageTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Read-only store hours for students before they arrange a handover. */
@Composable
internal fun StoreHoursInfoDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val token = remember { context.getSharedPreferences("fatimarket_prefs", 0).getString("auth_token", "").orEmpty() }
    var loading by remember { mutableStateOf(true) }
    var hours by remember { mutableStateOf<StoreHours?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }

    LaunchedEffect(reload) {
        loading = true
        error = null
        when (val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchStoreHours(token) }) {
            is MarketplaceApi.Result.Ok -> hours = result.value
            is MarketplaceApi.Result.Failure -> error = result.message
        }
        loading = false
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column {
                MarketPageTopBar(title = "Store hours", onBack = onDismiss)
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Meet-up bookings are only available during these hours. The administrator confirms your handover schedule.")
                    if (loading) CircularProgressIndicator()
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (!loading && hours == null) Button(onClick = { reload++ }) { Text("Retry") }
                    hours?.let { store ->
                        HourInfo("Hours", store.label)
                        HourInfo("Open days", store.daysLabel())
                        HourInfo("Booking slot", "${store.slotMinutes} minutes")
                    }
                }
            }
        }
    }
}

@Composable
private fun HourInfo(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
