package com.fati_market

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.fati_market.auth.utils.copyUriToCache
import com.fati_market.ui.components.MarketPageTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    Dialog(onDismissRequest = { if (!saving) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column {
                MarketPageTopBar(title = "GCash payment settings", onBack = { if (!saving) onDismiss() })
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("The account and QR buyers see when they choose Pay with GCash.")
                    if (loading) CircularProgressIndicator()
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (!loading && !loaded) Button(onClick = { reload++ }) { Text("Retry") }
                    if (success) Text("GCash payment settings saved.", color = MaterialTheme.colorScheme.primary)
                    if (loaded) {
                        OutlinedTextField(
                            value = name, onValueChange = { name = it; success = false },
                            label = { Text("GCash account name") }, singleLine = true,
                            enabled = !saving, modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = number, onValueChange = { number = it; success = false },
                            label = { Text("GCash mobile number") }, placeholder = { Text("09XXXXXXXXX") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true, enabled = !saving, modifier = Modifier.fillMaxWidth(),
                        )
                        val preview: Any? = selectedQr ?: qrUrl?.takeUnless { removeQr }
                        if (preview != null) AsyncImage(
                            model = preview, contentDescription = "GCash payment QR preview",
                            contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().height(240.dp),
                        )
                        OutlinedButton(onClick = { picker.launch("image/*") }, enabled = !saving) { Text("Upload / replace QR") }
                        if (preview != null) TextButton(onClick = {
                            selectedQr = null; removeQr = true; success = false
                        }, enabled = !saving) { Text("Remove QR") }
                        Text("PNG or JPG, up to 5 MB. Confirm the QR belongs to the account above.", style = MaterialTheme.typography.bodySmall)
                        Button(
                            enabled = !saving && !loading,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                error = null
                                success = false
                                if (name.trim().isEmpty() || name.trim().length > 255 ||
                                    !Regex("^(09[0-9]{9}|\\+639[0-9]{9})$").matches(number.trim())) {
                                    error = "Enter an account name and valid GCash number (09XXXXXXXXX or +639XXXXXXXXX)."
                                } else {
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
                            },
                        ) { Text(if (saving) "Saving..." else "Save GCash settings") }
                    }
                }
            }
        }
    }
}
