package com.fati_market.auth

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.fati_market.auth.utils.getFileName
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
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
 * Returns null when the person backs out of the sheet; throws when Google
 * itself failed, so the screen can tell those two apart.
 */
suspend fun requestGoogleIdToken(context: Context): String? {
    val option = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(GOOGLE_WEB_CLIENT_ID)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(option)
        .build()

    val response = CredentialManager.create(context).getCredential(context, request)
    val credential = response.credential

    return runCatching {
        GoogleIdTokenCredential.createFrom(credential.data).idToken
    }.getOrNull()
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
 * address, which is the whole of what registration establishes. The profile
 * picture is optional - Google has one of those too.
 */
fun googleRegister(
    context: Context,
    idToken: String,
    profilePictureUri: Uri? = null,
): GoogleAuthResult {
    val builder = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("id_token", idToken)

    profilePictureUri?.let { uri ->
        context.contentResolver.openInputStream(uri)?.readBytes()?.let { bytes ->
            builder.addFormDataPart(
                "profile_picture",
                getFileName(context, uri).ifEmpty { "profile.jpg" },
                bytes.toRequestBody((context.contentResolver.getType(uri) ?: "image/jpeg").toMediaType()),
            )
        }
    }

    val request = Request.Builder()
        .url("$API/auth/google/register")
        .header("Accept", "application/json")
        .post(builder.build())
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
    } catch (e: Exception) {
        GoogleAuthResult(false, e.message ?: "Network error", null)
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
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    ) {
        androidx.compose.material3.Icon(
            androidx.compose.material.icons.Icons.Filled.AccountCircle,
            contentDescription = null,
            modifier = androidx.compose.ui.Modifier.size(20.dp),
        )
        androidx.compose.foundation.layout.Spacer(
            androidx.compose.ui.Modifier.width(10.dp)
        )
        androidx.compose.material3.Text(
            text,
            fontSize = 15.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        )
    }
}
