package com.fati_market

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fati_market.auth.setPersonalEmailPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun PersonalEmailPasswordDialog(
    onDismiss: () -> Unit,
    onChangeEmail: () -> Unit = {},
    passwordAlreadySet: Boolean = false,
    onSaved: () -> Unit = {},
) {
    val context = LocalContext.current
    val token = remember { context.getSharedPreferences("fatimarket_prefs", 0).getString("auth_token", "").orEmpty() }
    val scope = rememberCoroutineScope()
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = { if (!saving) onDismiss() }) {
        AlertDialog(
            onDismissRequest = { if (!saving) onDismiss() },
            icon = { Icon(Icons.Filled.Lock, null) },
            title = { Text(if (passwordAlreadySet) "Personal email settings" else "Set personal-email password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (passwordAlreadySet) {
                        Text("Your personal email is verified. You can change your password or email below.")
                        Text("Change password", style = MaterialTheme.typography.titleSmall)
                    } else Text("Use this password when signing in with your verified personal email.")
                    OutlinedTextField(password, { password = it; error = null }, label = { Text("Password") }, singleLine = true,
                        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = { IconButton({ visible = !visible }) { Icon(if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, "Show password") } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                    OutlinedTextField(confirmation, { confirmation = it; error = null }, label = { Text("Confirm password") }, singleLine = true,
                        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                    PasswordRequirement("At least 8 characters", password.length >= 8)
                    PasswordRequirement("One uppercase letter", password.any { it.isUpperCase() })
                    PasswordRequirement("One lowercase letter", password.any { it.isLowerCase() })
                    PasswordRequirement("One number", password.any { it.isDigit() })
                    PasswordRequirement("One special character (@\$!%*?&)", password.any { it in "@\$!%*?&" })
                    PasswordRequirement("Passwords match", password.isNotEmpty() && password == confirmation)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = { Button(enabled = !saving && password.length >= 8 && password == confirmation, onClick = {
                scope.launch {
                    saving = true
                    val result = withContext(Dispatchers.IO) { setPersonalEmailPassword(token, password) }
                    saving = false
                    if (result.success) {
                        context.getSharedPreferences("fatimarket_prefs", 0).edit()
                            .putBoolean("personal_email_password_set", true).apply()
                        onSaved()
                        onDismiss()
                    } else error = result.message
                }
            }) { Text(if (saving) "Saving..." else "Save password") } },
            dismissButton = { Row { TextButton(onClick = onChangeEmail, enabled = !saving) { Text("Change email") }; TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") } } },
        )
    }
}

@Composable
private fun PasswordRequirement(label: String, met: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            imageVector = if (met) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (met) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = if (met) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
