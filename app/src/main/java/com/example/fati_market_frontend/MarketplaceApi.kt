package com.fati_market

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Network calls for the cash marketplace: buyer checkout, admin acquisition,
 * turnover, publication and transaction management.
 *
 * All of these are blocking and must be called from Dispatchers.IO, matching
 * the convention used elsewhere in the app.
 */
internal object MarketplaceApi {

    const val BASE_URL = "https://fati-api.alertaraqc.com/api"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    /** The outcome of a call: either a parsed body, or the server's message. */
    internal sealed class Result<out T> {
        data class Ok<T>(val value: T) : Result<T>()
        data class Failure(val message: String, val code: Int) : Result<Nothing>()
    }

    // ── Buyer checkout ───────────────────────────────────────────────────

    /**
     * The server's own breakdown for a prospective purchase.
     *
     * The checkout screen renders this rather than calculating locally, so the
     * totals shown are the ones that will actually be charged.
     */
    fun fetchQuote(token: String, itemId: Int, pointsUsed: Int): Result<CheckoutQuote> =
        get(token, "/checkout/quote?item_id=$itemId&points_used=$pointsUsed") { parseCheckoutQuote(it) }

    fun checkout(token: String, itemId: Int, pointsUsed: Int, paymentMethod: String): Result<MarketTransaction> {
        val body = JSONObject().apply {
            put("item_id", itemId)
            put("points_used", pointsUsed)
            put("payment_method", paymentMethod)
        }

        return post(token, "/checkout", body) { parseTransaction(it.getJSONObject("data")) }
    }

    /**
     * Where to send a GCash payment.
     *
     * Served from backend config so Ofelia can change the account or the QR
     * without shipping a new app build.
     */
    fun fetchPaymentDetails(token: String): Result<GcashDetails> =
        get(token, "/checkout/payment-details") { json ->
            val gcash = json.getJSONObject("data").getJSONObject("gcash")

            fun str(key: String) =
                gcash.optString(key).takeIf { it.isNotBlank() && it != "null" }

            GcashDetails(
                accountName = str("account_name"),
                accountNumber = str("account_number"),
                qrImageUrl = str("qr_image_url"),
                instructions = str("instructions").orEmpty(),
            )
        }

    /** Upload a GCash receipt for admin review. There is no live gateway. */
    fun uploadPaymentProof(
        token: String,
        transactionId: Int,
        file: File,
        mimeType: String,
        referenceNumber: String? = null,
    ): Result<MarketTransaction> {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("proof", file.name, file.asRequestBody(mimeType.toMediaType()))
            .also { builder ->
                referenceNumber?.takeIf { it.isNotBlank() }?.let {
                    builder.addFormDataPart("reference_number", it)
                }
            }
            .build()

        val request = Request.Builder()
            .url("$BASE_URL/checkout/$transactionId/payment-proof")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .post(body)
            .build()

        return execute(request) { parseTransaction(it.getJSONObject("data")) }
    }

    fun cancelOwnCheckout(token: String, transactionId: Int): Result<MarketTransaction> =
        post(token, "/checkout/$transactionId/cancel", JSONObject()) {
            parseTransaction(it.getJSONObject("data"))
        }

    fun fetchMyTransactions(token: String): Result<List<MarketTransaction>> =
        get(token, "/transactions") { json ->
            val arr = json.getJSONArray("data")
            (0 until arr.length()).map { parseTransaction(arr.getJSONObject(it)) }
        }

    /**
     * Send a chat reply.
     *
     * Used by the in-app notification banner so the user can answer without
     * leaving the screen they are on.
     */
    fun sendChatReply(
        token: String,
        itemId: Int,
        receiverId: Int,
        message: String,
    ): Result<Unit> {
        val body = JSONObject().apply {
            put("receiver_id", receiverId)
            put("message", message)
        }

        return post(token, "/messages/$itemId", body) { }
    }

    /** The buyer's proof of transaction. */
    fun fetchReceipt(token: String, transactionId: Int): Result<Receipt> =
        get(token, "/transactions/$transactionId/receipt") { parseReceipt(it) }

