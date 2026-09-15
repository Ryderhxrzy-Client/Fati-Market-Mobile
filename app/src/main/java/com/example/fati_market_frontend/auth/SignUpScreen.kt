package com.fati_market.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fati_market.ui.components.InfoBanner
import com.fati_market.ui.components.PrimaryButton
import com.fati_market.ui.components.StatusTone
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // ── Success ───────────────────────────────────────────────────────────
    successMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = {
                successMessage = null
                navController.navigate("login")
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp),
                )
            },
            title = { Text("You're in!", fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = {
                PrimaryButton(
                    text = "Go to login",
                    compact = true,
                    onClick = {
                        successMessage = null
                        navController.navigate("login")
                    },
                )
            },
        )
    }

    AuthScaffold(
        title = "Create your account",
        subtitle = "Signing up takes one tap with your Fatima Google account.",
        onBack = { navController.navigateUp() },
        heroIcon = Icons.Filled.PersonAdd,
    ) {
        // ── Why it is one tap ─────────────────────────────────────────
        // The school account is what proves a student, Google has already
        // verified it, and there is no document and nobody to wait for -
        // so there is no form here to fill in and no password to invent.
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SignUpPoint(
                icon = Icons.Filled.School,
                title = "Use your school email",
                text = "Only @student.fatima.edu.ph accounts can join, so everyone here is a Fatima student.",
            )
            SignUpPoint(
                icon = Icons.Filled.Verified,
                title = "No forms, no waiting",
                text = "Your name and email come straight from Google. No password to invent and no code to type.",
            )
            SignUpPoint(
                icon = Icons.Filled.Shield,
                title = "Your account stays yours",
                text = "Add a personal email later so you keep access after graduation.",
            )
        }

        Spacer(Modifier.height(Spacing.xl))

        // ── Optional photo ────────────────────────────────────────────
        // Google supplies the name and profile picture after the account is chosen.
        errorMessage?.let { msg ->
            Spacer(Modifier.height(Spacing.md))
            InfoBanner(text = msg, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
        }

        Spacer(Modifier.height(Spacing.xl))

        // ── The whole of signing up ───────────────────────────────────
        GoogleButton(
            text = "Sign up with Google",
            enabled = !isLoading && googleSignInConfigured,
            onClick = {
                scope.launch {
                    errorMessage = null
                    isLoading = true

                    try {
                        val idToken = requestGoogleIdToken(context)

                        if (idToken == null) {
                            isLoading = false
                            return@launch
                        }

                        val result = withContext(Dispatchers.IO) {
                            googleRegister(idToken)
                        }

                        if (result.success && result.body != null) {
                            // Google verified the address, so there is
                            // no code and no queue - they are in.
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
                "Google sign-up is not set up in this build yet.",
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
