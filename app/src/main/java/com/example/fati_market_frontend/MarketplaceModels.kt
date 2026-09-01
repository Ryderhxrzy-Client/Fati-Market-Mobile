package com.fati_market

import org.json.JSONObject

/**
 * A marketplace item as the API presents it.
 *
 * Prices are peso decimal strings ("250.00") rather than numbers: the backend
 * is the only thing that does money arithmetic, so the app carries the exact
 * value it was handed and never re-derives one.
 *
 * Which of these fields are populated depends on who is asking. A student
 * seller only ever receives `sellerAskingPrice`; the buyer catalog receives
 * `publicPrice` and `rewardPoints`; admin receives everything.
 */
internal data class Item(
    val itemId: Int,
    val sellerId: Int,
    val sellerEmail: String,
    val title: String,
    val description: String,
    val categoryId: Int,
    val status: String,
    val photos: List<String>,
    val createdAt: String,

    /** What the student asked for. Seller and admin views only. */
    val sellerAskingPrice: String? = null,

    /** What Admin agreed to pay the seller. Admin view only. */
    val acquisitionPrice: String? = null,

    /** The buyer-facing selling price. Catalog and admin views. */
    val publicPrice: String? = null,

    /** publicPrice − acquisitionPrice. Admin view only. */
    val markup: String? = null,

    /** What a buyer earns once their purchase is completed. */
    val rewardPoints: Int = 0,

    val sellerPayoutStatus: String = "unpaid",
    val sellerPayoutAmount: String? = null,
    val sellerPaidAt: String? = null,

    val acquiredAt: String? = null,
    val turnoverNotes: String? = null,
    val meetupSchedule: String? = null,
    val publishedAt: String? = null,
    val rejectedReason: String? = null,

    val isTurnoverVerified: Boolean = false,
    val canBePublished: Boolean = false,
    val isLegacyPriced: Boolean = false,

    /**
     * The seller's turnover code, present only between Admin accepting the
     * offer and the item arriving at the store. Rendered as the QR the seller
     * shows at the counter.
     */
    val qrCode: String? = null,

    /** Counter proof: the item being received, the seller being paid. */
    val turnoverPhoto: String? = null,
    val sellerPayoutPhoto: String? = null,

    /**
     * True when the viewer is the buyer holding this item's reservation - a
     * reserved item reads as "someone else is checking out" to everyone else.
     */
    val reservedByMe: Boolean = false,
) {
    /** Admin has agreed a price - the moment the offer counts as accepted. */
    val offerAccepted: Boolean get() = acquisitionPrice != null

    /** The price to show a buyer. */
    val displayPrice: String get() = Money.format(publicPrice)

    /** The price to show the seller for their own listing. */
    val displayAskingPrice: String get() = Money.format(sellerAskingPrice)

    val isPending: Boolean get() = status.equals("pending", true) || status.equals("private", true)
    val isPublic: Boolean get() = status.equals("public", true)
    val isSold: Boolean get() = status.equals("sold", true)
    val isReserved: Boolean get() = status.equals("reserved", true)
    val isAcquired: Boolean get() = status.equals("acquired", true)
    val isRejected: Boolean get() = status.equals("rejected", true)

    val sellerIsPaid: Boolean get() = sellerPayoutStatus.equals("paid", true)
}

/**
 * Build an [Item] from an API object.
 *
 * Handles both the current payload and the shapes older endpoints returned, so
 * a mixed-version server never produces a blank price. `price_points` and
 * `markup_points` are read only as a last resort, and are interpreted as pesos
 * because the migration converted them to peso values.
 */