    /**
     * The order a chat thread is about, if there is a live one.
     *
     * Conversations are scoped per item and per buyer, so this is what lets
     * Admin approve or decline straight from the conversation.
     */
    fun fetchOrderForConversation(
        token: String,
        itemId: Int,
        buyerId: Int,
    ): Result<MarketTransaction?> =
        get(token, "/admin/transactions?item_id=$itemId&buyer_id=$buyerId") { json ->
            val arr = json.getJSONArray("data")
            val all = (0 until arr.length()).map { parseTransaction(arr.getJSONObject(it)) }

            // An order still in flight is what the admin needs to act on; fall
            // back to the most recent closed one for context.
            all.firstOrNull { !it.isTerminal } ?: all.firstOrNull()
        }

    /**
     * One item, as the caller is entitled to see it.
     *
     * Tapping the photo on an order card opens the listing behind it, and the
     * server decides which prices that viewer gets.
     */
    fun fetchItem(token: String, itemId: Int): Result<Item> =
        get(token, "/items/$itemId") { parseItem(it.getJSONObject("data")) }

    fun fetchWalletPoints(token: String): Result<Int> =
        get(token, "/wallet") { it.optJSONObject("data")?.optInt("wallet_points", 0) ?: 0 }

    // ── Admin: inventory ─────────────────────────────────────────────────

    fun fetchAdminItems(token: String, status: String?): Result<List<Item>> {
        val path = if (status.isNullOrBlank()) "/admin/items" else "/admin/items?status=$status"

        return get(token, path) { json ->
            val arr = json.getJSONArray("data")
            (0 until arr.length()).map { parseItem(arr.getJSONObject(it)) }
        }
    }

    fun setAcquisitionPrice(token: String, itemId: Int, price: String): Result<Item> =
        post(token, "/admin/items/$itemId/acquisition-price", JSONObject().apply {
            put("acquisition_price", price)
        }) { parseItem(it.getJSONObject("data")) }

    fun setMeetupSchedule(token: String, itemId: Int, schedule: String?): Result<Item> =
        post(token, "/admin/items/$itemId/meetup", JSONObject().apply {
            put("meetup_schedule", schedule ?: JSONObject.NULL)
        }) { parseItem(it.getJSONObject("data")) }

    /**
     * Record that the item was physically received and verified. This is what
     * entitles the student seller to their cash.
     */
    fun verifyTurnover(
        token: String,
        itemId: Int,
        acquisitionPrice: String?,
        payoutAmount: String?,
        notes: String?,
    ): Result<Item> =
        post(token, "/admin/items/$itemId/verify-turnover", JSONObject().apply {
            acquisitionPrice?.let { put("acquisition_price", it) }
            payoutAmount?.let { put("seller_payout_amount", it) }
            notes?.takeIf { it.isNotBlank() }?.let { put("notes", it) }
        }) { parseItem(it.getJSONObject("data")) }

    /**
     * Resolve a scanned turnover QR to the listing it names.
     *
     * The mirror of [scanOrder]: the server checks the signature, so anything
     * that is not a genuine item code fails rather than resolving.
     */
    fun scanItem(token: String, code: String): Result<Item> =
        get(
            token,
            "/admin/items/scan?code=" + java.net.URLEncoder.encode(code, "UTF-8"),
        ) { parseItem(it.getJSONObject("data")) }

    /**
     * The counter version of [verifyTurnover]: mark the item received with
     * the proof photographed on the spot - the item in hand, the seller
     * being paid.
     */
    fun verifyTurnoverWithPhotos(
        token: String,
        itemId: Int,
        turnoverPhoto: File?,
        payoutPhoto: File?,
        payoutAmount: String? = null,
        acquisitionPrice: String? = null,
    ): Result<Item> {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

        acquisitionPrice?.let { builder.addFormDataPart("acquisition_price", it) }

        turnoverPhoto?.let {
            builder.addFormDataPart("turnover_photo", it.name, it.asRequestBody("image/jpeg".toMediaType()))
        }
        payoutPhoto?.let {
            builder.addFormDataPart("payout_photo", it.name, it.asRequestBody("image/jpeg".toMediaType()))
        }
        payoutAmount?.let { builder.addFormDataPart("seller_payout_amount", it) }

        val request = Request.Builder()
            .url("$BASE_URL/admin/items/$itemId/verify-turnover")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .post(builder.build())
            .build()

        return execute(request) { parseItem(it.getJSONObject("data")) }
    }

    /** Mark the seller as paid in cash - not a points transfer. */
    fun recordSellerPayout(token: String, itemId: Int, amount: String?): Result<Item> =
        post(token, "/admin/items/$itemId/seller-payout", JSONObject().apply {
            amount?.let { put("amount", it) }
        }) { parseItem(it.getJSONObject("data")) }

