package com.fati_market.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fati_market.auth.network.loginUser
import com.fati_market.auth.network.parseToken
import com.fati_market.ui.components.BrandMark
import com.fati_market.ui.components.InfoBanner
import com.fati_market.ui.components.MarketIcon
import com.fati_market.ui.components.MarketTextField
import com.fati_market.ui.components.PrimaryButton
import com.fati_market.ui.components.StatusTone
import com.fati_market.ui.components.StoreLogoIcon
import com.fati_market.ui.theme.Elevation
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.Spacing
import com.fati_market.ui.theme.brandGradient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedRole by remember { mutableStateOf("student") } // "student" or "admin"

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    fun goToDashboard() {
        val role = context.getSharedPreferences("fatimarket_prefs", 0)
            .getString("user_role", "admin") ?: "admin"
        val destination = if (role == "admin") "admin_home" else "student_home"
        navController.navigate(destination) {
            popUpTo("login") { inclusive = true }
        }
    }

    fun submit() {
        focusManager.clearFocus()
        scope.launch {
            if (email.isBlank()) { errorMessage = "Enter your email address."; return@launch }
            if (password.isBlank()) { errorMessage = "Enter your password."; return@launch }
            errorMessage = null
            isLoading = true
            try {
                val (success, message, responseBody) = withContext(Dispatchers.IO) {
                    loginUser(email.trim(), password)
                }
                if (success) {
                    // Check if account is blocked
                    val bodyLower = responseBody?.lowercase() ?: ""
                    if (bodyLower.contains("block")) {
                        errorMessage = "Your account has been blocked. Please contact the administrator."
                        isLoading = false
                        return@launch
                    }
                    val editor = context.getSharedPreferences("fatimarket_prefs", 0).edit()
                    val loginAt = System.currentTimeMillis()
                    editor.putLong("login_timestamp", loginAt)
                    var roleMismatch = false
                    if (responseBody != null) {
                        parseToken(responseBody)?.let { editor.putString("auth_token", it) }
                        try {
                            val data = org.json.JSONObject(responseBody).optJSONObject("data")
                            data?.let { d ->
                                // Age the session off this device's clock so a
                                // server/phone time difference cannot sign the
                                // user out early or keep them in past the token.
                                val expiresIn = d.optLong("expires_in", 0L)
                                if (expiresIn > 0L) {
                                    editor.putLong("session_expires_at", loginAt + expiresIn * 1000L)
                                } else {
                                    editor.remove("session_expires_at")
                                }

                                val userRole = d.optString("role", "").lowercase()

                                // VALIDATE ROLE
                                if (userRole != selectedRole) {
                                    errorMessage = if (selectedRole == "admin") {
                                        "This account is not a store administrator. Switch to Student to log in."
                                    } else {
                                        "This is an administrator account. Switch to Store admin to log in."
                                    }
                                    roleMismatch = true
                                    return@let
                                }

                                editor.putInt("user_id", d.optInt("user_id", 0))
                                editor.putString("user_email", d.optString("email", ""))
                                editor.putString("user_first_name", d.optString("first_name", ""))
                                editor.putString("user_last_name", d.optString("last_name", ""))
                                val pic = if (d.isNull("profile_picture")) "" else d.optString("profile_picture", "")
                                editor.putString("user_profile_picture", pic)
                                editor.putString("user_role", d.optString("role", "admin"))
                                editor.putInt("user_wallet_points", d.optInt("wallet_points", 0))
                            }
                        } catch (_: Exception) {}
                    }
                    if (roleMismatch) {
                        isLoading = false
                        return@launch
                    }
                    editor.apply()
                    goToDashboard()
                } else {
                    errorMessage = if (message.contains("block", ignoreCase = true)) {
                        "Your account has been blocked. Please contact the administrator."
                    } else {
                        message
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Login failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    AuthScaffold(
        title = "Welcome back",
        subtitle = "Log in to buy, sell and swap school supplies with Ofelia's Store.",
    ) {
        // ── Who is logging in ─────────────────────────────────────────
        RoleSwitch(
            selected = selectedRole,
            onSelect = { selectedRole = it; errorMessage = null },
        )

        Spacer(Modifier.height(Spacing.xl))

        MarketTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = "Email address",
            placeholder = if (selectedRole == "student") "yourname@student.fatima.edu.ph" else null,
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xs),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                "Forgot password?",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .clickable { navController.navigate("forgot_password") }
                    .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
            )
        }

        errorMessage?.let { msg ->
            Spacer(Modifier.height(Spacing.sm))
            InfoBanner(text = msg, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
        }

        Spacer(Modifier.height(Spacing.lg))

        PrimaryButton(
            text = "Log in",
            onClick = { submit() },
            loading = isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        // ── Continue with Google ──────────────────────────────────────────
        // Only for students: the store's admin account is not a Google
        // account, and offering it there would only ever fail.
        if (selectedRole == "student") {
            Spacer(Modifier.height(Spacing.lg))
            OrDivider()
            Spacer(Modifier.height(Spacing.lg))

            GoogleButton(
                text = "Continue with Google",
                enabled = !isLoading && googleSignInConfigured,
                onClick = {
                    scope.launch {
                        errorMessage = null
                        isLoading = true

                        try {
                            val idToken = requestGoogleIdToken(context)

                            if (idToken == null) {
                                // They backed out of the sheet, which is
                                // not an error worth shouting about.
                                isLoading = false
                                return@launch
                            }

                            val result = withContext(Dispatchers.IO) { googleLogin(idToken) }

                            when {
                                result.success && result.body != null -> {
                                    persistSession(context, result.body)
                                    goToDashboard()
                                }

                                // No account yet - the document still has
                                // to be uploaded, so send them to sign up.
                                result.needsRegistration -> {
                                    errorMessage = result.message
                                    navController.navigate("signup")
                                }

                                else -> errorMessage = result.message
                            }
                        } catch (e: Exception) {
                            errorMessage = "Google sign-in failed: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )

            if (!googleSignInConfigured) {
                Text(
                    "Google sign-in is not set up in this build yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                )
            }

            Spacer(Modifier.height(Spacing.xl))

            AuthFooterLink(
                prompt = "New to Fati-Market?",
                action = "Create an account",
                onClick = { navController.navigate("signup") },
            )
        }
    }
}

// ── Shared scaffolding for the auth screens ──────────────────────────────────

/**
 * The frame every auth screen sits in: a brand hero with the mark and the
 * app name, and a card that overlaps its bottom edge carrying the form.
 */
@Composable
fun AuthScaffold(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    heroIcon: ImageVector = StoreLogoIcon,
    content: @Composable ColumnScope.() -> Unit,
) {
    val accents = LocalMarketAccents.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Edge-to-edge is on, so the screen insets itself: the last
            // button must clear the gesture bar, and the form must rise
            // above the keyboard.
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(brandGradient()),
        ) {
            // A soft disc for depth, matching the splash.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(220.dp)
                    .offset(x = 70.dp, y = (-60).dp)
                    .background(Color.White.copy(alpha = 0.06f), CircleShape),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = Spacing.sm, bottom = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = accents.onBrand,
                            )
                        }
                    }
                }

                BrandMark(size = 68.dp, icon = heroIcon)
                Spacer(Modifier.height(Spacing.md))
                Text(
                    "Fati-Market",
                    style = MaterialTheme.typography.headlineMedium,
                    color = accents.onBrand,
                )
                Text(
                    "Ofelia's Store · OLFU",
                    style = MaterialTheme.typography.labelMedium,
                    color = accents.onBrandMuted,
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl)
                .offset(y = (-32).dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = Elevation.floating,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.xxl),
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.xl))

                content()
            }
        }

        Spacer(Modifier.height(Spacing.sm))
    }
}

/** A two-way segmented control for "Student" / "Store admin". */
@Composable
fun RoleSwitch(selected: String, onSelect: (String) -> Unit) {
    Column {
        Text(
            "I am a",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Spacing.sm, start = Spacing.xxs),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            RoleOption(
                label = "Student",
                icon = Icons.Filled.School,
                selected = selected == "student",
                modifier = Modifier.weight(1f),
                onClick = { onSelect("student") },
            )
            RoleOption(
                label = "Store admin",
                icon = StoreLogoIcon,
                selected = selected == "admin",
                modifier = Modifier.weight(1f),
                onClick = { onSelect("admin") },
            )
        }
    }
}

@Composable
private fun RoleOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        label = "roleContainer",
    )
    val content = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = MaterialTheme.shapes.extraSmall,
        color = container,
        shadowElevation = if (selected) Elevation.card else Elevation.flat,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize(),
        ) {
            MarketIcon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = content,
            )
        }
    }
}

/** A hairline with "or" in the middle. */
@Composable
fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            "or",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** "Prompt? Action" - the line at the foot of an auth card. */
@Composable
fun AuthFooterLink(prompt: String, action: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            action,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable(onClick = onClick)
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        )
    }
}
