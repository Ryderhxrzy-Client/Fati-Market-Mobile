package com.fati_market.auth

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * What to tell the person when signing in fails.
 *
 * The raw exception text - "No credentials available", "timeout", "Unable to
 * resolve host" - says what broke inside a library, not what the student can
 * do about it. Every failure the auth screens can hit is translated here, once,
 * so both screens say the same thing and a weak Wi-Fi connection is reported
 * as a weak connection rather than as a missing Google account.
 *
 * Pure functions, so they are unit-tested without a device.
 */

/** A Google sign-in that failed for a reason already put into words. */
class GoogleSignInFailure(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Shown when the phone has no network at all. */
const val OFFLINE_MESSAGE =
    "You're offline. Connect to Wi-Fi or mobile data and try again."

/** Shown when Google's account sheet could not be produced. */
const val NO_GOOGLE_ACCOUNT_MESSAGE =
    "Google could not show your accounts. Make sure a Google account is added to this phone " +
        "(Settings > Passwords & accounts), that Google Play services is up to date, and that your " +
        "internet connection is working, then try again."

/**
 * The no-account message, with what a developer needs to fix it: Google only
 * offers accounts to a build whose signing certificate is registered in the
 * project, so the build's own SHA-1 is the one fact that settles "it worked
 * before" - the key changed, the registration did not.
 */
fun noGoogleAccountMessage(signingSha1: String?): String =
    if (signingSha1.isNullOrBlank()) NO_GOOGLE_ACCOUNT_MESSAGE
    else NO_GOOGLE_ACCOUNT_MESSAGE + "\n\nFor the developer: this build is signed with SHA-1 $signingSha1. " +
        "Google sign-in only works when that fingerprint is registered for com.fati_market in the " +
        "Fati-Market Google Cloud project (Firebase > Project settings > Add fingerprint), " +
        "and google-services.json is downloaded again afterwards."

/** The reason a Google sign-in or sign-up did not complete. */
fun describeGoogleSignInFailure(e: Throwable): String = when (e) {
    is GoogleSignInFailure -> e.message ?: NO_GOOGLE_ACCOUNT_MESSAGE

    is GetCredentialCancellationException -> "Google sign-in was cancelled."

    // Play services answers "no credentials" both when the phone has no Google
    // account and when it could not reach Google to list the accounts - which
    // is what a weak connection looks like.
    is NoCredentialException -> NO_GOOGLE_ACCOUNT_MESSAGE

    is GetCredentialInterruptedException ->
        "Google sign-in was interrupted before it finished. Please try again."

    is GetCredentialProviderConfigurationException ->
        "Google Play services is missing or out of date on this phone. Update it from the Play Store and try again."

    is GetCredentialUnsupportedException ->
        "This phone does not support Google sign-in. Sign in with your email and password instead."

    is GetCredentialException ->
        "Google could not complete the sign-in (${e.type.substringAfterLast('.')}: ${e.message ?: "no details"}). Please try again."

    is GoogleIdTokenParsingException ->
        "Google returned a sign-in token this app could not read. Please try again."

    is IOException -> describeNetworkFailure(e)

    else -> "Google sign-in failed: ${e.message ?: e::class.java.simpleName}"
}

/** The reason a request to the Fati Market server did not get an answer. */
fun describeNetworkFailure(e: IOException): String = when (e) {
    is UnknownHostException ->
        "Could not reach the Fati Market server. Check your internet connection and try again."

    is SocketTimeoutException ->
        "The Fati Market server took too long to respond. Your connection may be weak - please try again."

    is ConnectException ->
        "Could not connect to the Fati Market server. Check your internet connection and try again."

    is SSLException ->
        "A secure connection to the Fati Market server could not be made. Check your connection " +
            "and the phone's date and time, then try again."

    is InterruptedIOException ->
        "The request was interrupted before it finished. Please try again."

    is SocketException ->
        "The connection to the Fati Market server was lost. Please try again."

    else -> "Network error: ${e.message ?: e::class.java.simpleName}. Please try again."
}

/** The reason an email sign-in, sign-up or code request failed before the server answered. */
fun describeRequestFailure(e: Throwable): String = when (e) {
    is IOException -> describeNetworkFailure(e)
    else -> "Something went wrong: ${e.message ?: e::class.java.simpleName}. Please try again."
}
