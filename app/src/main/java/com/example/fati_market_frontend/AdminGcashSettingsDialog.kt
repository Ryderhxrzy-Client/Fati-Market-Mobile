package com.fati_market

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.fati_market.auth.utils.copyUriToCache
import com.fati_market.ui.components.ErrorState
import com.fati_market.ui.components.InfoBanner
import com.fati_market.ui.components.LoadingState
import com.fati_market.ui.components.MarketPageTopBar
import com.fati_market.ui.components.MarketTextField
import com.fati_market.ui.components.PrimaryButton
import com.fati_market.ui.components.SecondaryButton
import com.fati_market.ui.components.SettingsGroup
import com.fati_market.ui.components.StatusTone
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** The admin's editor for the GCash account and QR that buyers pay to. */
@Composable
internal fun AdminGcashSettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val token = remember { context.getSharedPreferences("fatimarket_prefs", 0).getString("auth_token", "").orEmpty() }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var loaded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var qrUrl by remember { mutableStateOf<String?>(null) }
    var selectedQr by remember { mutableStateOf<Uri?>(null) }
    var removeQr by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    var reload by remember { mutableIntStateOf(0) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { selectedQr = uri; removeQr = false; success = false }
    }

    LaunchedEffect(reload) {
        loading = true
        error = null
        when (val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchGcashSettings(token) }) {
            is MarketplaceApi.Result.Ok -> {
                name = result.value.accountName.orEmpty()
                number = result.value.accountNumber.orEmpty()
                qrUrl = result.value.qrImageUrl
                loaded = true
            }
            is MarketplaceApi.Result.Failure -> error = result.message
        }
        loading = false
    }

    fun save() {
        error = null
        success = false
        if (name.trim().isEmpty() || name.trim().length > 255 ||
            !Regex("^(09[0-9]{9}|\\+639[0-9]{9})$").matches(number.trim())) {
            error = "Enter an account name and a valid GCash number (09XXXXXXXXX or +639XXXXXXXXX)."
            return
        }
        saving = true
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val prepared = selectedQr?.let { copyUriToCache(context, it) }
                    if (selectedQr != null && (prepared == null || prepared.second !in listOf("image/png", "image/jpeg"))) {
                        prepared?.first?.delete()
                        MarketplaceApi.Result.Failure("Choose a readable PNG or JPG image up to 5 MB.", 422)
                    } else {
                        try {
                            MarketplaceApi.saveGcashSettings(token, name, number, prepared?.first, prepared?.second, removeQr)
                        } finally { prepared?.first?.delete() }
                    }
                }
                when (result) {
                    is MarketplaceApi.Result.Ok -> {
                        name = result.value.accountName.orEmpty()
                        number = result.value.accountNumber.orEmpty()
                        qrUrl = result.value.qrImageUrl
                        selectedQr = null; removeQr = false; success = true
                    }
                    is MarketplaceApi.Result.Failure -> error = result.message
                }
            } finally { saving = false }
        }
    }

    Dialog(
        onDismissRequest = { if (!saving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column {
                MarketPageTopBar(title = "GCash payment settings", onBack = { if (!saving) onDismiss() })

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = Spacing.screen, vertical = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xl),
                ) {
                    InfoBanner(
                        text = "The account and QR buyers see when they choose Pay with GCash.",
                        icon = Icons.Outlined.Info,
                    )

                    when {
                        loading -> LoadingState(
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            message = "Loading settings…",
                        )
                        !loaded -> ErrorState(
                            title = "Settings unavailable",
                            message = error ?: "Could not load the GCash settings.",
                            onRetry = { reload++ },
                            modifier = Modifier.fillMaxWidth().height(280.dp),
                        )
                        else -> {
                            if (success) {
                                InfoBanner(
                                    text = "GCash payment settings saved.",
                                    tone = StatusTone.Success,
                                    icon = Icons.Filled.CheckCircle,
                                )
                            }
                            error?.let {
                                InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                            }

                            SettingsGroup(title = "Account") {
                                Column(
                                    modifier = Modifier.padding(Spacing.lg),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                                ) {
                                    MarketTextField(
                                        value = name,
                                        onValueChange = { name = it; success = false },
                                        label = "GCash account name",
                                        leadingIcon = Icons.Outlined.Person,
                                        enabled = !saving,
                                    )
                                    MarketTextField(
                                        value = number,
                                        onValueChange = { number = it; success = false },
                                        label = "GCash mobile number",
                                        placeholder = "09XXXXXXXXX",
                                        leadingIcon = Icons.Outlined.Phone,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        enabled = !saving,
                                    )
                                }
                            }

                            SettingsGroup(title = "Payment QR") {
                                Column(
                                    modifier = Modifier.padding(Spacing.lg),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                                ) {
                                    val preview: Any? = selectedQr ?: qrUrl?.takeUnless { removeQr }

                                    // A QR is scanned off the screen, so the panel behind
                                    // it stays white in both themes.
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(230.dp)
                                            .clip(MaterialTheme.shapes.small)
                                            .background(Color.White)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (preview != null) {
                                            AsyncImage(
                                                model = preview,
                                                contentDescription = "GCash payment QR preview",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.fillMaxSize().padding(Spacing.md),
                                            )
                                        } else {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                                            ) {
                                                Icon(
                                                    Icons.Filled.QrCode2,
                                                    null,
                                                    tint = Color(0xFF9A9A92),
                                                    modifier = Modifier.size(54.dp),
                                                )
                                                Text(
                                                    "No QR uploaded yet",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF6B6B64),
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        SecondaryButton(
                                            text = if (preview != null) "Replace QR" else "Upload QR",
                                            icon = Icons.Filled.Upload,
                                            compact = true,
                                            enabled = !saving,
                                            onClick = { picker.launch("image/*") },
                                            modifier = Modifier.weight(1f),
                                        )
                                        if (preview != null) {
                                            TextButton(
                                                onClick = { selectedQr = null; removeQr = true; success = false },
                                                enabled = !saving,
                                            ) {
                                                Text("Remove", color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }

                                    Text(
                                        "PNG or JPG, up to 5 MB. Confirm the QR belongs to the account above.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            PrimaryButton(
                                text = "Save GCash settings",
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
