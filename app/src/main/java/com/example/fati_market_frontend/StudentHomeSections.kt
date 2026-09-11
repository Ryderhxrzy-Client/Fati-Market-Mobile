package com.fati_market

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.Spacing
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** How many listings the home page features before "See all". */
internal const val FEATURED_COUNT = 8

/** How many category circles fit across a phone. */
private const val CIRCLES_PER_ROW = 5

private val LISTED_ON = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())

// ── Home: category circles ────────────────────────────────────────────────────

/**
 * The categories as round icons, the way a shop's home screen shows its
 * departments. As many as fit go across; past that the last circle is "More",
 * which opens the rest in place. Tapping a category filters the catalog to it,
 * and tapping it again clears the filter.
 */
@Composable
internal fun CategoryCircles(
    categories: List<Category>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val overflows = categories.size > CIRCLES_PER_ROW
    val shown = if (overflows && !expanded) categories.take(CIRCLES_PER_ROW - 1) else categories

    // null is the More / Less toggle.
    val cells: List<Category?> = shown + if (overflows) listOf(null) else emptyList()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        cells.chunked(CIRCLES_PER_ROW).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { category ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                        if (category == null) {
                            CategoryCircle(
                                icon = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.MoreHoriz,
                                label = if (expanded) "Less" else "More",
                                selected = false,
                                onClick = { expanded = !expanded },
                            )
                        } else {
                            val selected = category.id == selectedId

                            CategoryCircle(
                                icon = categoryIcon(category.name),
                                label = category.name,
                                selected = selected,
                                onClick = { onSelect(if (selected) null else category.id) },
                            )
                        }
                    }
                }
                repeat(CIRCLES_PER_ROW - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CategoryCircle(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val accents = LocalMarketAccents.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        // The chosen one wears the reward-gold ring around the same disc.
        Box(
            modifier = Modifier
                .size(58.dp)
                .then(if (selected) Modifier.border(2.5.dp, accents.reward, CircleShape) else Modifier)
                .padding(if (selected) 4.dp else 0.dp)
                .clip(CircleShape)
                .background(colors.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(if (selected) 22.dp else 26.dp),
            )
        }

        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) colors.primary else colors.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * An icon for a category, read from its name.
 *
 * Categories are Ofelia's to name and carry no icon of their own, so this
 * recognises the usual words and falls back to a generic one for the rest.
 */
internal fun categoryIcon(name: String): ImageVector {
    val lower = name.lowercase()

    fun has(vararg words: String) = words.any { it in lower }

    return when {
        // Before books: "Notebooks" contains "book", but a notebook is
        // stationery, not reading.
        has("notebook", "stationery", "stationary", "pen", "paper") -> Icons.Outlined.EditNote
        has("book", "novel", "module", "reviewer") -> Icons.AutoMirrored.Outlined.MenuBook
        has("gadget", "electronic", "phone", "laptop", "calculator", "tech", "computer") -> Icons.Outlined.Devices
        has("dorm", "living", "home", "furniture", "bed", "kitchen") -> Icons.Outlined.Bed
        has("uniform", "scrub", "gown", "medical", "nursing") -> Icons.Outlined.MedicalServices
        has("fashion", "cloth", "apparel", "wear", "shirt", "dress", "shoe") -> Icons.Outlined.Checkroom
        has("bag") -> Icons.Outlined.ShoppingBag
        has("supply", "supplies", "stationery", "school") -> Icons.Outlined.Edit
        has("sport", "gym", "fitness") -> Icons.Outlined.SportsBasketball
        has("music", "instrument") -> Icons.Outlined.MusicNote
        else -> Icons.Outlined.Category
    }
}

// ── My listings: the history card ─────────────────────────────────────────────

/**
 * A listing that has left the seller's hands, as one line of their history.
 *
 * Deliberately unlike the big card an open offer gets: nothing is left to edit
 * or ask about, so this is a compact record - the photo, when it was listed,
 * how far it has travelled, and the money that matters to the seller.
 */
@Composable
internal fun ListingHistoryCard(item: Item, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val accents = LocalMarketAccents.current
    val listedOn = Dates.parse(item.createdAt)?.atZone(ZoneId.systemDefault())?.format(LISTED_ON)

    // The figure that means something to the seller at this stage.
    val (amountLabel, amount) = when {
        item.isRejected -> null to null
        item.sellerIsPaid -> "Paid to you" to item.sellerPayoutAmount
        item.acquisitionPrice != null -> "Agreed" to item.acquisitionPrice
        else -> "Asked" to item.sellerAskingPrice
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(colors.surfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                val photo = item.photos.firstOrNull()

                if (photo != null) {
                    AsyncImage(
                        model = photo,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        Icons.Outlined.Image,
                        null,
                        tint = colors.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (amount != null) {
                        Text(
                            Money.format(amount),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (item.sellerIsPaid) accents.success else colors.onSurface,
                        )
                    }
                }

                Row {
                    Text(
                        listedOn?.let { "Listed $it" } ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    amountLabel?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(Spacing.xxs))

                if (item.isRejected) {
                    Text(
                        item.rejectedReason?.let { "Declined: $it" } ?: "Declined by the store",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    ListingProgress(item)
                }
            }
        }
    }
}

/**
 * Where the item has got to, as four short bars: the offer accepted, the item
 * received at the store, the seller paid, the item sold on.
 */
@Composable
private fun ListingProgress(item: Item) {
    val colors = MaterialTheme.colorScheme

    val steps = listOf(
        "Accepted" to item.offerAccepted,
        "Received" to item.isTurnoverVerified,
        "Paid" to item.sellerIsPaid,
        "Sold" to item.isSold,
    )

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            steps.forEach { (_, done) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(if (done) colors.primary else colors.outlineVariant),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            steps.forEach { (label, done) ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (done) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = if (done) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
