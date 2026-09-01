package com.fati_market.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fati_market.safeAreaTopHeight
import com.fati_market.ui.theme.DarkGreen
import com.fati_market.ui.theme.DarkGreenLight
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.OverlineStyle
import com.fati_market.ui.theme.PriceStyle
import com.fati_market.ui.theme.PriceStyleLarge
import com.fati_market.ui.theme.PriceStyleSmall
import com.fati_market.ui.theme.Spacing

/**
 * The shared building blocks for the app's screens.
 *
 * Every one of these exists because the same pattern was being rebuilt by hand
 * on multiple screens with slightly different padding, radius and colour. Using
 * them keeps a card on the buyer catalog and a card on the admin inventory
 * looking like the same product.
 */

// ── Containers ──────────────────────────────────────────────────────────

/**
 * The standard content card: soft elevation, generous radius, no hard border.
 *
 * Prefer this over a raw `Card` so shadow and corner radius stay uniform.
 */
@Composable
fun MarketCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(Spacing.lg),
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.medium

    ElevatedCard(
        modifier = if (onClick != null) {
            modifier.clip(shape).clickable(onClick = onClick)
        } else {
            modifier
        },
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            content = content,
        )
    }
}

/**
 * A quieter card for grouped detail, drawn with an outline instead of a
 * shadow so it can sit inside a MarketCard without competing with it.
 */
@Composable
fun MarketPanel(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentPadding: PaddingValues = PaddingValues(Spacing.md),
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            content = content,
        )
    }
}

// ── Labels and headings ─────────────────────────────────────────────────

/** Small uppercase label above a group of fields or a section of content. */
@Composable
fun Overline(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Text(
        text = text.uppercase(),
        style = OverlineStyle,
        color = color,
        modifier = modifier,
    )
}

/**
 * The full-screen page header, built the way the admin dashboard builds it.
 *
 * The status bar clearance is a Spacer of its own above a fixed 56.dp row -
 * not a Material [TopAppBar], whose 64.dp height plus its own window insets
 * left the student pages with a noticeably taller gap up top than every
 * other page in the app. Same construction, same height, everywhere.
 */
@Composable
fun MarketPageTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector = Icons.Filled.ArrowBack,
    navigationContentDescription: String = "Back",
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(DarkGreen, DarkGreenLight))),
    ) {
        Spacer(Modifier.safeAreaTopHeight())

        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(navigationIcon, contentDescription = navigationContentDescription, tint = Color.White)
            }
            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            actions()
        }
    }
}

/** A section title with an optional trailing action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .clickable(onClick = onAction)
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            )
        }
    }
}

// ── Money and points ────────────────────────────────────────────────────

enum class PriceSize { Small, Medium, Large }

/**
 * A peso price.
 *
 * Always rendered through this so prices share one weight and colour, and a
 * price never gets mistaken for ordinary body text.
 */
@Composable
fun PriceTag(
    price: String,
    modifier: Modifier = Modifier,
    size: PriceSize = PriceSize.Medium,
    color: Color = MaterialTheme.colorScheme.primary,
    strikethrough: Boolean = false,
) {
    Text(
        text = price,
        style = when (size) {
            PriceSize.Small -> PriceStyleSmall
            PriceSize.Medium -> PriceStyle
            PriceSize.Large -> PriceStyleLarge
        },
        color = if (strikethrough) MaterialTheme.colorScheme.onSurfaceVariant else color,
        textDecoration = if (strikethrough) {
            androidx.compose.ui.text.style.TextDecoration.LineThrough
        } else null,
        modifier = modifier,
    )
}

/**
 * The reward preview a buyer sees on a listing.
 *
 * Amber is reserved for loyalty throughout the app, so this chip reads as
 * "points" without needing to repeat the word everywhere.
 */
@Composable
fun RewardChip(
    points: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    if (points <= 0) return

    val accents = LocalMarketAccents.current

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = accents.rewardContainer,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) Spacing.sm else Spacing.md,
                vertical = if (compact) 3.dp else Spacing.xs,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Icon(
                Icons.Filled.Stars,
                contentDescription = null,
                tint = accents.onRewardContainer,
                modifier = Modifier.size(if (compact) 12.dp else 14.dp),
            )
            Text(
                text = if (compact) {
                    "$points pt${if (points == 1) "" else "s"}"
                } else {
                    "Earn $points point${if (points == 1) "" else "s"}"
                },
                style = if (compact) {
                    MaterialTheme.typography.labelSmall
                } else {
                    MaterialTheme.typography.labelMedium
                },
                fontWeight = FontWeight.SemiBold,
                color = accents.onRewardContainer,
            )
        }
    }
}

