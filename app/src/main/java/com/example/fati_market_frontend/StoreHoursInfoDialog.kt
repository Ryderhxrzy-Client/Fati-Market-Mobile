package com.fati_market

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fati_market.ui.components.ErrorState
import com.fati_market.ui.components.InfoBanner
import com.fati_market.ui.components.InfoRowItem
import com.fati_market.ui.components.LoadingState
import com.fati_market.ui.components.MarketPageTopBar
import com.fati_market.ui.components.RowDivider
import com.fati_market.ui.components.SettingsGroup
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HOURS_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

/**
 * Read-only store hours and location for students, shown before they
 * arrange a handover. Same groups and rows as the profile, so it reads as
 * one more page of the same app rather than a pop-up of plain text.
 */
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column {
                MarketPageTopBar(title = "Store hours", onBack = onDismiss)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = Spacing.screen, vertical = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xl),
                ) {
                    InfoBanner(
                        text = "Meet-up bookings are only available during these hours. " +
                            "The administrator confirms your handover schedule.",
                        icon = Icons.Outlined.Info,
                    )

                    val store = hours
                    when {
                        loading -> LoadingState(
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            message = "Loading store hours…",
                        )
                        store == null -> ErrorState(
                            title = "Store hours unavailable",
                            message = error ?: "Could not load the store hours.",
                            onRetry = { reload++ },
                            modifier = Modifier.fillMaxWidth().height(280.dp),
                        )
                        else -> SettingsGroup(title = "Opening hours") {
                            InfoRowItem(
                                Icons.Outlined.Schedule,
                                "Hours",
                                store.label.ifBlank {
                                    "${store.openTime.format(HOURS_FORMAT)} - ${store.closeTime.format(HOURS_FORMAT)}"
                                },
                            )
                            RowDivider()
                            InfoRowItem(Icons.Outlined.CalendarMonth, "Open days", store.daysLabel())
                            RowDivider()
                            InfoRowItem(Icons.Outlined.Timer, "Booking slot", "${store.slotMinutes} minutes")
                        }
                    }

                    StoreLocationGroup(subtitle = "Meet-ups and walk-in pickups happen here")

                    Spacer(Modifier.height(Spacing.sm))
                }
            }
        }
    }
}