    /** What publishing at this price would mean, including the reward preview. */
    fun publishPreview(token: String, itemId: Int, publicPrice: String): Result<PublishPreview> =
        get(token, "/admin/items/$itemId/publish-preview?public_price=$publicPrice") { json ->
            val data = json.getJSONObject("data")
            val blockersArr = data.optJSONArray("blockers")

            PublishPreview(
                publicPrice = data.optString("public_price"),
                acquisitionPrice = data.optString("acquisition_price").takeIf { it.isNotBlank() && it != "null" },
                markup = data.optString("markup").takeIf { it.isNotBlank() && it != "null" },
                rewardPoints = data.optInt("reward_points"),
                canPublish = data.optBoolean("can_publish"),
                blockers = if (blockersArr != null) {
                    (0 until blockersArr.length()).map { blockersArr.getString(it) }
                } else emptyList(),
            )
        }

    fun publish(token: String, itemId: Int, publicPrice: String): Result<Item> =
        post(token, "/admin/items/$itemId/publish", JSONObject().apply {
            put("public_price", publicPrice)
        }) { parseItem(it.getJSONObject("data")) }

    fun unpublish(token: String, itemId: Int): Result<Item> =
        post(token, "/admin/items/$itemId/unpublish", JSONObject()) {
            parseItem(it.getJSONObject("data"))
        }

    fun rejectItem(token: String, itemId: Int, reason: String): Result<Item> =
        post(token, "/admin/items/$itemId/reject", JSONObject().apply {
            put("reason", reason)
        }) { parseItem(it.getJSONObject("data")) }

    // ── Admin: transactions ──────────────────────────────────────────────

    fun fetchAdminTransactions(token: String, status: String?): Result<List<MarketTransaction>> {
        val path = if (status.isNullOrBlank()) {
            "/admin/transactions"
        } else {
            "/admin/transactions?status=$status"
        }

        return get(token, path) { json ->
            val arr = json.getJSONArray("data")
            (0 until arr.length()).map { parseTransaction(arr.getJSONObject(it)) }
        }
    }

    /** Money that has arrived: a GCash transfer whose proof checks out. */
    fun verifyPayment(token: String, transactionId: Int): Result<MarketTransaction> =
        post(token, "/admin/transactions/$transactionId/verify-payment", JSONObject()) {
            parseTransaction(it.getJSONObject("data"))
        }

    /**
     * Approve a pay-at-the-store order without marking it paid.
     *
     * Cash is handed over at the counter, so this accepts the method the buyer
     * chose, holds the item and releases their pickup code. The money is
     * settled by [completeTransaction], where it is actually taken.
     */
    fun approveOrder(token: String, transactionId: Int): Result<MarketTransaction> =
        post(token, "/admin/transactions/$transactionId/approve-order", JSONObject()) {
            parseTransaction(it.getJSONObject("data"))
        }

    fun rejectPayment(token: String, transactionId: Int, reason: String): Result<MarketTransaction> =
        post(token, "/admin/transactions/$transactionId/reject-payment", JSONObject().apply {
            put("reason", reason)
        }) { parseTransaction(it.getJSONObject("data")) }

    fun markReadyForPickup(token: String, transactionId: Int): Result<MarketTransaction> =
        post(token, "/admin/transactions/$transactionId/ready-for-pickup", JSONObject()) {
            parseTransaction(it.getJSONObject("data"))
        }

    /** Completing is what credits the buyer's reward points, exactly once. */
    fun completeTransaction(token: String, transactionId: Int): Result<MarketTransaction> =
        post(token, "/admin/transactions/$transactionId/complete", JSONObject()) {
            parseTransaction(it.getJSONObject("data"))
        }

    /**
     * Complete a walk-in handover with the photo taken at the counter.
     *
     * The server refuses this while the payment is unverified, exactly as it
     * refuses the plain completion, so the photo can never rescue an unpaid
     * order.
     */
    fun completeTransactionWithPhoto(
        token: String,
        transactionId: Int,
        photo: File,
        mimeType: String,
    ): Result<MarketTransaction> {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("handover_photo", photo.name, photo.asRequestBody(mimeType.toMediaType()))
            .build()

        val request = Request.Builder()
            .url("$BASE_URL/admin/transactions/$transactionId/complete")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .post(body)
            .build()

        return execute(request) { parseTransaction(it.getJSONObject("data")) }
    }

