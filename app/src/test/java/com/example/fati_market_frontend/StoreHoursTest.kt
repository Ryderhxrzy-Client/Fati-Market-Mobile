package com.fati_market

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/** The meet-up picker's rules: this month only, open days, slots in store hours. */
class StoreHoursTest {

    private val manila = ZoneId.of("Asia/Manila")

    private val hours = StoreHours(
        openTime = LocalTime.of(8, 0),
        closeTime = LocalTime.of(17, 0),
        openDays = setOf(1, 2, 3, 4, 5, 6),
        slotMinutes = 30,
        label = "8:00 AM - 5:00 PM",
        zone = manila,
    )

    // Wednesday, 9 September 2026, 10:10 AM.
    private val now = ZonedDateTime.of(2026, 9, 9, 10, 10, 0, 0, manila)

    @Test
    fun slotsRunFromOpeningToTheLastStartBeforeClosing() {
        val slots = hours.slotsOn(LocalDate.of(2026, 9, 10), now)

        assertEquals(LocalTime.of(8, 0), slots.first())
        assertEquals(LocalTime.of(16, 30), slots.last())
        assertEquals(18, slots.size)
    }

    @Test
    fun todayOnlyOffersSlotsStillAhead() {
        val slots = hours.slotsOn(LocalDate.of(2026, 9, 9), now)

        assertEquals(LocalTime.of(10, 30), slots.first())
    }

    @Test
    fun onlyOpenDaysLeftInThisMonthAreBookable() {
        assertFalse(hours.isBookableDay(LocalDate.of(2026, 9, 8), now)) // yesterday
        assertTrue(hours.isBookableDay(LocalDate.of(2026, 9, 9), now)) // today, slots left
        assertFalse(hours.isBookableDay(LocalDate.of(2026, 9, 13), now)) // Sunday
        assertTrue(hours.isBookableDay(LocalDate.of(2026, 9, 30), now)) // last day of the month
        assertFalse(hours.isBookableDay(LocalDate.of(2026, 10, 1), now)) // next month
    }

    @Test
    fun aDayWithNoSlotLeftIsNotBookable() {
        val afterLastSlot = ZonedDateTime.of(2026, 9, 9, 16, 45, 0, 0, manila)

        assertFalse(hours.isBookableDay(LocalDate.of(2026, 9, 9), afterLastSlot))
    }

    @Test
    fun theLastEveningOfTheMonthHasNothingLeft() {
        val lastEvening = ZonedDateTime.of(2026, 9, 30, 18, 0, 0, 0, manila)

        assertFalse(hours.hasBookableDay(lastEvening))
        assertTrue(hours.hasBookableDay(now))
    }
}