/** The wallet balance chip shown in headers. */
@Composable
fun PointsBalanceChip(
    points: Int,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val accents = LocalMarketAccents.current

    Surface(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = CircleShape,
        color = accents.rewardContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Icon(
                Icons.Filled.Stars,
                contentDescription = null,
                tint = accents.onRewardContainer,
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = if (visible) "$points pts" else "••• pts",
                style = MaterialTheme.typography.labelLarge,
                color = accents.onRewardContainer,
            )
        }
    }
}

// ── Status ──────────────────────────────────────────────────────────────

enum class StatusTone { Neutral, Success, Warning, Danger, Info, Brand }

/**
 * A status badge.
 *
 * Tone carries the meaning, so the same vocabulary of colours applies to an
 * item status, a payment status and a pickup status alike.
 */
@Composable
fun StatusPill(
    label: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.Neutral,
    icon: ImageVector? = null,
) {
    val accents = LocalMarketAccents.current

    val (container, content) = when (tone) {
        StatusTone.Success -> accents.successContainer to accents.onSuccessContainer
        StatusTone.Warning -> accents.warningContainer to accents.onWarningContainer
        StatusTone.Info -> accents.infoContainer to accents.onInfoContainer
        StatusTone.Danger -> MaterialTheme.colorScheme.errorContainer to
            MaterialTheme.colorScheme.onErrorContainer
        StatusTone.Brand -> MaterialTheme.colorScheme.primaryContainer to
            MaterialTheme.colorScheme.onPrimaryContainer
        StatusTone.Neutral -> MaterialTheme.colorScheme.surfaceContainerHigh to
            MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(modifier = modifier, shape = CircleShape, color = container) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            if (icon != null) {
                Icon(icon, null, tint = content, modifier = Modifier.size(13.dp))
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = content)
        }
    }
}

/**
 * Maps an item status to its badge tone and wording.
 *
 * `offerAccepted` splits the pending state in two: an offer Admin has priced
 * is no longer "pending review" to anyone reading it - it is accepted, and
 * only the physical hand-over remains.
 */
@Composable
fun ItemStatusPill(status: String, modifier: Modifier = Modifier, offerAccepted: Boolean = false) {
    val (label, tone) = when (status.lowercase()) {
        "pending", "private" ->
            if (offerAccepted) "Offer accepted" to StatusTone.Success
            else "Pending review" to StatusTone.Warning
        "acquired" -> "In stock" to StatusTone.Info
        "public" -> "Available" to StatusTone.Success
        "reserved" -> "Reserved" to StatusTone.Warning
        "sold" -> "Sold" to StatusTone.Neutral
        "rejected" -> "Rejected" to StatusTone.Danger
        else -> status.replaceFirstChar { it.uppercase() } to StatusTone.Neutral
    }

    StatusPill(label = label, tone = tone, modifier = modifier)
}

/**
 * Maps an order status to its badge tone and wording.
 *
 * `pending_payment` means two different things depending on how the buyer is
 * paying, so pass [paymentMethod] where it is known. A GCash buyer still owes
 * the money - they are the ones awaiting payment. A cash buyer owes nothing
 * yet: they pay at the counter, and what they are waiting on is Admin
 * accepting the order.
 */
@Composable
fun TransactionStatusPill(
    status: String,
    modifier: Modifier = Modifier,
    paymentMethod: String? = null,
) {
    val (label, tone) = when (status) {
        "pending_payment" -> when (paymentMethod) {
            "cash" -> "Awaiting admin approval" to StatusTone.Warning
            else -> "Awaiting payment" to StatusTone.Warning
        }
        "payment_proof_submitted" -> "Proof submitted" to StatusTone.Info
        "payment_verified" -> "Payment verified" to StatusTone.Info
        "reserved" -> "Reserved" to StatusTone.Info
        "ready_for_pickup" -> "Ready for pickup" to StatusTone.Brand
        "completed" -> "Completed" to StatusTone.Success
        "cancelled" -> "Cancelled" to StatusTone.Neutral
        "rejected" -> "Rejected" to StatusTone.Danger
        else -> status.replaceFirstChar { it.uppercase() } to StatusTone.Neutral
    }

    StatusPill(label = label, tone = tone, modifier = modifier)
}

// ── Rows ────────────────────────────────────────────────────────────────

/**
 * A label/value line for breakdowns and detail lists.
 *
 * `emphasized` is for the line that matters most - a total, an amount due -
 * which is drawn heavier so the eye lands on it first.
 */
@Composable
fun SummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    valueColor: Color? = null,
    supporting: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = if (emphasized) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                color = if (emphasized) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            if (supporting != null) {
                Text(
                    supporting,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.width(Spacing.md))

        Text(
            value,
            style = if (emphasized) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
                ?: if (emphasized) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
        )
    }
}