internal fun parseItem(raw: JSONObject): Item {
    val obj = raw.optJSONObject("item") ?: raw

    val photosArr = obj.optJSONArray("photos") ?: raw.optJSONArray("photos")
    val photos = if (photosArr != null) {
        (0 until photosArr.length()).map { photosArr.getString(it) }
    } else emptyList()

    fun str(key: String): String? =
        obj.optString(key).takeIf { it.isNotBlank() && it != "null" }
            ?: raw.optString(key).takeIf { it.isNotBlank() && it != "null" }

    // Legacy fallbacks. The backend migration converted these point values into
    // pesos at 1 point = 1 peso, so reading them as pesos is correct.
    val legacyAsking = obj.optInt("price_points", 0)
        .takeIf { it > 0 }?.let { "$it.00" }
    val legacyPublic = obj.optInt("markup_points", 0)
        .takeIf { it > 0 }?.let { "$it.00" }

    val publicPrice = str("public_price") ?: str("price") ?: legacyPublic

    return Item(
        itemId = obj.optInt("item_id"),
        sellerId = obj.optInt("seller_id"),
        sellerEmail = str("seller_email") ?: "",
        title = obj.optString("title"),
        description = obj.optString("description"),
        categoryId = obj.optInt("category_id"),
        status = str("status") ?: "",
        photos = photos,
        createdAt = obj.optString("created_at"),

        sellerAskingPrice = str("seller_asking_price") ?: legacyAsking,
        acquisitionPrice = str("acquisition_price"),
        publicPrice = publicPrice,
        markup = str("markup"),

        // Trust the server's figure; fall back to the shared rule only when the
        // payload predates the field.
        rewardPoints = if (obj.has("reward_points")) {
            obj.optInt("reward_points", 0)
        } else {
            LoyaltyRules.rewardPointsFor(publicPrice)
        },

        sellerPayoutStatus = str("seller_payout_status") ?: "unpaid",
        sellerPayoutAmount = str("seller_payout_amount"),
        sellerPaidAt = str("seller_paid_at"),

        acquiredAt = str("acquired_at"),
        turnoverNotes = str("turnover_notes"),
        meetupSchedule = str("meetup_schedule"),
        publishedAt = str("published_at"),
        rejectedReason = str("rejected_reason"),

        isTurnoverVerified = obj.optBoolean("is_turnover_verified", str("acquired_at") != null),
        canBePublished = obj.optBoolean("can_be_published", false),
        isLegacyPriced = obj.optBoolean("is_legacy_priced", false),
        qrCode = str("qr_code"),
        turnoverPhoto = str("turnover_photo"),
        sellerPayoutPhoto = str("seller_payout_photo"),
        reservedByMe = obj.optBoolean("reserved_by_me", false),
    )
}

/**
 * The server-calculated checkout breakdown.
 *
 * Every figure here comes from GET /api/checkout/quote. The screen displays
 * these values rather than computing its own, so what the buyer confirms is
 * exactly what the server will charge.
 */
internal data class CheckoutQuote(
    val itemId: Int,
    val itemTitle: String,
    val itemPrice: String,
    val availablePoints: Int,
    val pointsUsed: Int,
    val pointsDiscount: String,
    val amountDue: String,
    val maxUsablePoints: Int,
    val rewardPointsOnCompletion: Int,
    val isFullPointsCheckout: Boolean,
    val paymentRequired: Boolean,
)

internal fun parseCheckoutQuote(json: JSONObject): CheckoutQuote {
    val data = json.optJSONObject("data") ?: json

    return CheckoutQuote(
        itemId = data.optInt("item_id"),
        itemTitle = data.optString("item_title"),
        itemPrice = data.optString("item_price", "0.00"),
        availablePoints = data.optInt("available_points"),
        pointsUsed = data.optInt("points_used"),
        pointsDiscount = data.optString("points_discount", "0.00"),
        amountDue = data.optString("amount_due", "0.00"),
        maxUsablePoints = data.optInt("max_usable_points"),
        rewardPointsOnCompletion = data.optInt("reward_points_on_completion"),
        isFullPointsCheckout = data.optBoolean("is_full_points_checkout"),
        paymentRequired = data.optBoolean("payment_required", true),
    )
}

