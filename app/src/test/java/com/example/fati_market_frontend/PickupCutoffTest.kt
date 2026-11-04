package com.fati_market

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * When a buyer checks out, which day is the first they can pick up?
 *
 * Store hours are Monday to Saturday, 8:00 AM to 6:00 PM. Checking out
 * before opening (even at midnight) still lands on the same day; only an
 * order at or after closing, or on a closed day, moves to the next open day.
 */
class PickupCutoffTest {

    private val manila = ZoneId.of("Asia/Manila")

    private val hours = StoreHours(
        openTime = LocalTime.of(8, 0),
        closeTime = LocalTime.of(18, 0),
        openDays = setOf(1, 2, 3, 4, 5, 6),
        slotMinutes = 30,
        label = "8:00 AM - 6:00 PM",
        zone = manila,
    )

    private fun at(day: Int, hour: Int, minute: Int, second: Int = 0): ZonedDateTime =
        ZonedDateTime.of(2026, 11, day, hour, minute, second, 0, manila)

    private fun nov(day: Int) = LocalDate.of(2026, 11, day)

    // November 2026: Wed 4, Thu 5, Sat 7, Sun 8, Mon 9.
    private val cases = listOf(
        // now, expected earliest pick-up date, past cutoff
        Triple(at(4, 0, 0), nov(4), false), // Wednesday 12:00 AM (midnight)
        Triple(at(4, 0, 24), nov(4), false), // Wednesday 12:24 AM
        Triple(at(4, 7, 0), nov(4), false), // Wednesday 7:00 AM
        Triple(at(4, 7, 59, 59), nov(4), false), // Wednesday 7:59:59 AM
        Triple(at(4, 8, 0), nov(4), false), // Wednesday 8:00 AM (opening)
        Triple(at(4, 10, 0), nov(4), false), // Wednesday 10:00 AM
        Triple(at(4, 17, 59, 59), nov(4), false), // Wednesday 5:59:59 PM
        Triple(at(4, 18, 0), nov(5), true), // Wednesday 6:00 PM (closing)
        Triple(at(4, 18, 30), nov(5), true), // Wednesday 6:30 PM
        Triple(at(4, 23, 59, 59), nov(5), true), // Wednesday 11:59 PM
        Triple(at(7, 19, 0), nov(9), true), // Saturday 7:00 PM
        Triple(at(8, 0, 30), nov(9), true), // Sunday 12:30 AM
        Triple(at(8, 10, 0), nov(9), true), // Sunday 10:00 AM
        Triple(at(8, 20, 0), nov(9), true), // Sunday 8:00 PM
    )

    @Test
    fun earliestPickupDateFollowsStoreHours() {
        for ((now, expected, pastCutoff) in cases) {
            assertEquals("isPastCutoff at $now", pastCutoff, hours.isPastCutoff(now))
            assertEquals("earliest at $now", expected, hours.earliestPickupDate(now))
        }
    }

    @Test
    fun theStoreClockIsReadInStoreTimeNotPhoneTime() {
        // 16:30 UTC on Tuesday is 00:30 Wednesday in Manila: still Wednesday.
        val utc = ZonedDateTime.of(2026, 11, 3, 16, 30, 0, 0, ZoneId.of("UTC"))

        assertFalse(hours.isPastCutoff(utc))
        assertEquals(nov(4), hours.earliestPickupDate(utc))

        // 10:30 UTC on Wednesday is 6:30 PM in Manila: closed, tomorrow.
        val utcEvening = ZonedDateTime.of(2026, 11, 4, 10, 30, 0, 0, ZoneId.of("UTC"))

        assertTrue(hours.isPastCutoff(utcEvening))
        assertEquals(nov(5), hours.earliestPickupDate(utcEvening))
    }

    @Test
    fun nextOpenDaySkipsClosedDays() {
        assertEquals(nov(4), hours.nextOpenDayFrom(nov(4))) // Wednesday stays
        assertEquals(nov(9), hours.nextOpenDayFrom(nov(8))) // Sunday to Monday

        val weekdaysOnly = hours.copy(openDays = setOf(1, 2, 3, 4, 5))
        assertEquals(nov(9), weekdaysOnly.nextOpenDayFrom(nov(7))) // Saturday to Monday
        assertEquals(nov(9), weekdaysOnly.earliestPickupDate(at(6, 18, 0))) // Friday closing to Monday
    }

    @Test
    fun defaultHoursMatchTheBackendDefaults() {
        assertEquals(LocalTime.of(8, 0), StoreHours.DEFAULT.openTime)
        assertEquals(LocalTime.of(18, 0), StoreHours.DEFAULT.closeTime)
        assertEquals(setOf(1, 2, 3, 4, 5, 6), StoreHours.DEFAULT.openDays)
        assertEquals(manila, StoreHours.DEFAULT.zone)
    }
}
