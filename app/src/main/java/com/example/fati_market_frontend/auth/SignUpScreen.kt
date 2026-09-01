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
import com.fati_market.auth.network.registerUser
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
    var awaitingCode by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf<String?>(null) }

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

                // ── Profile Picture ──────────────────────────────────────────────
                Text(
                    text = "Profile Photo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Avatar with camera badge
                Box(
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .clickable { profileLauncher.launch("image/*") }
                ) {
                    val bitmap = profileBitmap
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .border(2.dp, DarkGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bitmap != null) {
                            Image(
                                painter = BitmapPainter(bitmap),
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = DarkGreen,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }

                    // Camera badge overlay
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Gold)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = "Add photo",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // ── First Name ───────────────────────────────────────────────────
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    placeholder = { Text("Juan") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // ── Last Name ────────────────────────────────────────────────────
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    placeholder = { Text("Dela Cruz") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // ── Email ────────────────────────────────────────────────────────
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    placeholder = { Text("juan.delacruz$emailSuffix") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = if (isEmailSuffixValid) "Email format is valid" else "Email must end with $emailSuffix",
                    color = if (isEmailSuffixValid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )


                // ── Password ─────────────────────────────────────────────────────
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Enter strong password") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp)
                )

                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Text(
                        text = "Password requirements:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Text(
                        text = "• At least 8 characters (min:8)",
                        color = if (hasMinLen8) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• At least one lowercase letter",
                        color = if (hasLowercase) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• At least one uppercase letter",
                        color = if (hasUppercase) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• At least one digit",
                        color = if (hasDigit) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• At least one special character (@$!%*?&)",
                        color = if (hasAllowedSpecialChar) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )

                    if (isPasswordValid) {
                        Text(
                            text = "Password is valid",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }


                // ── Confirm Password ─────────────────────────────────────────────
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },

                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),

                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp)
                )
                
                if (!isConfirmPasswordEmpty) {
                    Text(
                        text = if (isConfirmPasswordMatch) "Passwords match" else "Passwords do not match",
                        color = if (isConfirmPasswordMatch) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Section Divider ──────────────────────────────────────────────
                Divider(
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = DarkGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Student Verification",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // ── The emailed code ──────────────────────────────────────────
                // Shown in place once the account exists. It is the last step
                // and the only one: no document, no admin, no waiting.
                if (awaitingCode) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.filter { c -> c.isDigit() }.take(6); errorMessage = null },
                        label = { Text("6-digit code") },
                        placeholder = { Text("Sent to $email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        enabled = !isLoading && code.length == 6,
                        onClick = {
                            scope.launch {
                                errorMessage = null
                                isLoading = true

                                val result = withContext(Dispatchers.IO) {
                                    verifyEmailCode(email.trim(), code)
                                }

                                isLoading = false

                                if (result.success && result.body != null) {
                                    // Verified means signed in - there is
                                    // nothing left to wait for.
                                    persistSession(context, result.body)
                                    successMessage = result.message
                                } else {
                                    errorMessage = result.message
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        Text("Verify my email", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    TextButton(
                        enabled = !isLoading,
                        onClick = {
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    resendVerificationCode(email.trim())
                                }
                                notice = result.message
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Send me another code", color = DarkGreen) }

                    notice?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }
                }

                // Nothing to upload any more. Registration used to mean
                // photographing a student ID or registration card and then
                // waiting for an admin to look at it; the school email address
                // proves the same thing - only a student has one, and only its
                // holder can read the code sent to it.

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

                // ── Sign Up Button ────────────────────────────────────────────────
                Button(
                    onClick = {
                        scope.launch {
                            // Validation
                            if (firstName.isBlank()) { errorMessage = "First name is required"; return@launch }
                            if (lastName.isBlank()) { errorMessage = "Last name is required"; return@launch }
                            if (email.isEmpty()) { errorMessage = "Email is required"; return@launch }
                            if (!email.endsWith("@student.fatima.edu.ph")) {
                                errorMessage = "Email must end with @student.fatima.edu.ph"; return@launch
                            }
                            if (password.isEmpty()) { errorMessage = "Password is required"; return@launch }
                            if (password.length < 8) { errorMessage = "Password must be at least 8 characters"; return@launch }
                            if (!password.any { it.isDigit() }) { errorMessage = "Password must contain at least one number"; return@launch }
                            if (!password.any { it.isUpperCase() }) { errorMessage = "Password must contain at least one uppercase letter"; return@launch }
                            if (!password.any { it.isLowerCase() }) { errorMessage = "Password must contain at least one lowercase letter"; return@launch }
                            if (!password.any { !it.isLetterOrDigit() }) { errorMessage = "Password must contain at least one symbol (!@#\$%^&* etc.)"; return@launch }
                            if (password != confirmPassword) { errorMessage = "Passwords do not match"; return@launch }
                            errorMessage = null
                            isLoading = true

                            try {
                                val (success, message) = withContext(Dispatchers.IO) {
                                    registerUser(
                                        context = context,
                                        firstName = firstName,
                                        lastName = lastName,
                                        email = email,
                                        password = password,
                                        passwordConfirmation = confirmPassword,
                                        profilePictureUri = profileUri
                                    )
                                }
                                if (success) {
                                    // The account exists but cannot be used
                                    // yet - the code is the last step, and it
                                    // is asked for right here rather than in
                                    // an email the student has to act on.
                                    awaitingCode = true
                                } else {
                                    errorMessage = message
                                }
                            } catch (e: Exception) {
                                errorMessage = "Registration failed: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // ── Or finish with Google ─────────────────────────────────────
                // Google is offered at the END of the form, not the start: the
                // verification type and the document above are what actually
                // get an account approved, and a Google button at the top would
                // read as a way to skip them. It cannot be - the server refuses
                // a registration without a document either way.
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        "  or  ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

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
