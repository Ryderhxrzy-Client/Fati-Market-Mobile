package com.fati_market

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.fati_market.ui.components.RowDivider
import com.fati_market.ui.components.SecondaryButton
import com.fati_market.ui.components.SettingsGroup
import com.fati_market.ui.components.TonalButton
import com.fati_market.ui.theme.Spacing

/**
 * Where Ofelia's Store physically is.
 *
 * Every meet-up and walk-in pickup happens at this address, so it is shown
 * on the profile (both roles), on the settings page and beside the store
 * hours. The coordinates are fixed here rather than fetched: the store does
 * not move, and a map that needs a network round-trip before it can even say
 * the address is worse than one that always can.
 */
object StoreLocation {
    const val NAME = "Ofelia's Store"
    const val ADDRESS = "Hollywood Terraces, Sumulong Hwy, Antipolo, 1870, Rizal"
    const val LATITUDE = 14.619292
    const val LONGITUDE = 121.151418

    private const val COORDS = "$LATITUDE,$LONGITUDE"

    /** The Google Maps embed: needs no API key, renders in a plain WebView. */
    const val EMBED_URL = "https://maps.google.com/maps?q=$COORDS&z=16&output=embed"

    /** The OpenStreetMap embed, used only if Google's cannot be loaded. */
    val fallbackEmbedUrl: String
        get() {
            val d = 0.0045
            val bbox = "${LONGITUDE - d}%2C${LATITUDE - d}%2C${LONGITUDE + d}%2C${LATITUDE + d}"
            return "https://www.openstreetmap.org/export/embed.html?bbox=$bbox&layer=mapnik&marker=$LATITUDE%2C$LONGITUDE"
        }

    /**
     * A page that frames the embed. Google's embed refuses to be loaded as
     * a top-level page ("must be used in an iframe"), so the WebView shows
     * this instead, and the map sits in the frame the way it would on a
     * website.
     */
    fun framedHtml(url: String): String = """
        <!doctype html>
        <html><head><meta name="viewport" content="width=device-width, initial-scale=1">
        <style>html,body{margin:0;height:100%;background:#e8ece9}iframe{border:0;width:100%;height:100%;display:block}</style>
        </head><body><iframe src="$url" allowfullscreen referrerpolicy="no-referrer-when-downgrade"></iframe></body></html>
    """.trimIndent()

    private const val VIEW_URL = "https://www.google.com/maps/search/?api=1&query=$COORDS"
    private const val DIRECTIONS_URL = "https://www.google.com/maps/dir/?api=1&destination=$COORDS"

    /** Opens the pin in whatever maps app the phone has; falls back to the browser. */
    fun openInMaps(context: Context) {
        val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$COORDS?q=$COORDS(${Uri.encode(NAME)})"))
        launch(context, geo, VIEW_URL)
    }

    /** Starts turn-by-turn directions to the store. */
    fun openDirections(context: Context) {
        val nav = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$COORDS"))
        launch(context, nav, DIRECTIONS_URL)
    }

    fun copyAddress(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Store address", ADDRESS))
        Toast.makeText(context, "Address copied", Toast.LENGTH_SHORT).show()
    }

