package com.fati_market

import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale

/**
 * When the store is open, from GET /api/store/hours.
 *
 * Times are wall-clock times in [zone]; [openDays] are ISO weekdays, 1 being
 * Monday. The meet-up picker is built from this, and the server checks a
 * booking against the same figures. The admin can save them in the backend;
 * its .env values remain the fallback before any setting is saved.
 */
internal data class StoreHours(
    val openTime: LocalTime,
    val closeTime: LocalTime,
    val openDays: Set<Int>,
    val slotMinutes: Int,
    /** "8:00 AM - 5:00 PM", as the server words it. */
    val label: String,
    val zone: ZoneId,
) {
    /**
     * The start times on [date], every [slotMinutes] from opening, that are
     * still ahead of [now]. The last one starts before closing.
     */
    fun slotsOn(date: LocalDate, now: ZonedDateTime): List<LocalTime> {
        val slots = mutableListOf<LocalTime>()
        var slot = openTime

        while (slot.isBefore(closeTime)) {
            if (date.atTime(slot).atZone(zone).isAfter(now)) slots += slot

            val next = slot.plusMinutes(slotMinutes.toLong())
            if (!next.isAfter(slot)) break // wrapped past midnight
            slot = next
        }

        return slots
    }

    /** In this month, not yet past, a day the store opens, with a slot left. */
    fun isBookableDay(date: LocalDate, now: ZonedDateTime): Boolean =
        YearMonth.from(date) == YearMonth.from(now) &&
            !date.isBefore(now.toLocalDate()) &&
            date.dayOfWeek.value in openDays &&
            slotsOn(date, now).isNotEmpty()

    /** Whether anything at all is still bookable this month. */
    fun hasBookableDay(now: ZonedDateTime): Boolean {
        val month = YearMonth.from(now)

        return (now.dayOfMonth..month.lengthOfMonth()).any { isBookableDay(month.atDay(it), now) }
    }

    /**
     * Whether today can no longer take a pick-up: the store is closed today,
     * or it is already at or past closing time, in the store's own [zone].
     *
     * Before opening on an open day is NOT past cutoff: an order placed at
     * 12:24 AM on a Wednesday can still be picked up that same Wednesday.
     */
    fun isPastCutoff(now: ZonedDateTime): Boolean {
        val local = now.withZoneSameInstant(zone)

        return local.dayOfWeek.value !in openDays || !local.toLocalTime().isBefore(closeTime)
    }

    /** The first open day on or after [date]. */
    fun nextOpenDayFrom(date: LocalDate): LocalDate {
        var day = date
        while (day.dayOfWeek.value !in openDays) day = day.plusDays(1)
        return day
    }

    /**
     * The first day a pick-up/meet-up can be scheduled: today while the store
     * can still take one, otherwise the next open day (skipping closed days).
     */
    fun earliestPickupDate(now: ZonedDateTime): LocalDate {
        val today = now.withZoneSameInstant(zone).toLocalDate()

        return nextOpenDayFrom(if (isPastCutoff(now)) today.plusDays(1) else today)
    }

    /** "Mon-Sat" for a run of days, "Mon, Wed, Fri" otherwise. */
    fun daysLabel(): String {
        val days = openDays.sorted()

        fun name(day: Int) = DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.getDefault())

        return when {
            days.size == 7 -> "Every day"
            days.size > 2 && days.last() - days.first() == days.size - 1 ->
                "${name(days.first())}-${name(days.last())}"
            else -> days.joinToString(", ") { name(it) }
        }
    }

    companion object {
        /** The backend's own defaults, used until GET /api/store/hours answers. */
        val DEFAULT = StoreHours(
            openTime = LocalTime.of(8, 0),
            closeTime = LocalTime.of(18, 0),
            openDays = (1..6).toSet(),
            slotMinutes = 30,
            label = "8:00 AM - 6:00 PM",
            zone = ZoneId.of("Asia/Manila"),
        )
    }
}

internal fun parseStoreHours(data: JSONObject): StoreHours {
    fun time(key: String, fallback: LocalTime): LocalTime =
        runCatching { LocalTime.parse(data.optString(key)) }.getOrDefault(fallback)

    val daysArr = data.optJSONArray("open_days")
    val days = if (daysArr != null) {
        (0 until daysArr.length()).map { daysArr.optInt(it) }.filter { it in 1..7 }.toSet()
    } else emptySet()

    // The fallbacks mirror the backend's own defaults, for a payload that is
    // missing a field rather than one that was configured.
    return StoreHours(
        openTime = time("open_time", LocalTime.of(8, 0)),
        closeTime = time("close_time", LocalTime.of(18, 0)),
        openDays = days.ifEmpty { (1..6).toSet() },
        slotMinutes = data.optInt("slot_minutes", 30).takeIf { it > 0 } ?: 30,
        label = data.optString("hours_label"),
        zone = runCatching { ZoneId.of(data.optString("timezone")) }.getOrDefault(ZoneId.systemDefault()),
    )
}
