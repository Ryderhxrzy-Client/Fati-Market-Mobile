package com.fati_market

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.fati_market.auth.GoogleSignInFailure
import com.fati_market.auth.NO_GOOGLE_ACCOUNT_MESSAGE
import com.fati_market.auth.OFFLINE_MESSAGE
import com.fati_market.auth.describeGoogleSignInFailure
import com.fati_market.auth.describeNetworkFailure
import com.fati_market.auth.describeRequestFailure
import com.fati_market.auth.noGoogleAccountMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

/**
 * The words the auth screens use for a failure.
 *
 * The bug these guard against: the screen showed the library's own text -
 * "Google sign-up failed: No credentials available" - for every failure,
 * including a dropped Wi-Fi connection, so the student could not tell what
 * to fix.
 */
class AuthErrorsTest {

    @Test
    fun `no credentials is explained as an account or connection problem`() {
        val message = describeGoogleSignInFailure(NoCredentialException("No credentials available"))

        assertEquals(NO_GOOGLE_ACCOUNT_MESSAGE, message)
        assertTrue(message.contains("Google account"))
        assertTrue(message.contains("internet connection"))
        assertFalse(message.contains("No credentials available"))
    }

    @Test
    fun `a weak connection is reported as the server taking too long`() {
        val message = describeGoogleSignInFailure(SocketTimeoutException("timeout"))

        assertTrue(message.contains("took too long"))
        assertTrue(message.contains("connection may be weak"))
        assertFalse(message.contains("No credentials"))
    }

    @Test
    fun `no route to the server is reported as an internet problem`() {
        val message = describeNetworkFailure(UnknownHostException("Unable to resolve host \"fati-api.alertaraqc.com\""))

        assertTrue(message.contains("Could not reach the Fati Market server"))
        assertTrue(message.contains("internet connection"))
    }

    @Test
    fun `refused connection and tls failures get their own words`() {
        assertTrue(describeNetworkFailure(ConnectException("Connection refused")).contains("Could not connect"))
        assertTrue(describeNetworkFailure(SSLHandshakeException("handshake failed")).contains("secure connection"))
    }

    @Test
    fun `an unknown io failure keeps its detail`() {
        val message = describeNetworkFailure(IOException("unexpected end of stream"))

        assertTrue(message.startsWith("Network error: unexpected end of stream"))
    }

    @Test
    fun `cancelling the sheet is not blamed on anything`() {
        assertEquals(
            "Google sign-in was cancelled.",
            describeGoogleSignInFailure(GetCredentialCancellationException("User cancelled")),
        )
    }

    @Test
    fun `play services problems name play services`() {
        val message = describeGoogleSignInFailure(GetCredentialProviderConfigurationException("no provider"))

        assertTrue(message.contains("Google Play services"))
        assertTrue(message.contains("Play Store"))
    }

    @Test
    fun `interrupted and unsupported have specific advice`() {
        assertTrue(describeGoogleSignInFailure(GetCredentialInterruptedException("interrupted")).contains("interrupted"))
        assertTrue(describeGoogleSignInFailure(GetCredentialUnsupportedException("nope")).contains("email and password"))
    }

    @Test
    fun `other credential errors carry the type and the detail`() {
        val message = describeGoogleSignInFailure(
            GetCredentialCustomException("androidx.credentials.TYPE_SOMETHING_ODD", "server said no"),
        )

        assertTrue(message.contains("TYPE_SOMETHING_ODD"))
        assertTrue(message.contains("server said no"))
    }

    @Test
    fun `a failure already put into words is shown as is`() {
        assertEquals(OFFLINE_MESSAGE, describeGoogleSignInFailure(GoogleSignInFailure(OFFLINE_MESSAGE)))
    }

    @Test
    fun `the no-account message names the signing key when it is known`() {
        val message = noGoogleAccountMessage("66:06:C7:1F")

        assertTrue(message.startsWith(NO_GOOGLE_ACCOUNT_MESSAGE))
        assertTrue(message.contains("SHA-1 66:06:C7:1F"))
        assertTrue(message.contains("Firebase"))
        assertEquals(NO_GOOGLE_ACCOUNT_MESSAGE, noGoogleAccountMessage(null))
        assertEquals(NO_GOOGLE_ACCOUNT_MESSAGE, noGoogleAccountMessage(""))
    }

    @Test
    fun `anything else still says what it was`() {
        val message = describeGoogleSignInFailure(IllegalStateException("boom"))

        assertEquals("Google sign-in failed: boom", message)
        assertTrue(describeRequestFailure(IllegalStateException("boom")).contains("boom"))
        assertTrue(describeRequestFailure(SocketTimeoutException()).contains("took too long"))
    }
}
