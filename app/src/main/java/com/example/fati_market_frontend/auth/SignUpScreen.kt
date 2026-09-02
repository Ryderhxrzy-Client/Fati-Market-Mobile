package com.fati_market.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.graphics.BitmapFactory
import com.fati_market.auth.utils.getFileName
import com.fati_market.ui.theme.DarkGreen
import com.fati_market.ui.theme.DarkGreenLight
import com.fati_market.ui.theme.DarkText
import com.fati_market.ui.theme.Gold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // ── Realtime validation ───────────────────────────────────────────────
    val emailSuffix = "@student.fatima.edu.ph"
    val normalizedEmail = email.trim()

    val isEmailEmpty = normalizedEmail.isEmpty()
    val isEmailSuffixValid = normalizedEmail.endsWith(emailSuffix)

    val hasMinLen8 = password.length >= 8
    val hasLowercase = password.any { it.isLowerCase() }
    val hasUppercase = password.any { it.isUpperCase() }
    val hasDigit = password.any { it.isDigit() }
    val allowedSpecialChars = "@$!%*?&"
    val hasAllowedSpecialChar = password.any { allowedSpecialChars.contains(it) }

    val isPasswordValid = hasMinLen8 && hasLowercase && hasUppercase && hasDigit && hasAllowedSpecialChar
    val isConfirmPasswordEmpty = confirmPassword.isBlank()
    val isConfirmPasswordMatch = !isConfirmPasswordEmpty && confirmPassword == password


    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Profile picture
    var profileBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var profileUri by remember { mutableStateOf<Uri?>(null) }

    // Verification
    val verificationOptions = listOf("Student ID", "Registration Card")
    // The account is not usable until the emailed code comes back, so the
    // screen stays put and asks for it rather than sending them to a login
    // they would only be turned away from.

    // Image picker for profile photo
    val profileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileUri = uri
        uri?.let {
            scope.launch(Dispatchers.IO) {
                val stream = context.contentResolver.openInputStream(it)
                val bmp = BitmapFactory.decodeStream(stream)?.asImageBitmap()
                withContext(Dispatchers.Main) { profileBitmap = bmp }
            }
        }
    }

    val headerGradient = Brush.verticalGradient(
        colors = listOf(DarkGreen, DarkGreenLight)
    )

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
        // ── Header with tab switcher ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(headerGradient),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Logo — same as login screen
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(2.dp, Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Text(
                    text = "Fati-Market",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Create Your Account",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                // ── Login / Sign Up tab switcher ──────────────────────────────────
                AuthTabSwitcher(
                    isLoginSelected = false,
                    onLoginClick = { navController.navigate("login") },
                    onSignUpClick = { /* already here */ }
                )
            }
        }

        // ── Floating Form Card ─────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-28).dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Signing up is one tap. The school account is what proves a
                // student, Google has already verified it, and there is no
                // document and nobody to wait for - so there is no form here
                // to fill in and no password to invent.
                Text(
                    text = "Use your Fatima account to join. We take your name and email from Google - nothing to fill in, nothing to upload.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )

                // ── Success Dialog ────────────────────────────────────────────────
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
                                tint = DarkGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        title = { Text("Registration Successful", fontWeight = FontWeight.Bold) },
                        text = { Text(msg) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    successMessage = null
                                    navController.navigate("login")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                            ) {
                                Text("Go to Login", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                // ── Error Message ─────────────────────────────────────────────────
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                // ── The whole of signing up ───────────────────────────────────
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
                                    isLoading = false
                                    return@launch
                                }

                                val result = withContext(Dispatchers.IO) {
                                    googleRegister(
                                        context = context,
                                        idToken = idToken,
                                        // Google already has a picture of them,
                                        // so this one is a bonus, not a hurdle.
                                        profilePictureUri = profileUri,
                                    )
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
                                errorMessage = "Google sign-up failed: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )

                Text(
                    text = if (googleSignInConfigured) {
                        "Your name and email come from Google - no password and no code needed."
                    } else {
                        "Google sign-up is not set up in this build yet."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
