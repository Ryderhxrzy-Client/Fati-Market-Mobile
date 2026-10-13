package com.fati_market.auth.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.FileOutputStream
import kotlin.math.max

fun getFileName(context: Context, uri: Uri): String {
    var result = "Document"
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) result = cursor.getString(index)
            }
        }
    } catch (e: Exception) {
        result = "Selected document"
    }
    return result
}

/** Uploads are capped at 20 MB, matching the server-side limit. */
const val MAX_UPLOAD_BYTES = 20L * 1024 * 1024

/**
 * Optimizes an image bitmap to avoid excessive memory and upload file size,
 * scaling it down so that max dimension does not exceed [maxDimension] (default 1920px),
 * and compressing with JPEG at [quality]% (default 85%).
 */
fun compressImageToCache(
    context: Context,
    uri: Uri,
    maxDimension: Int = 1920,
    quality: Int = 85
): Pair<File, String>? = try {
    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
    if (!mimeType.startsWith("image/") || mimeType.contains("pdf")) {
        copyUriToCache(context, uri)
    } else {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        }

        var sampleSize = 1
        val width = options.outWidth
        val height = options.outHeight
        if (width > maxDimension || height > maxDimension) {
            val largest = max(width, height)
            sampleSize = max(1, largest / maxDimension)
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        }

        if (bitmap == null) {
            copyUriToCache(context, uri)
        } else {
            val target = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            FileOutputStream(target).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            bitmap.recycle()

            if (target.exists() && target.length() > 0L) {
                target to "image/jpeg"
            } else {
                target.delete()
                null
            }
        }
    }
} catch (e: Exception) {
    copyUriToCache(context, uri)
}

/**
 * Copy a picked content:// URI into the cache directory so it can be uploaded
 * as a file.
 *
 * Returns the file and its MIME type, or null when the file cannot be read or
 * is over the size limit - the caller shows the user why rather than failing
 * silently at upload time.
 */
fun copyUriToCache(context: Context, uri: Uri): Pair<File, String>? = try {
    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
    val extension = when (mimeType) {
        "image/png" -> "png"
        "application/pdf" -> "pdf"
        else -> "jpg"
    }

    val target = File(context.cacheDir, "upload_${System.currentTimeMillis()}.$extension")

    context.contentResolver.openInputStream(uri)?.use { input ->
        target.outputStream().use { output -> input.copyTo(output) }
    }

    when {
        !target.exists() || target.length() == 0L -> {
            target.delete()
            null
        }
        target.length() > MAX_UPLOAD_BYTES -> {
            target.delete()
            null
        }
        else -> target to mimeType
    }
} catch (e: Exception) {
    null
}
