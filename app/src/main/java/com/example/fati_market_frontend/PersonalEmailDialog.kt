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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.fati_market.auth.confirmPersonalEmail
import com.fati_market.auth.requestPersonalEmail
import com.fati_market.auth.verifyPersonalEmailCode
import com.fati_market.auth.personalEmailStatus
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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

    val pendingEmail = remember { prefs.getString("personal_email_pending", "").orEmpty() }
    var address by remember { mutableStateOf(existing?.takeIf { it.isNotBlank() } ?: pendingEmail) }
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(pendingEmail.isNotBlank() && existing.isNullOrBlank()) }
    var codeVerified by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var showPasswordConfirmation by remember { mutableStateOf(false) }
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
                    if (!codeVerified) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.filter { c -> c.isDigit() }.take(6); codeVerified = false; error = null },
                        label = { Text("6-digit code") },
                        placeholder = { Text("Sent to that address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = {
                            codeSent = false
                            codeVerified = false
                            code = ""
                            notice = null
                            error = null
                        }, enabled = !working) { Text("Edit email") }
                        TextButton(onClick = {
                            scope.launch {
                                working = true
                                error = null
                                val result = withContext(Dispatchers.IO) { requestPersonalEmail(token, address.trim()) }
                                working = false
                                if (result.success) {
                                    prefs.edit().putString("personal_email_pending", address.trim()).apply()
                                    notice = result.message
                                } else error = result.message
                            }
                        }, enabled = !working && address.isNotBlank()) { Text("Resend OTP") }
                    }

                    TextButton(
                        onClick = {
                            if (code.length != 6) error = "Enter the complete 6-digit code first."
                            else scope.launch {
                                working = true
                                error = null
                                val result = withContext(Dispatchers.IO) { verifyPersonalEmailCode(token, address.trim(), code) }
                                working = false
                                if (result.success) {
                                    codeVerified = true
                                    notice = result.message.ifBlank { "Code verified successfully." }
                                    // OTP verification is its own step. The
                                    // backend has already persisted and
                                    // verified the email; password setup is a
                                    // separate account action.
                                    prefs.edit().putString("personal_email", address.trim()).remove("personal_email_pending").apply()
                                    scope.launch {
                                        delay(1200)
                                        onLinked(address.trim())
                                    }
                                } else {
                                    codeVerified = false
                                    error = result.message.ifBlank { "That OTP is incorrect or expired." }
                                }
                            }
                        },
                        enabled = !working,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(if (codeVerified) "Code verified" else "Verify code") }
                    }

                    Text(
                        if (codeVerified) "OTP verified. Set a password for signing in with this personal email."
                        else "Set a password for signing in with this personal email.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, "Show password")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                    )

                    OutlinedTextField(
                        value = passwordConfirmation,
                        onValueChange = { passwordConfirmation = it; error = null },
                        label = { Text("Confirm password") },
                        singleLine = true,
                        visualTransformation = if (showPasswordConfirmation) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPasswordConfirmation = !showPasswordConfirmation }) {
                                Icon(if (showPasswordConfirmation) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, "Show password")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                shape = MaterialTheme.shapes.small,
                enabled = !working && if (codeSent) {
                    codeVerified && password.length >= 8 && password == passwordConfirmation
                } else {
                    address.isNotBlank()
                },
                onClick = {
                    scope.launch {
                        working = true
                        error = null

                        val result = withContext(Dispatchers.IO) {
                            if (codeSent) {
                                confirmPersonalEmail(token, address.trim(), code, password)
                            } else {
                                requestPersonalEmail(token, address.trim())
                            }
                        }

                        working = false

                        when {
                            !result.success -> error = result.message

                            codeSent -> {
                                // Remembered so the dashboard stops asking.
                                prefs.edit().putString("personal_email", address.trim()).remove("personal_email_pending").apply()
                                onLinked(address.trim())
                            }

                            else -> {
                                codeSent = true
                                prefs.edit().putString("personal_email_pending", address.trim()).apply()
                                notice = result.message
                            }
                        }
                    }
                },
            ) {
                Text(
                    when {
                        working -> "Please wait..."
                        codeSent -> "Save password"
                        else -> "Save email & send verification code"
                    }
                )
            }
        },
        dismissButton = {
                TextButton(onClick = onDismiss, enabled = !working) {
                Text(if (codeSent) "Close" else "Not now")
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

    LaunchedEffect(isStudent) {
        if (!isStudent) return@LaunchedEffect
        val result = withContext(Dispatchers.IO) { personalEmailStatus(prefs.getString("auth_token", "").orEmpty()) }
        if (result.success) {
            runCatching { org.json.JSONObject(result.body.orEmpty()).optString("personal_email") }
                .getOrNull()?.takeIf { it.isNotBlank() }?.let { linked = it }
        }
    }

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
