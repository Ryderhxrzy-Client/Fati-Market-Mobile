package com.fati_market

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.fati_market.auth.confirmPersonalEmail
import com.fati_market.auth.requestPersonalEmail
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Linking the address a student keeps after they graduate.
 *
 * A school account is lent, not owned. The day it is disabled, an account keyed
 * only to it is unreachable - the points, the order history and the listings go
 * with it. This is the way back in.
 *
 * Two steps in one dialog: name the address, then type the code that arrives
 * there. The code always goes to the NEW address, which is what stops anyone
 * holding an open session from quietly pointing the recovery route at
 * themselves.
 */
@Composable
internal fun PersonalEmailDialog(
    onDismiss: () -> Unit,
    onLinked: (String) -> Unit = {},
    /** Prefilled when changing one that is already linked. */
    existing: String? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token = remember { prefs.getString("auth_token", "") ?: "" }

    var address by remember { mutableStateOf(existing.orEmpty()) }
    var code by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!working) onDismiss() },
        icon = { Icon(Icons.Filled.AlternateEmail, null) },
        title = {
            Text(if (existing == null) "Add a personal email" else "Change your personal email")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    "Your school email stops working after you graduate. Link a personal " +
                        "address now and you keep this account - your points, your orders " +
                        "and your listings - for good.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; error = null },
                    label = { Text("Personal email") },
                    placeholder = { Text("you@gmail.com") },
                    singleLine = true,
                    enabled = !codeSent,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                )

                if (codeSent) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.filter { c -> c.isDigit() }.take(6); error = null },
                        label = { Text("6-digit code") },
                        placeholder = { Text("Sent to that address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                    )
                }

                notice?.let {
                    InfoBanner(text = it, tone = StatusTone.Info, icon = Icons.Filled.MarkEmailRead)
                }

                error?.let {
                    InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !working && if (codeSent) code.length == 6 else address.isNotBlank(),
                onClick = {
                    scope.launch {
                        working = true
                        error = null

                        val result = withContext(Dispatchers.IO) {
                            if (codeSent) {
                                confirmPersonalEmail(token, address.trim(), code)
                            } else {
                                requestPersonalEmail(token, address.trim())
                            }
                        }

                        working = false

                        when {
                            !result.success -> error = result.message

                            codeSent -> {
                                // Remembered so the dashboard stops asking.
                                prefs.edit().putString("personal_email", address.trim()).apply()
                                onLinked(address.trim())
                            }

                            else -> {
                                codeSent = true
                                notice = result.message
                            }
                        }
                    }
                },
            ) {
                Text(
                    when {
                        working -> "Please wait..."
                        codeSent -> "Link it"
                        else -> "Send me a code"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !working) {
                Text(if (codeSent) "Cancel" else "Not now")
            }
        },
    )
}

/**
 * The nudge on the dashboard, shown while nothing is linked.
 *
 * Deliberately dismissible. The cost of skipping this is invisible for four
 * years and then total, so it has to be said out loud - but a student trying to
 * buy something should not be held hostage by it.
 */
@Composable
internal fun PersonalEmailPrompt(context: Context, content: @Composable () -> Unit) {
    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", 0) }

    var linked by remember { mutableStateOf(prefs.getString("personal_email", "").orEmpty()) }
    var dismissed by remember { mutableStateOf(prefs.getBoolean("personal_email_prompt_hidden", false)) }
    var asking by remember { mutableStateOf(false) }

    val isStudent = remember { (prefs.getString("user_role", "") ?: "").equals("student", true) }

    if (asking) {
        PersonalEmailDialog(
            onDismiss = {
                asking = false
                // Asked and declined: do not ask again on every launch. The
                // profile screen still carries it.
                dismissed = true
                prefs.edit().putBoolean("personal_email_prompt_hidden", true).apply()
            },
            onLinked = { linked = it; asking = false },
        )
    }

    content()

    LaunchedEffect(linked, dismissed, isStudent) {
        if (isStudent && linked.isBlank() && !dismissed) asking = true
    }
}