/** A buyer order / admin transaction row. */
internal data class MarketTransaction(
    val transactionId: Int,
    val itemId: Int,
    val itemTitle: String,
    val itemPhoto: String?,
    val buyerId: Int,
    val buyerEmail: String,
    val buyerName: String?,
    val buyerProfilePicture: String?,
    val buyerWalletPoints: Int,
    val receiptNo: String,
    val subtotal: String,
    val pointsUsed: Int,
    val pointsDiscountAmount: String,
    val amountDue: String,
    val paymentMethod: String,
    val paymentProof: String?,
    val paymentReference: String?,
    val paymentStatus: String,
    val pickupStatus: String,
    val status: String,
    val rewardPointsToCredit: Int,
    val rewardPointsCredited: Boolean,
    val isFullPointsCheckout: Boolean,
    val cancelReason: String?,
    val reservedUntil: String?,
    val createdAt: String,
    val availableActions: List<String>,

    /**
     * The signed walk-in pickup code. The buyer renders it as a QR at the
     * counter; the admin app scans it and lands on this exact order.
     */
    val qrCode: String? = null,

    /** Photo taken at the counter proving the item was handed over. */
    val handoverPhoto: String? = null,

    /** When the handover happened, for captioning that photo. */
    val completedAt: String? = null,
) {
    val awaitingProofReview: Boolean get() = paymentStatus == "proof_submitted"
    val isTerminal: Boolean get() = status in listOf("completed", "cancelled", "rejected")

    fun canDo(action: String): Boolean = availableActions.contains(action)
}

internal fun parseTransaction(obj: JSONObject): MarketTransaction {
    val item = obj.optJSONObject("item")
    val buyer = obj.optJSONObject("buyer")

    val photosArr = item?.optJSONArray("photos")
    val firstPhoto = if (photosArr != null && photosArr.length() > 0) photosArr.getString(0) else null

    val actionsArr = obj.optJSONArray("available_actions")
    val actions = if (actionsArr != null) {
        (0 until actionsArr.length()).map { actionsArr.getString(it) }
    } else emptyList()

    fun str(key: String): String? =
        obj.optString(key).takeIf { it.isNotBlank() && it != "null" }

    return MarketTransaction(
        transactionId = obj.optInt("transaction_id"),
        itemId = obj.optInt("item_id"),
        itemTitle = item?.optString("title") ?: "Item #${obj.optInt("item_id")}",
        itemPhoto = firstPhoto,
        buyerId = buyer?.optInt("user_id") ?: obj.optInt("buyer_id"),
        buyerEmail = buyer?.optString("email") ?: "",
        buyerName = buyer?.optString("name")?.takeIf { it.isNotBlank() && it != "null" },
        buyerProfilePicture = buyer?.optString("profile_picture")
            ?.takeIf { it.isNotBlank() && it != "null" },
        buyerWalletPoints = buyer?.optInt("wallet_points") ?: 0,
        receiptNo = obj.optString("receipt_no"),
        subtotal = obj.optString("subtotal", "0.00"),
        pointsUsed = obj.optInt("points_used"),
        pointsDiscountAmount = obj.optString("points_discount_amount", "0.00"),
        amountDue = obj.optString("amount_due", "0.00"),
        paymentMethod = obj.optString("payment_method"),
        paymentProof = str("payment_proof"),
        paymentReference = str("payment_reference"),
        paymentStatus = obj.optString("payment_status"),
        pickupStatus = obj.optString("pickup_status"),
        status = obj.optString("status"),
        rewardPointsToCredit = obj.optInt("reward_points_to_credit"),
        rewardPointsCredited = obj.optBoolean("reward_points_credited"),
        isFullPointsCheckout = obj.optBoolean("is_full_points_checkout"),
        cancelReason = str("cancel_reason"),
        reservedUntil = str("reserved_until"),
        createdAt = obj.optString("created_at"),
        availableActions = actions,
        qrCode = str("qr_code"),
        handoverPhoto = str("handover_photo"),
        completedAt = str("completed_at"),
    )
}

/**
 * A buyer's proof of transaction.
 *
 * Rendered on screen and exported to PDF, so the buyer can show it at the
 * store or keep it for their records.
 */
internal data class Receipt(
    val receiptNo: String,
    val issuedAt: String?,
    val isOfficial: Boolean,
    val statusLabel: String,
    val storeName: String,
    val buyerName: String,
    val buyerEmail: String,
    val itemTitle: String,
    val itemPhoto: String?,
    val subtotal: String,
    val pointsUsed: Int,
    val pointsDiscountAmount: String,
    val amountPaid: String,
    val rewardPointsEarned: Int,
    val paymentMethodLabel: String,
    val paymentReference: String?,
    val paymentStatus: String,
)

