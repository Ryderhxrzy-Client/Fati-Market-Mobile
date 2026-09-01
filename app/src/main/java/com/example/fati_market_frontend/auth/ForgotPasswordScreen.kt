package com.fati_market.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.fati_market.ui.theme.DarkGreen
import com.fati_market.ui.theme.DarkGreenLight
import com.fati_market.ui.theme.Gold

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }

    // The reset happens in two halves on this one screen: ask for a code, then
    // spend it. Sending them out to a browser and back for a link is a worse
    // trip than typing six digits.
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    var codeSent by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var working by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val headerGradient = Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Edge-to-edge is on, so the screen insets itself: the last
            // button must clear the gesture bar, and the form must rise
            // above the keyboard.
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ──────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(headerGradient)
        ) {
            // Back button top-left
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 8.dp, start = 4.dp)
                    .statusBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Header content centered
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.LockReset,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Forgot Password?",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "We'll send you a reset link",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp
                )
            }
        }

        // ── Form Card ────────────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-24).dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reset your password",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter your student email address and we'll send you instructions to reset your password.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    placeholder = { Text("yourname@student.fatima.edu.ph") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null,
                            tint = DarkGreen
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp)
                )

                // ── The code, and the new password it buys ────────────────────
                if (codeSent) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.filter { c -> c.isDigit() }.take(6); error = null },
                        label = { Text("6-digit code") },
                        placeholder = { Text("From your email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; error = null },
                        label = { Text("New password") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                notice?.let {
                    Text(
                        text = it,
                        color = DarkGreen,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                }

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                }

                Button(
                    enabled = !working,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text(
                        text = when {
                            working -> "Please wait..."
                            codeSent -> "Change my password"
                            else -> "Email me a code"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (codeSent) {
                    TextButton(
                        onClick = { codeSent = false; code = ""; notice = null; error = null },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Use a different email", color = DarkGreen) }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Back to login link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Remember your password? ",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ClickableText(
                        text = AnnotatedString("Login"),
                        onClick = { navController.navigate("login") },
                        style = TextStyle(
                            color = DarkGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
