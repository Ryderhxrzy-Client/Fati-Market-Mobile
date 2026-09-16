package com.fati_market.auth

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CompletableDeferred
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.io.IOException
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * "Continue with Google", from the button to the session.
 *
 * Google replaces the password and nothing else. It cannot say whether someone
 * is a student of this school, so registering still collects the verification
 * document first and the account still waits for an admin - the server enforces
 * both, and refuses any address outside the school domain however it signed in.
 */

/**
 * The OAuth **web** client ID from the Google Cloud project that owns this app.
 *
 * Not the Android client's ID: the Android client is what proves the app's
 * signature to Google, while the token itself is minted for the web client, and
 * the server checks the token's `aud` against this exact string. The two must
 * match or every sign-in is refused as "issued for another app".
 *
 * Paste it here and in the backend's GOOGLE_CLIENT_ID; the button stays
 * disabled while it is blank rather than failing at the tap.
 */
const val GOOGLE_WEB_CLIENT_ID = "314372663644-hftntsft9bf2kfnft6dflc77gt4pefro.apps.googleusercontent.com"

/** Whether the build has been given a client ID to sign in with. */
val googleSignInConfigured: Boolean get() = GOOGLE_WEB_CLIENT_ID.isNotBlank()

private const val TAG = "FatiGoogleAuth"

private const val API = "https://fati-api.alertaraqc.com/api"

private val googleHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

/**
 * Ask the device for a Google ID token.
 *
 * `filterByAuthorizedAccounts = false` so a student who has never used this app
 * still sees their accounts - filtering to previously authorised ones shows an
 * empty sheet on a first run, which reads as "Google is broken".
 *
 * Returns null when the person backs out of the sheet - that is a choice, not
 * a failure. Throws [GoogleSignInFailure], already worded for the screen, for
 * everything else: the phone being offline, Play services unable to list any
 * account, an unreadable token.
 */
suspend fun requestGoogleIdToken(context: Context): String? {
    if (!isOnline(context)) throw GoogleSignInFailure(OFFLINE_MESSAGE)

    val option = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(GOOGLE_WEB_CLIENT_ID)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(option)
        .build()

    // The "Sign in with Google" chooser: the flow a tapped button is meant to
    // open, and the one that still works when the bottom sheet above has no
    // account to suggest - which Play services reports as "no credentials".
    val chooser = GetCredentialRequest.Builder()
        .addCredentialOption(GetSignInWithGoogleOption.Builder(GOOGLE_WEB_CLIENT_ID).build())
        .build()

    val manager = CredentialManager.create(context)

    for ((attempt, current) in listOf(request, chooser).withIndex()) {
        try {
            val response = manager.getCredential(context, current)

            return try {
                GoogleIdTokenCredential.createFrom(response.credential.data).idToken
            } catch (e: GoogleIdTokenParsingException) {
                throw GoogleSignInFailure(describeGoogleSignInFailure(e), e)
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "attempt $attempt cancelled: ${e.type} / ${e.message}", e)

            // Play services reports several real failures as a cancellation,
            // with the status code in the message. "[16] Account reauth
            // failed." is the one that matters: the phone's stored credential
            // has gone stale, and Credential Manager has no way to run the
            // recovery Play services is offering. The legacy chooser does, so
            // this is handed on rather than swallowed.
            val failure = describeCancelledSignIn(e.message, appSigningSha1(context))
                ?: return null

            if (attempt == 0) continue
            throw GoogleSignInFailure(failure, e, retryWithLegacy = true)
        } catch (e: NoCredentialException) {
            Log.w(TAG, "attempt $attempt no-credential: ${e.type} / ${e.message}", e)
            // The sheet had nothing to offer: try the full chooser before
            // giving up. If that has nothing either, the build's signing key
            // is almost certainly not registered - say so, with the key.
            if (attempt == 0) continue
            throw GoogleSignInFailure(
                noGoogleAccountMessage(appSigningSha1(context)),
                e,
                retryWithLegacy = true,
            )
        } catch (e: GetCredentialException) {
            Log.e(TAG, "attempt $attempt failed: ${e.type} / ${e.message}", e)
            throw GoogleSignInFailure(describeGoogleSignInFailure(e), e, retryWithLegacy = true)
        }
    }

    return null
}

// ── The legacy chooser, behind Credential Manager ────────────────────────────

/**
 * "Continue with Google" as one awaitable call, with the fallback built in.
 *
 * Credential Manager stays the default: it is the API Google supports, and on
 * a healthy phone it is the whole story. It has one gap this app kept falling
 * into - when the phone's stored Google credential has gone stale, Play
 * services answers "[16] Account reauth failed" and stops, because Credential
 * Manager cannot launch the recovery that Play services is, in the same
 * breath, offering ("GetToken failed with status code and recovery intent").
 *
 * The legacy chooser runs that recovery, which is why a sign-in that dies here
 * still succeeds there. So it is tried second rather than not at all.
 *
 * Both mint the token for [GOOGLE_WEB_CLIENT_ID], so the `aud` the server
 * checks is identical and the backend needs to know nothing about any of this.
 *
 * Returns null when the person backs out of either chooser.
 */
