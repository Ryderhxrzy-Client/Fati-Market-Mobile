package com.fati_market.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fati_market.R

/**
 * The store's mark - the Fati-Market logo in res/drawable/app_icon_new.png -
 * wherever the app shows Ofelia's store.
 */
@Composable
fun StoreLogo(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    shape: Shape = RoundedCornerShape(percent = 22),
) {
    val context = LocalContext.current
    val bitmap = remember { StoreLogoBitmap.get(context) }

    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(shape),
    )
}

/**
 * Stands for the store in the icon slot of the shared components.
 *
 * Pass it where they take an [ImageVector] and they draw [StoreLogo], in its
 * own colours, instead of a tinted glyph. It is an empty vector, so it only
 * works through [MarketIcon]; a plain material Icon would draw nothing.
 */
val StoreLogoIcon: ImageVector = ImageVector.Builder(
    name = "StoreLogo",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).build()

/** An [Icon] that draws [StoreLogo] when it is handed [StoreLogoIcon]. */
@Composable
fun MarketIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    if (icon === StoreLogoIcon) {
        StoreLogo(modifier = modifier, contentDescription = contentDescription)
    } else {
        Icon(icon, contentDescription, modifier, tint)
    }
}

/**
 * The logo, decoded once and shared.
 *
 * The PNG is 2000 px square: decoded as-is it is 16 MB, and a catalog card
 * shows it at 12 dp. So it is sampled down to 500 px, and density scaling is
 * off - the file sits in the density-less drawable folder, which Android would
 * otherwise scale up by the screen density.
 */
private object StoreLogoBitmap {
    @Volatile
    private var cached: ImageBitmap? = null

    fun get(context: Context): ImageBitmap =
        cached ?: synchronized(this) {
            cached ?: decode(context).also { cached = it }
        }

    private fun decode(context: Context): ImageBitmap {
        val options = BitmapFactory.Options().apply {
            inSampleSize = 4
            inScaled = false
        }

        return BitmapFactory.decodeResource(context.resources, R.drawable.app_icon_new, options)
            .asImageBitmap()
    }
}