/** A detail line with a leading icon, for metadata like seller or date. */
@Composable
fun IconInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SoftDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

// ── Buttons ─────────────────────────────────────────────────────────────

/**
 * The main call to action.
 *
 * Height and radius are fixed here so primary actions have the same physical
 * presence on every screen, and `loading` swaps the label for a spinner
 * without the button resizing under the user's finger.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    /** Shorter, for pinned strips where a full-height button crowds out the content. */
    compact: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(if (compact) 38.dp else 52.dp),
        contentPadding = if (compact) PaddingValues(horizontal = Spacing.md) else ButtonDefaults.ContentPadding,
        enabled = enabled && !loading,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(
                text,
                style = if (compact) {
                    MaterialTheme.typography.labelMedium
                } else {
                    MaterialTheme.typography.labelLarge
                },
            )
        }
    }
}

/** The lower-emphasis companion to [PrimaryButton]. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    /** Shorter, matching [PrimaryButton]'s compact form. */
    compact: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(if (compact) 38.dp else 52.dp),
        contentPadding = if (compact) PaddingValues(horizontal = Spacing.md) else ButtonDefaults.ContentPadding,
        enabled = enabled && !loading,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.45f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(
                text,
                style = if (compact) {
                    MaterialTheme.typography.labelMedium
                } else {
                    MaterialTheme.typography.labelLarge
                },
            )
        }
    }
}

// ── Feedback ────────────────────────────────────────────────────────────

/**
 * An inline message: a rule the user has to satisfy, a warning, a confirmation.
 *
 * Tinted rather than loud, so a form can carry several without shouting.
 */
@Composable
fun InfoBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.Info,
    icon: ImageVector? = null,
    title: String? = null,
) {
    val accents = LocalMarketAccents.current

    val (container, content) = when (tone) {
        StatusTone.Success -> accents.successContainer to accents.onSuccessContainer
        StatusTone.Warning -> accents.warningContainer to accents.onWarningContainer
        StatusTone.Danger -> MaterialTheme.colorScheme.errorContainer to
            MaterialTheme.colorScheme.onErrorContainer
        StatusTone.Brand -> MaterialTheme.colorScheme.primaryContainer to
            MaterialTheme.colorScheme.onPrimaryContainer
        StatusTone.Neutral -> MaterialTheme.colorScheme.surfaceContainerHigh to
            MaterialTheme.colorScheme.onSurfaceVariant
        StatusTone.Info -> accents.infoContainer to accents.onInfoContainer
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = container,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            if (icon != null) {
                Icon(icon, null, tint = content, modifier = Modifier.size(18.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                if (title != null) {
                    Text(title, style = MaterialTheme.typography.titleSmall, color = content)
                }
                Text(text, style = MaterialTheme.typography.bodySmall, color = content)
            }
        }
    }
}

/**
 * The screen shown when a list has nothing in it.
 *
 * An empty list with no explanation reads as a bug; this says what would
 * appear here and, where there is one, offers the action that fills it.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(34.dp),
            )
        }

        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.xs))
            PrimaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

/**
 * A shimmering placeholder.
 *
 * Skeletons that match the shape of the content coming make a load feel
 * shorter than a centred spinner does, and stop the layout jumping when the
 * data lands.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(10.dp),
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerProgress",
    )

    val base = MaterialTheme.colorScheme.surfaceContainer
    val highlight = MaterialTheme.colorScheme.surfaceContainerHighest

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start = androidx.compose.ui.geometry.Offset(progress * 600f - 300f, 0f),
                    end = androidx.compose.ui.geometry.Offset(progress * 600f, 300f),
                )
            )
    )
}

/** A skeleton shaped like a catalog card, used while the list loads. */
@Composable
fun ItemCardSkeleton(modifier: Modifier = Modifier) {
    MarketCard(modifier = modifier, contentPadding = PaddingValues(Spacing.md)) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = MaterialTheme.shapes.small,
        )
        Spacer(Modifier.height(Spacing.xs))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.75f).height(14.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(18.dp))
    }
}

// ── Misc ────────────────────────────────────────────────────────────────

/** Dots under an image carousel. */
@Composable
fun PagerDots(
    count: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
) {
    if (count <= 1) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .size(width = if (selected) 18.dp else 6.dp, height = 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) activeColor else activeColor.copy(alpha = 0.45f)
                    )
            )
        }
    }
}

/** A gradient scrim so white text stays readable over a photo. */
@Composable
fun PhotoScrim(modifier: Modifier = Modifier, height: Dp = 90.dp) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.62f))
                )
            )
    )
}
