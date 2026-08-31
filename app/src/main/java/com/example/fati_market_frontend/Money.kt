package com.fati_market

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Peso amounts for display and input.
 *
 * The API sends money as exact decimal strings ("250.00"), and this keeps them
 * that way. Nothing here converts a price to Double: the backend owns every
 * calculation, and the app's job is to render what it was given and to send
 * back what the user typed.
 */
object Money {

    private val displayFormat = DecimalFormat("#,##0.00")

    const val PESO = "₱"

    /** "250.00" -> "₱250.00". A missing price renders as a dash. */
    fun format(decimalString: String?): String {
        val amount = parse(decimalString) ?: return "$PESO—"
        return PESO + displayFormat.format(amount)
    }

    /** "250.00" -> "250.00", without the currency symbol. */
    fun formatPlain(decimalString: String?): String {
        val amount = parse(decimalString) ?: return "—"
        return displayFormat.format(amount)
    }

    /**
     * Parse an API decimal string. Returns null when the value is absent or
     * malformed, so callers must decide what an unpriced item looks like
     * rather than silently showing zero.
     */
    fun parse(decimalString: String?): BigDecimal? {
        if (decimalString.isNullOrBlank()) return null
        return try {
            BigDecimal(decimalString.trim())
        } catch (_: NumberFormatException) {
            null
        }
    }

    /** Sort key for price ordering; unpriced items sort last. */
    fun sortKey(decimalString: String?): BigDecimal =
        parse(decimalString) ?: BigDecimal("-1")

    /**
     * Normalise what the user typed into the "0.00" form the API expects.
     * Returns null when the text is not a usable peso amount.
     */
    fun normalizeInput(raw: String): String? {
        val trimmed = raw.trim().removePrefix(PESO).trim()
        if (trimmed.isEmpty()) return null
        if (!Regex("""^\d+(\.\d{1,2})?$""").matches(trimmed)) return null

        return try {
            BigDecimal(trimmed).setScale(2, RoundingMode.UNNECESSARY).toPlainString()
        } catch (_: ArithmeticException) {
            null
        }
    }

    /**
     * Accept keystrokes for a peso field: digits plus at most one decimal
     * point with no more than two places.
     */
    fun isValidPriceInput(text: String): Boolean =
        text.isEmpty() || Regex("""^\d{0,8}(\.\d{0,2})?$""").matches(text)
}

/**
 * The loyalty rules, mirrored from the backend for previews only.
 *
 * The server recalculates every figure it acts on; these constants exist so
 * the screen can show the buyer what to expect before the request is made.
 */
object LoyaltyRules {

    /** A redeemed point is worth this many pesos off the bill. */
    const val PESOS_PER_REDEEMED_POINT = 5

    /** A buyer earns one point per this many pesos of selling price. */
    const val PESOS_PER_REWARD_POINT = 100

    /** rewardPoints = floor(publicSellingPrice / 100) */
    fun rewardPointsFor(publicPrice: String?): Int {
        val price = Money.parse(publicPrice) ?: return 0
        if (price.signum() <= 0) return 0
        return price.divide(BigDecimal(PESOS_PER_REWARD_POINT), 0, RoundingMode.FLOOR).toInt()
    }

    /** pointsDiscount = pointsUsed × ₱5 */
    fun discountFor(points: Int): BigDecimal =
        if (points <= 0) BigDecimal.ZERO
        else BigDecimal(points * PESOS_PER_REDEEMED_POINT).setScale(2)

    /** finalAmountDue = max(itemPrice − pointsDiscount, ₱0) */
    fun amountDue(itemPrice: String?, points: Int): BigDecimal {
        val price = Money.parse(itemPrice) ?: return BigDecimal.ZERO.setScale(2)
        val due = price.subtract(discountFor(points))
        return if (due.signum() < 0) BigDecimal.ZERO.setScale(2) else due.setScale(2)
    }

    /** The most points worth spending before the bill hits zero. */
    fun maxUsefulPoints(itemPrice: String?): Int {
        val price = Money.parse(itemPrice) ?: return 0
        if (price.signum() <= 0) return 0
        return price
            .divide(BigDecimal(PESOS_PER_REDEEMED_POINT), 0, RoundingMode.CEILING)
            .toInt()
    }

    /** "Earn 2 points after completed purchase" */
    fun rewardLabel(points: Int): String =
        "Earn $points point${if (points == 1) "" else "s"} after completed purchase"
}
