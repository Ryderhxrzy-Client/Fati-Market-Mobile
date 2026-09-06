package com.fati_market.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavController
import com.fati_market.ui.components.InfoBanner
import com.fati_market.ui.components.MarketTextField
import com.fati_market.ui.components.PrimaryButton
import com.fati_market.ui.components.SecondaryButton
import com.fati_market.ui.components.StatusTone
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }

    // The reset happens in two halves on this one screen: ask for a code, then
    // spend it. Sending them out to a browser and back for a link is a worse
    // trip than typing six digits.
    val scope = rememberCoroutineScope()

    var codeSent by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var working by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    AuthScaffold(
        title = if (codeSent) "Check your email" else "Reset your password",
        subtitle = if (codeSent) {
            "Type the 6-digit code we sent, then choose a new password."
        } else {
            "Enter your student email and we'll send you a code to reset it."
        },
        onBack = { navController.navigateUp() },
        heroIcon = Icons.Filled.LockReset,
    ) {
        MarketTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = "Email address",
            placeholder = "yourname@student.fatima.edu.ph",
            leadingIcon = Icons.Filled.Email,
            enabled = !codeSent,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )

        // ── The code, and the new password it buys ────────────────────
        if (codeSent) {
            Spacer(Modifier.height(Spacing.md))

            MarketTextField(
                value = code,
                onValueChange = { code = it.filter { c -> c.isDigit() }.take(6); error = null },
                label = "6-digit code",
                placeholder = "From your email",
                leadingIcon = Icons.Filled.Pin,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            Spacer(Modifier.height(Spacing.md))

            MarketTextField(
                value = newPassword,
                onValueChange = { newPassword = it; error = null },
                label = "New password",
                leadingIcon = Icons.Filled.Lock,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
        }

        notice?.let {
            Spacer(Modifier.height(Spacing.md))
            InfoBanner(text = it, tone = StatusTone.Success, icon = Icons.Filled.CheckCircle)
        }

        error?.let {
            Spacer(Modifier.height(Spacing.md))
            InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
        }

        Spacer(Modifier.height(Spacing.xl))

        PrimaryButton(
            text = if (codeSent) "Change my password" else "Email me a code",
            loading = working,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    error = null
                    notice = null

                    if (email.isBlank()) {
                        error = "Enter your email first."
                        return@launch
                    }

                    working = true

                    val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        if (codeSent) {
                            resetPasswordWithCode(email.trim(), code, newPassword)
                        } else {
                            requestPasswordResetCode(email.trim())
                        }
                    }

                    working = false

                    when {
                        // The request step always succeeds - it must not
                        // reveal whether the address has an account.
                        result.success && !codeSent -> {
                            codeSent = true
                            notice = result.message
                        }

                        result.success -> {
                            notice = result.message
                            navController.navigate("login") {
                                popUpTo("forgot_password") { inclusive = true }
                            }
                        }

                        else -> error = result.message
                    }
                }
            },
        )

        if (codeSent) {
            Spacer(Modifier.height(Spacing.sm))
            SecondaryButton(
                text = "Use a different email",
                modifier = Modifier.fillMaxWidth(),
                onClick = { codeSent = false; code = ""; notice = null; error = null },
            )
        }

        Spacer(Modifier.height(Spacing.xl))

        AuthFooterLink(
            prompt = "Remembered it?",
            action = "Back to login",
            onClick = { navController.navigate("login") },
        )
    }
}
