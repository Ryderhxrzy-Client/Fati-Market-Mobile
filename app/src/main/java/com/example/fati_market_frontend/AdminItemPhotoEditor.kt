package com.fati_market

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fati_market.auth.utils.copyUriToCache
import com.fati_market.ui.components.InfoBanner
import com.fati_market.ui.components.Overline
import com.fati_market.ui.components.SecondaryButton
import com.fati_market.ui.components.StatusTone
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** The most photos a listing carries; the server holds the same limit. */
private const val MAX_ITEM_PHOTOS = 5

/**
 * Ofelia's photos for a listing, before it goes on sale.
 *
 * A seller's snapshots are not always what the catalog should show, so while
 * the item is pending or in stock the admin can add shots of their own and
 * drop the ones that do not work. Each change goes to the server at once -
 * there is nothing to save - and [onPhotosChanged] hands back the new set so
 * the page's photo viewer shows it.
 */
@Composable
internal fun AdminItemPhotoEditor(
    itemId: Int,
    token: String,
    onPhotosChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var photos by remember(itemId) { mutableStateOf<List<ItemPhoto>?>(null) }
    var reloadKey by remember { mutableStateOf(0) }
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmRemove by remember { mutableStateOf<ItemPhoto?>(null) }

    fun apply(result: MarketplaceApi.Result<List<ItemPhoto>>) {
        when (result) {
            is MarketplaceApi.Result.Ok -> {
                photos = result.value
                error = null
                onPhotosChanged(result.value.map { it.url })
            }
            is MarketplaceApi.Result.Failure -> error = result.message
        }
    }

    LaunchedEffect(itemId, reloadKey) {
        apply(withContext(Dispatchers.IO) { MarketplaceApi.fetchItemPhotos(token, itemId) })
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val room = MAX_ITEM_PHOTOS - (photos?.size ?: 0)
        val picked = uris.take(room)

        if (picked.isEmpty()) return@rememberLauncherForActivityResult

        scope.launch {
            working = true

            // copyUriToCache names files by the millisecond, so each copy is
            // moved aside before the next one can land on the same name.
            val files = withContext(Dispatchers.IO) {
                picked.mapIndexed { index, uri ->
                    copyUriToCache(context, uri)?.let { (file, mimeType) ->
                        val unique = File(file.parentFile, "item_photo_${index}_${file.name}")
                        if (file.renameTo(unique)) unique to mimeType else file to mimeType
                    }
                }
            }

            if (files.any { it == null }) {
                error = "One or more photos could not be read or are over 5 MB."
            } else {
                apply(withContext(Dispatchers.IO) {
                    MarketplaceApi.addItemPhotos(token, itemId, files.filterNotNull())
                })

                if (error == null && uris.size > picked.size) {
                    error = "Only ${picked.size} more fit - a listing holds $MAX_ITEM_PHOTOS photos."
                }
            }

            files.filterNotNull().forEach { it.first.delete() }
            working = false
        }
    }

    confirmRemove?.let { photo ->
        AlertDialog(
            onDismissRequest = { confirmRemove = null },
            title = { Text("Remove this photo?") },
            text = { Text("It comes off the listing straight away.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRemove = null
                    scope.launch {
                        working = true
                        apply(withContext(Dispatchers.IO) {
                            MarketplaceApi.deleteItemPhoto(token, itemId, photo.photoId)
                        })
                        working = false
                    }
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemove = null }) { Text("Cancel") }
            },
        )
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        val current = photos

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                Overline("Photos" + (current?.let { " (${it.size}/$MAX_ITEM_PHOTOS)" } ?: ""))
            }
            if (working) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            }
        }

        Text(
            "Add your own shots or remove the seller's before this goes on sale. " +
                "Changes are saved right away.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when {
            current != null -> LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(current, key = { it.photoId }) { photo ->
                    Box(modifier = Modifier.size(92.dp)) {
                        AsyncImage(
                            model = photo.url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.small),
                        )

                        // A listing always keeps one photo.
                        if (current.size > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .clickable(enabled = !working) { confirmRemove = photo },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Remove photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }

                if (current.size < MAX_ITEM_PHOTOS) {
                    item(key = "add") {
                        Column(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                                .border(
                                    1.5.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    MaterialTheme.shapes.small,
                                )
                                .clickable(enabled = !working) { picker.launch("image/*") },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                Icons.Outlined.AddAPhoto,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                "Add photo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            error == null -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            else -> SecondaryButton("Try again", { reloadKey++ }, Modifier.fillMaxWidth())
        }

        error?.let { InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline) }
    }
}

/**
 * The big photo with a counter and a thumbnail strip, as the admin edit pages
 * show a listing. Keeps a valid place when the set shrinks under it, so a
 * photo removed by [AdminItemPhotoEditor] never leaves it pointing past the end.
 */
@Composable
internal fun AdminItemPhotoViewer(photos: List<String>, modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(0) }
    val index = selected.coerceIn(0, (photos.size - 1).coerceAtLeast(0))

    Column(modifier = modifier) {
        if (photos.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = photos[index],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (photos.size > 1) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            "${index + 1} / ${photos.size}",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            // Thumbnail strip
            if (photos.size > 1) {
                LazyRow(
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(photos) { i, url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (i == index) 2.dp else 1.dp,
                                    color = if (i == index) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selected = i }
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Photo, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}
