package com.fati_market.auth.network

import android.content.Context
import android.net.Uri
import com.fati_market.auth.utils.getFileName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// ── HTTP Client ────────────────────────────────────────────────────────────────

val signUpHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

// ── API Call ───────────────────────────────────────────────────────────────────

/**
 * Open an account. The address is proven afterwards, by the emailed code.
 *
 * The student ID and registration card are gone: a photograph of a card could
 * be borrowed, and it cost an admin a decision on every sign-up, while only a
 * student holds an `@student.fatima.edu.ph` address and only its holder can
 * read what is sent to it. The profile picture is all that is left, and it is
 * optional.
 */
fun registerUser(
    context: Context,
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    passwordConfirmation: String,
    profilePictureUri: Uri? = null
): Pair<Boolean, String> {
    val builder = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("first_name", firstName.trim())
        .addFormDataPart("last_name", lastName.trim())
        .addFormDataPart("email", email.trim())
        .addFormDataPart("password", password)
        .addFormDataPart("password_confirmation", passwordConfirmation)

    profilePictureUri?.let { uri ->
        context.contentResolver.openInputStream(uri)?.readBytes()?.let { bytes ->
            builder.addFormDataPart(
                "profile_picture",
                getFileName(context, uri).ifEmpty { "profile.jpg" },
                bytes.toRequestBody(
                    (context.contentResolver.getType(uri) ?: "image/jpeg").toMediaType()
                )
            )
        }
    }

    val requestBody = builder.build()

    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/register")
        .header("Accept", "application/json")
        .header("X-Requested-With", "XMLHttpRequest")
        .post(requestBody)
        .build()

    signUpHttpClient.newCall(request).execute().use { response ->
        val body = response.body?.string() ?: ""
        return if (response.isSuccessful) {
            val serverMsg = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
                ?: "Your account has been created and is pending verification."
            Pair(true, serverMsg)
        } else {
            Pair(false, "[HTTP ${response.code}] ${parseRegistrationError(body)}")
        }
    }
}

// ── Response Parsers ───────────────────────────────────────────────────────────

fun parseRegistrationError(body: String): String {
    val errorsBlock = Regex("\"errors\"\\s*:\\s*\\{(.*?)\\}", RegexOption.DOT_MATCHES_ALL).find(body)
    if (errorsBlock != null) {
        val fieldErrors = Regex("\"[^\"]+\"\\s*:\\s*\\[\\s*\"([^\"]+)\"").findAll(errorsBlock.groupValues[1])
        val messages = fieldErrors.map { it.groupValues[1] }.toList()
        if (messages.isNotEmpty()) return messages.joinToString("\n")
    }
    val msgMatch = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)
    if (msgMatch != null) return msgMatch.groupValues[1]
    val errMatch = Regex("\"error\"\\s*:\\s*\"([^\"]+)\"").find(body)
    if (errMatch != null) return errMatch.groupValues[1]
    return if (body.isNotBlank()) "Server response: $body" else "Registration failed. Please try again."
}
