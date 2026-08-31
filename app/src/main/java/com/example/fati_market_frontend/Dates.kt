package com.fati_market

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formatting for the timestamps the API returns.
 *
 * Everything arrives as UTC ISO-8601 and is rendered in the device's own zone,
 * because a reservation deadline shown in UTC is worse than showing nothing.
 */
object Dates {

    private val readable: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.getDefault())

    private val readableWithYear: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a", Locale.getDefault())

    fun parse(iso: String?): Instant? {
        if (iso.isNullOrBlank()) return null

        return try {
            Instant.parse(iso)
        } catch (_: Exception) {
            // Laravel also emits "2026-08-31 15:45:00" without a zone.
            try {
                Instant.parse(iso.trim().replace(' ', 'T') + "Z")
            } catch (_: Exception) {
                null
            }
        }
    }

    /** "Aug 31, 3:45 PM" in the device's timezone. */
    fun short(iso: String?): String? =
        parse(iso)?.atZone(ZoneId.systemDefault())?.format(readable)

    /** "Aug 31, 2026 · 3:45 PM" for receipts, where the year matters. */
    fun full(iso: String?): String? =
        parse(iso)?.atZone(ZoneId.systemDefault())?.format(readableWithYear)

    /**
     * How long is left, as "5h 12m" or "20m".
     *
     * Returns null once the moment has passed, so callers must decide what an
     * expired deadline looks like rather than showing a negative countdown.
     */
    fun timeUntil(iso: String?): String? {
        val target = parse(iso) ?: return null
        val remaining = Duration.between(Instant.now(), target)

        if (remaining.isNegative || remaining.isZero) return null

        val hours = remaining.toHours()
        val minutes = remaining.toMinutes() % 60

        return when {
            hours >= 24 -> "${remaining.toDays()}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "less than a minute"
        }
    }

    fun hasPassed(iso: String?): Boolean {
        val target = parse(iso) ?: return false
        return Instant.now().isAfter(target)
    }
}
