package com.fati_market

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * What the counter makes of a scanned square.
 *
 * The website console hands its turnover off to a phone as a link, so the
 * same QR has to work whether it is read by the phone's own camera - which
 * opens the page in a browser - or by this app, which should open the native
 * turnover screen for the item the link names.
 */
class AdminCounterTest {

    @Test
    fun `an item code is taken as it is`() {
        assertEquals(
            "FMITEM1.7.1ef1b60622ac207d",
            AdminCounter.normalize("FMITEM1.7.1ef1b60622ac207d"),
        )
    }

    @Test
    fun `surrounding whitespace is ignored`() {
        assertEquals("FMQR1.42.abc123", AdminCounter.normalize("  FMQR1.42.abc123\n"))
    }

    @Test
    fun `a counter handoff link resolves to the item code inside it`() {
        val link = "https://fati.alertaraqc.com/turnover/FMITEM1.96.1ef1b60622ac207d?k=DfT7qsm2"

        assertEquals("FMITEM1.96.1ef1b60622ac207d", AdminCounter.normalize(link))
    }

    @Test
    fun `an order pickup link resolves to the order code inside it`() {
        assertEquals(
            "FMQR1.15.9f2c1a0b",
            AdminCounter.normalize("https://fati.alertaraqc.com/pickup/FMQR1.15.9f2c1a0b"),
        )
    }

    @Test
    fun `anything else is passed through for the screen to judge`() {
        assertEquals("FM-000015", AdminCounter.normalize("FM-000015"))
        assertEquals("https://example.com/", AdminCounter.normalize("https://example.com/"))
    }
}
