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
        closeTime = time("close_time", LocalTime.of(17, 0)),
        openDays = days.ifEmpty { (1..6).toSet() },
        slotMinutes = data.optInt("slot_minutes", 30).takeIf { it > 0 } ?: 30,
        label = data.optString("hours_label"),
        zone = runCatching { ZoneId.of(data.optString("timezone")) }.getOrDefault(ZoneId.systemDefault()),
    )
}