internal fun parseReceipt(json: JSONObject): Receipt {
    val d = json.optJSONObject("data") ?: json
    val buyer = d.optJSONObject("buyer")
    val item = d.optJSONObject("item")

    fun str(key: String): String? =
        d.optString(key).takeIf { it.isNotBlank() && it != "null" }

    return Receipt(
        receiptNo = d.optString("receipt_no"),
        issuedAt = str("issued_at"),
        isOfficial = d.optBoolean("is_official"),
        statusLabel = d.optString("status_label"),
        storeName = d.optString("store_name", "Ofelia Store"),
        buyerName = buyer?.optString("name").orEmpty(),
        buyerEmail = buyer?.optString("email").orEmpty(),
        itemTitle = item?.optString("title").orEmpty(),
        itemPhoto = item?.optString("photo")?.takeIf { it.isNotBlank() && it != "null" },
        subtotal = d.optString("subtotal", "0.00"),
        pointsUsed = d.optInt("points_used"),
        pointsDiscountAmount = d.optString("points_discount_amount", "0.00"),
        amountPaid = d.optString("amount_paid", "0.00"),
        rewardPointsEarned = d.optInt("reward_points_earned"),
        paymentMethodLabel = d.optString("payment_method_label"),
        paymentReference = str("payment_reference"),
        paymentStatus = d.optString("payment_status"),
    )
}

/** Human-readable labels for the order lifecycle. */
internal fun transactionStatusLabel(status: String): String = when (status) {
    "pending_payment" -> "Pending payment"
    "payment_proof_submitted" -> "Proof submitted"
    "payment_verified" -> "Payment verified"
    "reserved" -> "Reserved"
    "ready_for_pickup" -> "Ready for pickup"
    "completed" -> "Completed"
    "cancelled" -> "Cancelled"
    "rejected" -> "Rejected"
    else -> status.replaceFirstChar { it.uppercase() }
}

/**
 * The statuses an admin may set by hand, in lifecycle order.
 *
 * One list, so the edit pages and the counter screen offer the same choices -
 * they used to keep private copies that could drift apart.
 */
internal val ITEM_STATUS_OPTIONS = listOf("pending", "acquired", "public", "reserved", "sold")

internal fun itemStatusLabel(status: String): String = when (status.lowercase()) {
    "pending", "private" -> "Pending review"
    "acquired" -> "Acquired"
    "public" -> "Available"
    "reserved" -> "Reserved"
    "sold" -> "Sold"
    "rejected" -> "Rejected"
    else -> status.replaceFirstChar { it.uppercase() }
}

/**
 * An item category, as both apps' pickers and the admin console see it.
 *
 * [itemCount] is why a category can refuse to be deleted: the server will not
 * orphan listings, and the screen can say so before the admin tries.
 */
internal data class MarketCategory(
    val categoryId: Int,
    val name: String,
    val description: String?,
    val itemCount: Int = 0,
)

internal fun parseCategory(obj: JSONObject): MarketCategory = MarketCategory(
    categoryId = obj.optInt("category_id"),
    name = obj.optString("name"),
    description = obj.optString("description").takeIf { it.isNotBlank() && it != "null" },
    itemCount = obj.optInt("item_count", 0),
)

/**
 * One line of the store's history.
 *
 * Assembled by the server from rows that already record these moments - an
 * item's acquisition, an order's completion, a ledger entry - rather than from
 * an audit table, so the feed reaches back to the store's first day.
 */
internal data class ActivityEntry(
    val action: String,
    val user: String,
    val description: String,
    val resourceType: String,
    val resourceId: Int,
    val timestamp: String,
)

internal fun parseActivity(obj: JSONObject): ActivityEntry = ActivityEntry(
    action = obj.optString("action"),
    user = obj.optString("user"),
    description = obj.optString("description"),
    resourceType = obj.optString("resource_type"),
    resourceId = obj.optInt("resource_id"),
    timestamp = obj.optString("timestamp"),
)