@androidx.compose.runtime.Composable
fun rememberGoogleIdTokenRequest(): GoogleIdTokenRequest {
    val pending = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<CompletableDeferred<String?>?>(null)
    }

    // Read once, here, because the result callback has no Context of its own -
    // and a DEVELOPER_ERROR is useless without naming the key it refused.
    val context = androidx.compose.ui.platform.LocalContext.current
    val signingSha1 = androidx.compose.runtime.remember(context) { appSigningSha1(context) }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val waiting = pending.value ?: return@rememberLauncherForActivityResult
        pending.value = null

        try {
            waiting.complete(legacyIdTokenFrom(result.data, signingSha1))
        } catch (e: Throwable) {
            waiting.completeExceptionally(e)
        }
    }

    return androidx.compose.runtime.remember(launcher) {
        GoogleIdTokenRequest(pending) { launcher.launch(it) }
    }
}

/**
 * The two choosers behind one `invoke`, so a screen awaits it like any other
 * suspending call: `requestIdToken(context)`.
 */
class GoogleIdTokenRequest internal constructor(
    private val pending: androidx.compose.runtime.MutableState<CompletableDeferred<String?>?>,
    private val launch: (Intent) -> Unit,
) {
    suspend operator fun invoke(context: Context): String? =
        try {
            requestGoogleIdToken(context)
        } catch (primary: GoogleSignInFailure) {
            if (!primary.retryWithLegacy) throw primary

            Log.w(TAG, "credential manager failed, falling back to the legacy chooser", primary)
            awaitLegacyChooser(context, primary)
        }

    private suspend fun awaitLegacyChooser(context: Context, primary: GoogleSignInFailure): String? {
        val deferred = CompletableDeferred<String?>()
        pending.value = deferred

        try {
            startLegacySignIn(context, launch)
        } catch (e: Throwable) {
            pending.value = null
            // The fallback could not even start, so the first failure is the
            // one worth showing - this one is an implementation detail.
            Log.e(TAG, "legacy chooser could not be started", e)
            throw primary
        }

        return deferred.await()
    }
}

/**
 * The legacy client, asked for an ID token minted for the same web client.
 *
 * `requestEmail` costs nothing and makes the chooser show addresses, which is
 * what a student is picking between.
 */
private fun legacyGoogleSignInClient(context: Context): GoogleSignInClient =
    GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build(),
    )

/**
 * Open the legacy chooser.
 *
 * Signed out first, deliberately: the client otherwise reuses the last account
 * silently, and the cached account is exactly the one that just failed. The
 * launch is chained onto that so the two cannot race.
 */
private fun startLegacySignIn(context: Context, launch: (Intent) -> Unit) {
    val client = legacyGoogleSignInClient(context)

    client.signOut().addOnCompleteListener {
        launch(client.signInIntent)
    }
}

/**
 * The ID token out of what the chooser handed back.
 *
 * Null means the person closed it. Anything else that went wrong is raised
 * already worded, the same as the Credential Manager path.
 */
private fun legacyIdTokenFrom(data: Intent?, signingSha1: String?): String? {
    if (data == null) return null

    return try {
        val account = GoogleSignIn.getSignedInAccountFromIntent(data)
            .getResult(ApiException::class.java)

        account.idToken ?: throw GoogleSignInFailure(
            "Google signed in but returned no token this app could use. Please try again.",
        )
    } catch (e: ApiException) {
        Log.w(TAG, "legacy chooser failed: ${e.statusCode} / ${e.message}", e)

        if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) null
        else throw GoogleSignInFailure(describeLegacyStatus(e.statusCode, signingSha1), e)
    }
}

/**
 * The SHA-1 of the certificate this APK is signed with - the fingerprint the
 * Google Cloud project must list for Google sign-in to offer any account.
 */
fun appSigningSha1(context: Context): String? = runCatching {
    val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
    val signature = info.signingInfo?.apkContentsSigners?.firstOrNull() ?: return null
    val digest = java.security.MessageDigest.getInstance("SHA-1").digest(signature.toByteArray())
    digest.joinToString(":") { "%02X".format(it) }
}.getOrNull()

