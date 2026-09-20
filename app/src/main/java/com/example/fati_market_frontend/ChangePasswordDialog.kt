package com.fati_market

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.fati_market.auth.accountPasswordStatus
import com.fati_market.auth.changeAccountPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Changing the password of the account you are signed in as.
 *
 * The app could already set a password for the recovery address, but never
 * change the one you actually sign in with, and admins had no way at all -
 * the only route was the forgotten-password email. This is the ordinary one:
 * the current password proves the phone is yours rather than found unlocked,
 * and saving signs the other devices out.
 *
 * An account that has never had a usable password - a Google sign-in - is
 * setting its first, so there is nothing to prove and no field to fill.
 */
@Composable
internal fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSaved: () -> Unit = {},
) {
    val context = LocalContext.current
    val token = remember {
        context.getSharedPreferences("fatimarket_prefs", 0).getString("auth_token", "").orEmpty()
    }
    val scope = rememberCoroutineScope()

    // Assume there is a password until the server says otherwise: asking for
    // one that is not needed is a smaller mistake than skipping the check.
    var hasPassword by remember { mutableStateOf(true) }
    var current by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val result = withContext(Dispatchers.IO) { accountPasswordStatus(token) }

        if (result.success) {
            val data = runCatching { JSONObject(result.body.orEmpty()).optJSONObject("data") }.getOrNull()
            hasPassword = data?.optBoolean("password_set", true) ?: true
        }
    }

    val longEnough = password.length >= 8
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { it in "@\$!%*?&" }
    val matches = password.isNotEmpty() && password == confirmation
    val currentGiven = !hasPassword || current.isNotBlank()
    val ready = longEnough && hasUpper && hasLower && hasDigit && hasSpecial && matches && currentGiven

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        icon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        title = { Text(if (hasPassword) "Change password" else "Set a password") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    if (hasPassword) {
                        "Your other devices are signed out when the password changes."
                    } else {
                        "This account signs in with Google. Setting a password lets you sign in with your email as well."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (hasPassword) {
                    OutlinedTextField(
                        value = current,
                        onValueChange = { current = it; error = null },
                        label = { Text("Current password") },
                        singleLine = true,
                        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = { Text("New password") },
                    singleLine = true,
                    visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { show = !show }) {
                            Icon(
                                if (show) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (show) "Hide passwords" else "Show passwords",
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it; error = null },
                    label = { Text("Repeat the new password") },
                    singleLine = true,
                    visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )

                // The same rules the server applies, so a refusal is seen
                // before the round trip rather than after it.
                PasswordRule("At least 8 characters", longEnough)
                PasswordRule("One uppercase letter", hasUpper)
                PasswordRule("One lowercase letter", hasLower)
                PasswordRule("One number", hasDigit)
                PasswordRule("One special character (@\$!%*?&)", hasSpecial)
                PasswordRule("Both new passwords match", matches)

                error?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = ready && !saving,
                onClick = {
                    scope.launch {
                        saving = true
                        error = null

                        val result = withContext(Dispatchers.IO) {
                            changeAccountPassword(token, if (hasPassword) current else "", password)
                        }

                        saving = false

                        if (result.success) {
                            onSaved()
                            onDismiss()
                        } else {
                            error = result.message
                        }
                    }
                },
            ) {
                Text(if (saving) "Saving…" else if (hasPassword) "Change password" else "Set password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") }
        },
    )
}

/** One rule, ticked off as it is met. */
@Composable
private fun PasswordRule(label: String, met: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            imageVector = if (met) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (met) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = if (met) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
