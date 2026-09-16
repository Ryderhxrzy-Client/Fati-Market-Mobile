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

/**
 * A Google sign-in that failed for a reason already put into words.
 *
 * [retryWithLegacy] marks the failures the older chooser is known to survive -
 * a stale account credential, chiefly - so the caller knows to try it rather
 * than guessing from the message text.
 */
class GoogleSignInFailure(
    message: String,
    cause: Throwable? = null,
    val retryWithLegacy: Boolean = false,
) : Exception(message, cause)

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

/**
 * Shown when both choosers have failed on an account Google will not confirm.
 *
 * Only reached after the legacy chooser has been tried too, so by this point
 * the phone really cannot prove the account - which is the same thing that
 * makes the Play Store start asking for a password.
 */
const val GOOGLE_REAUTH_MESSAGE =
    "Google could not confirm this account on this phone. Open Settings > Passwords & accounts, tap " +
        "your Google account and sign in again - or remove it and add it back - then try again. " +
        "If the Play Store is also asking you to sign in, it is the same problem."

/** Shown when Google refuses the build rather than the account. */
fun googleMisconfiguredMessage(signingSha1: String?): String =
    "Google refused the sign-in because this build is not registered for it." +
        if (signingSha1.isNullOrBlank()) ""
        else "\n\nFor the developer: this build is signed with SHA-1 $signingSha1. That fingerprint and " +
            "the package name com.fati_market must both be registered in the Fati-Market Google Cloud project."

/**
 * Play services reports real failures as cancellations too, with the status
 * code in square brackets: "[16] Account reauth failed." is a phone whose
 * stored Google credential has gone stale, not a student changing their mind.
 *
 * Returns null when the message carries no code - a genuine dismissal, which
 * the screen stays quiet about. Anything else has to be said out loud, or the
 * button looks like it does nothing at all.
 */
fun describeCancelledSignIn(message: String?, signingSha1: String? = null): String? {
    val match = message?.trim()?.let { CANCELLATION_STATUS_CODE.find(it) } ?: return null
    val detail = match.groupValues[2].trim()

    return when (match.groupValues[1]) {
        // SIGN_IN_REQUIRED and the reauth variants of it.
        "4", "16" -> GOOGLE_REAUTH_MESSAGE

        // DEVELOPER_ERROR: the certificate or client ID does not match.
        "10" -> googleMisconfiguredMessage(signingSha1)

        else -> "Google could not complete the sign-in" +
            (if (detail.isBlank()) "" else " ($detail)") + ". Please try again."
    }
}

private val CANCELLATION_STATUS_CODE = Regex("""^\[(\d+)]\s*(.*)$""")

/**
 * The reason the legacy chooser failed, from its own status code.
 *
 * Kept as an Int rather than an ApiException so it stays testable off-device -
 * the codes are `GoogleSignInStatusCodes`, which the caller unwraps.
 */
fun describeLegacyStatus(code: Int, signingSha1: String? = null): String = when (code) {
    // DEVELOPER_ERROR - the one code that really does mean the build is wrong.
    10 -> googleMisconfiguredMessage(signingSha1)

    // NETWORK_ERROR
    7 -> "Google could not be reached. Check your internet connection and try again."

    // SIGN_IN_REQUIRED / INVALID_ACCOUNT
    4, 5 -> GOOGLE_REAUTH_MESSAGE

    // SIGN_IN_CURRENTLY_IN_PROGRESS
    12502 -> "A Google sign-in is already in progress. Wait for it to finish and try again."

    // SIGN_IN_FAILED - Google's catch-all, and what a stale account ends as.
    12500 -> GOOGLE_REAUTH_MESSAGE

    else -> "Google could not complete the sign-in (code $code). Please try again."
}

/** The reason a Google sign-in or sign-up did not complete. */
fun describeGoogleSignInFailure(e: Throwable): String = when (e) {
    is GoogleSignInFailure -> e.message ?: NO_GOOGLE_ACCOUNT_MESSAGE

    is GetCredentialCancellationException ->
        describeCancelledSignIn(e.message) ?: "Google sign-in was cancelled."

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