/** Whether the phone currently has a validated route to the internet. */
private fun isOnline(context: Context): Boolean {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
    val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

/**
 * Sign in with a verified Google identity.
 *
 * @return Triple(success, message, body) - and on a 404 the message is the
 *   server's invitation to register, which the screen turns into a nudge
 *   towards sign-up rather than an error.
 */
fun googleLogin(idToken: String): GoogleAuthResult {
    val body = JSONObject().put("id_token", idToken)
        .toString()
        .toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url("$API/auth/google")
        .header("Accept", "application/json")
        .post(body)
        .build()

    return call(request)
}

/**
 * Register with Google.
 *
 * Nothing to upload and nobody to wait for: Google has already proven a school
 * address, which is the whole of what registration establishes. The backend
 * stores the name and profile picture asserted by Google with the ID token.
 */
fun googleRegister(idToken: String): GoogleAuthResult {
    val body = JSONObject().put("id_token", idToken)
        .toString()
        .toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url("$API/auth/google/register")
        .header("Accept", "application/json")
        .post(body)
        .build()

    return call(request)
}

// ── Forgotten passwords ──────────────────────────────────────────────────────

/**
 * Spend the six-digit code and open the account.
 *
 * This replaced the student ID upload. On success the body carries a session,
 * so a student goes straight from the code into the app rather than back to a
 * login screen - and there is no admin queue between the two any more.
 */
fun verifyEmailCode(email: String, code: String): GoogleAuthResult {
    val body = JSONObject()
        .put("email", email)
        .put("code", code)
        .toString()
        .toRequestBody("application/json".toMediaType())

    return call(
        Request.Builder()
            .url("$API/auth/verify-email")
            .header("Accept", "application/json")
            .post(body)
            .build()
    )
}

/** Ask for another verification code. */
fun resendVerificationCode(email: String): GoogleAuthResult {
    val body = JSONObject().put("email", email)
        .toString()
        .toRequestBody("application/json".toMediaType())

    return call(
        Request.Builder()
            .url("$API/auth/resend-code")
            .header("Accept", "application/json")
            .post(body)
            .build()
    )
}

/** Ask for a six-digit code by email. The reply never says whether it exists. */
fun requestPasswordResetCode(email: String): GoogleAuthResult {
    val body = JSONObject().put("email", email)
        .toString()
        .toRequestBody("application/json".toMediaType())

    return call(
        Request.Builder()
            .url("$API/auth/forgot-password")
            .header("Accept", "application/json")
            .post(body)
            .build()
    )
}

/** Spend the code on a new password. */
fun resetPasswordWithCode(email: String, code: String, password: String): GoogleAuthResult {
    val body = JSONObject()
        .put("email", email)
        .put("code", code)
        .put("password", password)
        .put("password_confirmation", password)
        .toString()
        .toRequestBody("application/json".toMediaType())

    return call(
        Request.Builder()
            .url("$API/auth/reset-password")
            .header("Accept", "application/json")
            .post(body)
            .build()
    )
}

/** What every call here hands back: the outcome, the server's words, the body. */
data class GoogleAuthResult(
    val success: Boolean,
    val message: String,
    val body: String?,
    val code: Int = 0,
) {
    /** The server's way of saying "this person has no account yet". */
    val needsRegistration: Boolean get() = code == 404
}

private fun call(request: Request): GoogleAuthResult =
    try {
        googleHttpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            val json = runCatching { JSONObject(raw) }.getOrNull()

            GoogleAuthResult(
                success = response.isSuccessful,
                // The server owns the wording - a wrong domain, a pending
                // approval, an expired code - so it is what gets shown.
                message = json?.optString("message")?.takeIf { it.isNotBlank() }
                    ?: if (response.isSuccessful) "Done" else "Request failed (${response.code})",
                body = raw.takeIf { it.isNotBlank() },
                code = response.code,
            )
        }
    } catch (e: IOException) {
        // Weak Wi-Fi is the common case here: say so, not "timeout".
        GoogleAuthResult(false, describeNetworkFailure(e), null)
    } catch (e: Exception) {
        GoogleAuthResult(false, describeRequestFailure(e), null)
    }

/**
 * Write a successful sign-in into the preferences every screen reads.
 *
 * Shared with the email login rather than copied: the session is a dozen keys,
 * and a Google sign-in that stored eleven of them would fail somewhere far from
 * here, in a screen that just found a blank where a name should be.
 */
fun persistSession(context: Context, responseBody: String) {
    val editor = context.getSharedPreferences("fatimarket_prefs", 0).edit()
    val loginAt = System.currentTimeMillis()

    editor.putLong("login_timestamp", loginAt)

    val root = runCatching { JSONObject(responseBody) }.getOrNull() ?: return
    val data = root.optJSONObject("data") ?: return

    data.optString("token").takeIf { it.isNotBlank() }?.let { editor.putString("auth_token", it) }

    val expiresIn = data.optLong("expires_in", 0L)

    if (expiresIn > 0L) {
        editor.putLong("session_expires_at", loginAt + expiresIn * 1000L)
    } else {
        editor.remove("session_expires_at")
    }

    editor.putInt("user_id", data.optInt("user_id", 0))
    editor.putString("user_email", data.optString("email", ""))
    editor.putString("user_first_name", data.optString("first_name", ""))
    editor.putString("user_last_name", data.optString("last_name", ""))
    editor.putString(
        "user_profile_picture",
        if (data.isNull("profile_picture")) "" else data.optString("profile_picture", ""),
    )
    // Null while nothing is linked, which is what the dashboard prompt reads.
    editor.putString(
        "personal_email",
        if (data.isNull("personal_email")) "" else data.optString("personal_email", ""),
    )
    editor.putString("user_role", data.optString("role", "student"))
    editor.putInt("user_wallet_points", data.optInt("wallet_points", 0))

    editor.apply()
}

