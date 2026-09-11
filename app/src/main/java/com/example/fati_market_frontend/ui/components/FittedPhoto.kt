package com.fati_market.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * A listing photo shown whole, never cropped.
 *
 * Photos come from students' phones in every shape, and cropping them to fill
 * a card cut off the very thing being sold. The whole image sits in the
 * middle, and whatever room it leaves is filled with a blurred, dimmed copy of
 * the same photo, so the card still reads as one picture instead of a
 * letterbox.
 *
 * The backdrop is requested small - it is blurred anyway - so it costs a
 * thumbnail's decode, not a second full-size one.
 */
@Composable
fun FittedPhoto(
    model: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val backdrop = remember(model) {
        ImageRequest.Builder(context).data(model).size(96).build()
    }

    Box(modifier = modifier.clipToBounds()) {
        AsyncImage(
            model = backdrop,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .blur(24.dp),
        )
        // The shade that sets the photo off from its own backdrop.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.18f)),
        )
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier.matchParentSize(),
        )
    }
}