    private fun launch(context: Context, preferred: Intent, webUrl: String) {
        try {
            context.startActivity(preferred)
        } catch (e: ActivityNotFoundException) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No maps app found on this phone.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/**
 * The store's map, address and the two things anyone wants to do with an
 * address: open it in Maps, or get directions. Styled as a [SettingsGroup]
 * so it sits naturally between the other groups on the profile and settings
 * pages.
 */
@Composable
fun StoreLocationGroup(
    modifier: Modifier = Modifier,
    title: String = "Store location",
    /** One line under the address - why the reader cares about this place. */
    subtitle: String = "Meet-ups and walk-in pickups happen here",
) {
    val context = LocalContext.current

    SettingsGroup(title = title, modifier = modifier) {
        StoreMapPreview(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            onClick = { StoreLocation.openInMaps(context) },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Spacing.lg, end = Spacing.xs, top = Spacing.md, bottom = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        MaterialTheme.shapes.extraSmall,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    StoreLocation.NAME,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    StoreLocation.ADDRESS,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = Spacing.xxs),
                )
            }
            IconButton(onClick = { StoreLocation.copyAddress(context) }) {
                Icon(
                    Icons.Filled.ContentCopy,
                    contentDescription = "Copy address",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        RowDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            TonalButton(
                text = "Directions",
                icon = Icons.Filled.Directions,
                compact = true,
                onClick = { StoreLocation.openDirections(context) },
                modifier = Modifier.weight(1f),
            )
            SecondaryButton(
                text = "Open in Maps",
                icon = Icons.Filled.Map,
                compact = true,
                onClick = { StoreLocation.openInMaps(context) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * A non-interactive map of the store, drawn by a WebView so no Maps SDK or
 * API key is needed. A transparent layer on top takes the tap and hands the
 * gesture off to the real Maps app, which also keeps the map from fighting
 * the page for scroll.
 *
 * If Google's embed cannot be reached the OpenStreetMap one is tried; if
 * that fails too a plain placeholder stays, and the address and buttons
 * below still work.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun StoreMapPreview(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var failed by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        if (!failed) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        isVerticalScrollBarEnabled = false
                        isHorizontalScrollBarEnabled = false
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)

                        webViewClient = object : WebViewClient() {
                            private var triedFallback = false

                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                // The embed itself may redirect (consent, locale); let those
                                // through. Anything the user could have triggered is handled
                                // by the tap overlay instead, so nothing else navigates here.
                                return request.hasGesture()
                            }

                            override fun onPageFinished(view: WebView, url: String?) {
                                loaded = true
                            }

                            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                                if (isMapFrame(request)) fallBack(view)
                            }

                            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                                if (isMapFrame(request)) fallBack(view)
                            }

                            private fun fallBack(view: WebView) {
                                if (!triedFallback) {
                                    triedFallback = true
                                    view.loadFramed(StoreLocation.fallbackEmbedUrl)
                                } else {
                                    failed = true
                                }
                            }

                            /** The map lives in the iframe, so its failures arrive as sub-frame errors. */
                            private fun isMapFrame(request: WebResourceRequest): Boolean {
                                val url = request.url.toString()
                                return request.isForMainFrame ||
                                    url == StoreLocation.EMBED_URL ||
                                    url == StoreLocation.fallbackEmbedUrl
                            }
                        }

                        loadFramed(StoreLocation.EMBED_URL)
                    }
                },
            )
        }

        if (failed || !loaded) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Filled.Map,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(36.dp),
                )
                Text(
                    if (failed) "Map preview unavailable" else "Loading map…",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.sm),
                )
            }
        }

        // The tap target, over everything. The WebView never sees a touch.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
        )

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.sm),
            shape = MaterialTheme.shapes.extraSmall,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shadowElevation = 1.dp,
        ) {
            Text(
                "Tap to open in Maps",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            )
        }
    }
}

/** Show [url] inside an iframe, from an https origin so the embed accepts the referer. */
private fun WebView.loadFramed(url: String) {
    loadDataWithBaseURL("https://www.google.com/", StoreLocation.framedHtml(url), "text/html", "utf-8", null)
}

/**
 * Where to collect an order, drawn inside a chat card once the order reaches
 * the pickup stage: a short map, the address, Directions and Open in Maps.
 * The same pin as the profile's Store location group, sized for a bubble.
 */
@Composable
fun PickupLocationCard(modifier: Modifier = Modifier, showMap: Boolean = true) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        if (showMap) {
            StoreMapPreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                onClick = { StoreLocation.openInMaps(context) },
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Icon(
                    Icons.Outlined.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    "Pick up at ${StoreLocation.NAME}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                StoreLocation.ADDRESS,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                TonalButton(
                    text = "Directions",
                    icon = Icons.Filled.Directions,
                    compact = true,
                    onClick = { StoreLocation.openDirections(context) },
                    modifier = Modifier.weight(1f),
                )
                SecondaryButton(
                    text = "Open in Maps",
                    icon = Icons.Filled.Map,
                    compact = true,
                    onClick = { StoreLocation.openInMaps(context) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