// ── The button itself ────────────────────────────────────────────────────────

/**
 * "Continue with Google", drawn the same on every screen that offers it.
 *
 * White with a border rather than branded colour: it sits next to the store's
 * green primary button, and two filled buttons of equal weight would leave the
 * student guessing which one is the way in.
 */
@androidx.compose.runtime.Composable
fun GoogleButton(
    text: String,
    onClick: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    enabled: Boolean = true,
) {
    val scheme = androidx.compose.material3.MaterialTheme.colorScheme

    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = androidx.compose.material3.MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outline),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = scheme.surface,
            contentColor = scheme.onSurface,
        ),
    ) {
        // The four-colour "G" as a ring of arcs, so the button reads as
        // Google's without shipping a bitmap.
        androidx.compose.foundation.Canvas(
            modifier = androidx.compose.ui.Modifier.size(20.dp),
        ) {
            val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = size.width * 0.22f)
            val inset = size.width * 0.11f
            val arcSize = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(androidx.compose.ui.graphics.Color(0xFF4285F4), -45f, 90f, false, topLeft, arcSize, style = stroke)
            drawArc(androidx.compose.ui.graphics.Color(0xFF34A853), 45f, 90f, false, topLeft, arcSize, style = stroke)
            drawArc(androidx.compose.ui.graphics.Color(0xFFFBBC05), 135f, 90f, false, topLeft, arcSize, style = stroke)
            drawArc(androidx.compose.ui.graphics.Color(0xFFEA4335), 225f, 90f, false, topLeft, arcSize, style = stroke)
            drawLine(
                androidx.compose.ui.graphics.Color(0xFF4285F4),
                androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.5f),
                androidx.compose.ui.geometry.Offset(size.width - inset, size.height * 0.5f),
                strokeWidth = stroke.width,
            )
        }
        androidx.compose.foundation.layout.Spacer(
            androidx.compose.ui.Modifier.width(12.dp)
        )
        androidx.compose.material3.Text(
            text,
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
        )
    }
}

// ── The address a student keeps after graduating ─────────────────────────────

/**
 * Start linking or changing the personal email.
 *
 * Sends a code to the address being proposed - never to the one on file - so a
 * borrowed phone cannot quietly move the recovery address somewhere else.
 */
fun requestPersonalEmail(token: String, personalEmail: String): GoogleAuthResult {
    val body = JSONObject().put("personal_email", personalEmail)
        .toString()
        .toRequestBody("application/json".toMediaType())

    return call(
        Request.Builder()
            .url("$API/account/personal-email")
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()
    )
}

/** Validate the one-time code without linking the address yet. */
fun verifyPersonalEmailCode(token: String, personalEmail: String, code: String): GoogleAuthResult {
    val body = JSONObject().put("personal_email", personalEmail).put("code", code)
        .toString().toRequestBody("application/json".toMediaType())
    return call(Request.Builder().url("$API/account/personal-email/verify")
        .header("Accept", "application/json").header("Authorization", "Bearer $token")
        .post(body).build())
}

fun setPersonalEmailPassword(token: String, password: String): GoogleAuthResult {
    val body = JSONObject().put("password", password).put("password_confirmation", password)
        .toString().toRequestBody("application/json".toMediaType())
    return call(Request.Builder().url("$API/account/personal-email/password")
        .header("Accept", "application/json").header("Authorization", "Bearer $token")
        .post(body).build())
}

fun personalEmailStatus(token: String): GoogleAuthResult =
    call(Request.Builder().url("$API/account/personal-email/status")
        .header("Accept", "application/json").header("Authorization", "Bearer $token")
        .get().build())

/** Finish the link with the code that arrived at the new address. */
fun confirmPersonalEmail(
    token: String,
    personalEmail: String,
    code: String,
    password: String,
): GoogleAuthResult {
    val body = JSONObject()
        .put("personal_email", personalEmail)
        .put("code", code)
        .put("password", password)
        .put("password_confirmation", password)
        .toString()
        .toRequestBody("application/json".toMediaType())

    return call(
        Request.Builder()
            .url("$API/account/personal-email/confirm")
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()
    )
}
