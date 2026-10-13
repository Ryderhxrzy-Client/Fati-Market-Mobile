package com.fati_market.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fati_market.auth.network.registerUser
import com.fati_market.ui.components.InfoBanner
import com.fati_market.ui.components.MarketTextField
import com.fati_market.ui.components.PrimaryButton
import com.fati_market.ui.components.SecondaryButton
import com.fati_market.ui.components.StatusTone
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val requestIdToken = rememberGoogleIdTokenRequest()

    // Form fields
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // OTP state
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var registeredEmail by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    fun goToDashboard() {
        val role = context.getSharedPreferences("fatimarket_prefs", 0)
            .getString("user_role", "student") ?: "student"
        val destination = if (role == "admin") "admin_home" else "student_home"
        navController.navigate(destination) {
            popUpTo("login") { inclusive = true }
        }
    }

    // ── Success Dialog ────────────────────────────────────────────────────────
    successMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = {
                successMessage = null
                goToDashboard()
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp),
                )
            },
            title = { Text("Welcome to Fati Market!", fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = {
                PrimaryButton(
                    text = "Get started",
                    compact = true,
                    onClick = {
                        successMessage = null
                        goToDashboard()
                    },
                )
            },
        )
    }

    AuthScaffold(
        title = if (isVerifyingOtp) "Verify your email" else "Create your account",
        subtitle = if (isVerifyingOtp) {
            "Enter the 6-digit verification code sent to $registeredEmail"
        } else {
            "Open to Fatima students and public buyers. Sign up below."
        },
        onBack = {
            if (isVerifyingOtp) {
                isVerifyingOtp = false
                errorMessage = null
                infoMessage = null
            } else {
                navController.navigateUp()
            }
        },
        heroIcon = if (isVerifyingOtp) Icons.Filled.Verified else Icons.Filled.PersonAdd,
    ) {
        if (!isVerifyingOtp) {
            // ── Features Banner ──────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                SignUpPoint(
                    icon = Icons.Filled.School,
                    title = "Students & Public Buyers",
                    text = "Fatima students and public buyers are welcome! Join using your personal Gmail or school email.",
                )
                SignUpPoint(
                    icon = Icons.Filled.Verified,
                    title = "Secure OTP Verification",
                    text = "Verify your email with a 6-digit code sent to your inbox to protect your account.",
                )
                SignUpPoint(
                    icon = Icons.Filled.Shield,
                    title = "Your Account Stays Yours",
                    text = "Keep your account forever to buy, sell, or swap school items with Ofelia's store.",
                )
            }

            Spacer(Modifier.height(Spacing.xl))

            // ── Registration Form ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MarketTextField(
                        value = firstName,
                        onValueChange = { firstName = it; errorMessage = null },
                        label = "First name",
                        leadingIcon = Icons.Filled.Person,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    MarketTextField(
                        value = lastName,
                        onValueChange = { lastName = it; errorMessage = null },
                        label = "Last name",
                        leadingIcon = Icons.Filled.Person,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))

            MarketTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = "Email address",
                placeholder = "yourname@gmail.com or student email",
                leadingIcon = Icons.Filled.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            )

            Spacer(Modifier.height(Spacing.md))

            MarketTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = "Password",
                leadingIcon = Icons.Filled.Lock,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            )

            Spacer(Modifier.height(Spacing.md))

            MarketTextField(
                value = passwordConfirmation,
                onValueChange = { passwordConfirmation = it; errorMessage = null },
                label = "Confirm password",
                leadingIcon = Icons.Filled.Lock,
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )

            errorMessage?.let { msg ->
                Spacer(Modifier.height(Spacing.md))
                InfoBanner(text = msg, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
            }

            Spacer(Modifier.height(Spacing.xl))

            // ── Register Button ──────────────────────────────────────────
            PrimaryButton(
                text = "Create account",
                loading = isLoading,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    focusManager.clearFocus()
                    if (firstName.isBlank()) { errorMessage = "Please enter your first name."; return@PrimaryButton }
                    if (lastName.isBlank()) { errorMessage = "Please enter your last name."; return@PrimaryButton }
                    if (email.isBlank() || !email.contains("@")) { errorMessage = "Please enter a valid email address."; return@PrimaryButton }
                    if (password.length < 8) { errorMessage = "Password must be at least 8 characters."; return@PrimaryButton }
                    if (password != passwordConfirmation) { errorMessage = "Passwords do not match."; return@PrimaryButton }

                    scope.launch {
                        errorMessage = null
                        isLoading = true
                        val (success, message) = withContext(Dispatchers.IO) {
                            registerUser(
                                context = context,
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                email = email.trim(),
                                password = password,
                                passwordConfirmation = passwordConfirmation,
                            )
                        }
                        isLoading = false
                        if (success) {
                            registeredEmail = email.trim()
                            isVerifyingOtp = true
                            infoMessage = "A 6-digit verification code has been sent to $registeredEmail. Please enter it below."
                        } else {
                            errorMessage = message
                        }
                    }
                }
            )

            Spacer(Modifier.height(Spacing.lg))
            OrDivider()
            Spacer(Modifier.height(Spacing.lg))

            // ── Google One-Tap ───────────────────────────────────────────
            GoogleButton(
                text = "Sign up with Google",
                enabled = !isLoading && googleSignInConfigured,
                onClick = {
                    scope.launch {
                        errorMessage = null
                        isLoading = true

                        try {
                            val idToken = requestIdToken(context)

                            if (idToken == null) {
                                isLoading = false
                                return@launch
                            }

                            val result = withContext(Dispatchers.IO) {
                                googleRegister(idToken)
                            }

                            if (result.success && result.body != null) {
                                persistSession(context, result.body)
                                successMessage = result.message
                            } else {
                                errorMessage = result.message
                            }
                        } catch (e: Exception) {
                            errorMessage = describeGoogleSignInFailure(e)
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )

            if (!googleSignInConfigured) {
                Text(
                    "Google sign-up is not configured in this build.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                )
            }

            Spacer(Modifier.height(Spacing.xl))

            AuthFooterLink(
                prompt = "Already have an account?",
                action = "Log in",
                onClick = { navController.navigate("login") },
            )
        } else {
            // ── OTP Verification Flow ────────────────────────────────────
            infoMessage?.let { msg ->
                InfoBanner(text = msg, tone = StatusTone.Info, icon = Icons.Filled.CheckCircle)
                Spacer(Modifier.height(Spacing.md))
            }

            errorMessage?.let { msg ->
                InfoBanner(text = msg, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                Spacer(Modifier.height(Spacing.md))
            }

            MarketTextField(
                value = otpCode,
                onValueChange = { otpCode = it.filter { c -> c.isDigit() }.take(6); errorMessage = null },
                label = "6-digit verification code",
                placeholder = "123456",
                leadingIcon = Icons.Filled.Pin,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )

            Text(
                "Please check your inbox (and spam folder) for the 6-digit code.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs, start = Spacing.xs),
            )

            Spacer(Modifier.height(Spacing.xl))

            PrimaryButton(
                text = "Verify & open account",
                loading = isLoading,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    focusManager.clearFocus()
                    if (otpCode.length < 6) {
                        errorMessage = "Please enter the full 6-digit code."
                        return@PrimaryButton
                    }

                    scope.launch {
                        errorMessage = null
                        isLoading = true
                        val result = withContext(Dispatchers.IO) {
                            verifyEmailCode(registeredEmail, otpCode.trim())
                        }
                        isLoading = false
                        if (result.success && result.body != null) {
                            persistSession(context, result.body)
                            successMessage = result.message
                        } else {
                            errorMessage = result.message
                        }
                    }
                }
            )

            Spacer(Modifier.height(Spacing.md))

            SecondaryButton(
                text = if (isResending) "Sending code..." else "Resend code",
                enabled = !isResending && !isLoading,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        isResending = true
                        errorMessage = null
                        val result = withContext(Dispatchers.IO) {
                            resendVerificationCode(registeredEmail)
                        }
                        isResending = false
                        infoMessage = result.message
                    }
                }
            )

            Spacer(Modifier.height(Spacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                TextButton(
                    onClick = {
                        isVerifyingOtp = false
                        errorMessage = null
                        infoMessage = null
                    }
                ) {
                    Text("Change email address", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun SignUpPoint(icon: ImageVector, title: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.extraSmall),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(19.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
