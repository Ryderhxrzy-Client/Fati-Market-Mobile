package com.fati_market

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore

/**
 * Renders a receipt to a PDF and saves it where the buyer can find it.
 *
 * Uses the platform's own PdfDocument rather than pulling in a PDF library:
 * the layout is a single column of text, and a dependency for that would be
 * more weight than it is worth.
 *
 * The file goes to the public Downloads folder through MediaStore, which needs
 * no storage permission for an entry the app creates itself.
 */
internal object ReceiptPdf {

    private const val PAGE_WIDTH = 595   // A4 at 72dpi
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 48f

    /** The outcome, so the caller can tell the user what actually happened. */
    sealed class Result {
        data class Saved(val uri: Uri, val displayName: String) : Result()
        data class Failed(val message: String) : Result()
    }

    fun save(context: Context, receipt: Receipt): Result = try {
        val document = PdfDocument()
        val page = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        )

        draw(page.canvas, receipt)
        document.finishPage(page)

        val fileName = "Receipt-${receipt.receiptNo}.pdf"
        val uri = writeToDownloads(context, document, fileName)

        document.close()

        if (uri == null) {
            Result.Failed("Could not write the receipt to your Downloads folder.")
        } else {
            Result.Saved(uri, fileName)
        }
    } catch (e: Exception) {
        Result.Failed(e.message ?: "Could not create the receipt PDF.")
    }

    /** An intent for sharing or opening the saved receipt. */
    fun shareIntent(uri: Uri): Intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    // ── Drawing ─────────────────────────────────────────────────────────

    private fun draw(canvas: Canvas, receipt: Receipt) {
        val title = paint(20f, bold = true)
        val heading = paint(13f, bold = true)
        val body = paint(11f)
        val muted = paint(10f, color = Color.rgb(110, 110, 104))
        val total = paint(16f, bold = true)

        var y = MARGIN + 24f

        canvas.drawText(receipt.storeName, MARGIN, y, title)
        y += 20f
        canvas.drawText("Official Receipt", MARGIN, y, muted)

        // Receipt number, right-aligned.
        val noPaint = paint(13f, bold = true).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText(receipt.receiptNo, PAGE_WIDTH - MARGIN, MARGIN + 24f, noPaint)

        if (!receipt.isOfficial) {
            y += 22f
            canvas.drawText(
                "PROVISIONAL - payment not yet verified by the admin",
                MARGIN, y,
                paint(10f, bold = true, color = Color.rgb(178, 94, 2)),
            )
        }

        y += 26f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, dividerPaint())

        // ── Parties ─────────────────────────────────────────────────────
        y += 28f
        canvas.drawText("ISSUED TO", MARGIN, y, muted)
        y += 16f
        canvas.drawText(receipt.buyerName.ifBlank { receipt.buyerEmail }, MARGIN, y, body)
        if (receipt.buyerName.isNotBlank()) {
            y += 15f
            canvas.drawText(receipt.buyerEmail, MARGIN, y, muted)
        }

        receipt.issuedAt?.let {
            y += 20f
            canvas.drawText("Date: ${it.take(19).replace('T', ' ')}", MARGIN, y, muted)
        }

        y += 14f
        canvas.drawText("Status: ${receipt.statusLabel}", MARGIN, y, muted)

        // ── Item ────────────────────────────────────────────────────────
        y += 34f
        canvas.drawText("ITEM", MARGIN, y, muted)
        y += 18f
        canvas.drawText(receipt.itemTitle, MARGIN, y, heading)

        y += 22f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, dividerPaint())

        // ── Money ───────────────────────────────────────────────────────
        y += 30f
        y = row(canvas, y, "Item price", peso(receipt.subtotal), body)
        y = row(canvas, y, "Points used", receipt.pointsUsed.toString(), body)
        y = row(canvas, y, "Points discount", "-" + peso(receipt.pointsDiscountAmount), body)

        y += 10f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, dividerPaint())
        y += 30f

        canvas.drawText("AMOUNT PAID", MARGIN, y, heading)
        val totalPaint = total.apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText(peso(receipt.amountPaid), PAGE_WIDTH - MARGIN, y, totalPaint)

        // ── Payment ─────────────────────────────────────────────────────
        y += 34f
        y = row(canvas, y, "Payment method", receipt.paymentMethodLabel, body)

        receipt.paymentReference?.let {
            y = row(canvas, y, "GCash reference", it, body)
        }

        if (receipt.rewardPointsEarned > 0) {
            y = row(canvas, y, "Reward points earned", "+${receipt.rewardPointsEarned}", body)
        }

        // ── Footer ──────────────────────────────────────────────────────
        val footerY = PAGE_HEIGHT - MARGIN
        canvas.drawLine(MARGIN, footerY - 34f, PAGE_WIDTH - MARGIN, footerY - 34f, dividerPaint())
        canvas.drawText(
            "Keep this receipt as your proof of transaction.",
            MARGIN, footerY - 14f, muted,
        )
    }

    /** One label/value line. Returns the next y. */
    private fun row(canvas: Canvas, y: Float, label: String, value: String, body: Paint): Float {
        canvas.drawText(label, MARGIN, y, Paint(body).apply { color = Color.rgb(92, 92, 86) })
        canvas.drawText(
            value,
            PAGE_WIDTH - MARGIN,
            y,
            Paint(body).apply { textAlign = Paint.Align.RIGHT },
        )

        return y + 22f
    }

    private fun peso(amount: String): String = "PHP " + Money.formatPlain(amount)

    private fun paint(size: Float, bold: Boolean = false, color: Int = Color.rgb(26, 26, 24)) =
        Paint().apply {
            isAntiAlias = true
            textSize = size
            this.color = color
            typeface = Typeface.create(
                Typeface.DEFAULT,
                if (bold) Typeface.BOLD else Typeface.NORMAL,
            )
        }

    private fun dividerPaint() = Paint().apply {
        color = Color.rgb(214, 214, 206)
        strokeWidth = 1f
    }

    // ── Saving ──────────────────────────────────────────────────────────

    /**
     * Writes to the public Downloads folder through MediaStore.
     *
     * The app targets Android 13+, so scoped storage always applies and no
     * storage permission is required for an entry the app creates itself.
     */
    private fun writeToDownloads(
        context: Context,
        document: PdfDocument,
        fileName: String,
    ): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: return null

        resolver.openOutputStream(uri)?.use { document.writeTo(it) } ?: return null

        return uri
    }
}