    /**
     * Resolve a scanned order QR to the order it names.
     *
     * The server checks the signature inside the code, so anything that is not
     * a genuine Fati Market pickup code comes back as a failure.
     */
    fun scanOrder(token: String, code: String): Result<MarketTransaction> =
        get(
            token,
            "/admin/transactions/scan?code=" + java.net.URLEncoder.encode(code, "UTF-8"),
        ) { parseTransaction(it.getJSONObject("data")) }

    fun cancelTransaction(token: String, transactionId: Int, reason: String): Result<MarketTransaction> =
        post(token, "/admin/transactions/$transactionId/cancel", JSONObject().apply {
            put("reason", reason)
        }) { parseTransaction(it.getJSONObject("data")) }

    // ── Plumbing ─────────────────────────────────────────────────────────

    // ── Categories and activity ─────────────────────────────────────────

    /** Every category, with how many items each one holds. */
    fun fetchCategories(token: String): Result<List<MarketCategory>> =
        get(token, "/categories") { json ->
            val arr = json.getJSONArray("data")
            (0 until arr.length()).map { parseCategory(arr.getJSONObject(it)) }
        }

    fun createCategory(token: String, name: String, description: String?): Result<MarketCategory> =
        post(token, "/admin/categories", JSONObject().apply {
            put("name", name)
            description?.takeIf { it.isNotBlank() }?.let { put("description", it) }
        }) { parseCategory(it.getJSONObject("data")) }

    fun updateCategory(
        token: String,
        categoryId: Int,
        name: String,
        description: String?,
    ): Result<MarketCategory> {
        val body = JSONObject().apply {
            put("name", name)
            description?.takeIf { it.isNotBlank() }?.let { put("description", it) }
        }

        val request = Request.Builder()
            .url("$BASE_URL/admin/categories/$categoryId")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .put(body.toString().toRequestBody(JSON))
            .build()

        return execute(request) { parseCategory(it.getJSONObject("data")) }
    }

    /** Refused by the server while items still point at the category. */
    fun deleteCategory(token: String, categoryId: Int): Result<Unit> {
        val request = Request.Builder()
            .url("$BASE_URL/admin/categories/$categoryId")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .delete()
            .build()

        return execute(request) { }
    }

    /** What has been happening in the store, newest first. */
    fun fetchActivity(token: String, limit: Int = 100): Result<List<ActivityEntry>> =
        get(token, "/admin/activity?limit=$limit") { json ->
            val arr = json.getJSONArray("data")
            (0 until arr.length()).map { parseActivity(arr.getJSONObject(it)) }
        }

    private fun <T> get(token: String, path: String, parse: (JSONObject) -> T): Result<T> {
        val request = Request.Builder()
            .url("$BASE_URL$path")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .get()
            .build()

        return execute(request, parse)
    }

    private fun <T> post(token: String, path: String, body: JSONObject, parse: (JSONObject) -> T): Result<T> {
        val request = Request.Builder()
            .url("$BASE_URL$path")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .post(body.toString().toRequestBody(JSON))
            .build()

        return execute(request, parse)
    }

    /**
     * Run the call and surface the server's own message on failure, so the
     * user sees why an action was refused rather than a generic error.
     */
    private fun <T> execute(request: Request, parse: (JSONObject) -> T): Result<T> = try {
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            val json = runCatching { JSONObject(raw) }.getOrNull()

            if (response.isSuccessful && json != null) {
                Result.Ok(parse(json))
            } else {
                Result.Failure(
                    json?.optString("message")?.takeIf { it.isNotBlank() }
                        ?: "Request failed (${response.code})",
                    response.code,
                )
            }
        }
    } catch (e: Exception) {
        Result.Failure(e.message ?: "Network error", 0)
    }
}

/** The static GCash account a buyer pays into. */
internal data class GcashDetails(
    val accountName: String?,
    val accountNumber: String?,
    val qrImageUrl: String?,
    val instructions: String,
)

/** The admin reward preview shown before publishing. */
internal data class PublishPreview(
    val publicPrice: String,
    val acquisitionPrice: String?,
    val markup: String?,
    val rewardPoints: Int,
    val canPublish: Boolean,
    val blockers: List<String>,
)
