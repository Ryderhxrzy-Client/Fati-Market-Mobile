package com.fati_market.auth.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

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

/** Uploads are capped at 5 MB, matching the server-side limit. */
const val MAX_UPLOAD_BYTES = 5L * 1024 * 1024

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
