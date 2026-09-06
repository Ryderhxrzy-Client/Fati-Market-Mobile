package com.fati_market.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.fati_market.safeAreaTopHeight
import com.fati_market.ui.theme.Elevation
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.OverlineStyle
import com.fati_market.ui.theme.PriceStyle
import com.fati_market.ui.theme.PriceStyleLarge
import com.fati_market.ui.theme.PriceStyleSmall
import com.fati_market.ui.theme.Spacing
import com.fati_market.ui.theme.brandGradient

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
    content: @Composable ColumnScope.() -> Unit,
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
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = Elevation.card),
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
    content: @Composable ColumnScope.() -> Unit,
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

/**
 * A titled group of rows - a settings block, a profile section.
 *
 * The rows themselves are supplied by the caller; [SettingsRow] and
 * [InfoRowItem] are the usual occupants, with [RowDivider] between them.
 */
@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Overline(title, modifier = Modifier.padding(start = Spacing.xs))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = Elevation.flat,
            shadowElevation = Elevation.card,
        ) {
            Column(content = content)
        }
    }
}

/** The hairline between rows inside a [SettingsGroup]. */
@Composable
fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 62.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
    )
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
 * The header for a page reached by going *back*: a detail, a dialog, checkout.
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
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navigationContentDescription: String = "Back",
    actions: @Composable RowScope.() -> Unit = {},
) {
    MarketHeader(
        title = title,
        modifier = modifier,
        onBack = onBack,
        navigationIcon = navigationIcon,
        navigationContentDescription = navigationContentDescription,
        actions = actions,
    )
}

/**
 * The header for a tab root or a drawer page: menu on the left (or back,
 * when [onBack] is given), a title, and up to two actions on the right.
 *
 * Deliberately slim. Everything that used to crowd the old header - the
 * wallet chip, the favourites badge, the bell - now lives where it belongs:
 * points on the home hero and the profile, favourites and notifications as
 * plain actions only on the screens that need them.
 */
@Composable
fun MarketHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onMenuClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navigationContentDescription: String = "Back",
    actions: @Composable RowScope.() -> Unit = {},
    /** Extra content drawn below the title row, still on the brand gradient. */
    bottomContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val accents = LocalMarketAccents.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(brandGradient()),
    ) {
        Spacer(Modifier.safeAreaTopHeight())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                onBack != null -> IconButton(onClick = onBack) {
                    Icon(navigationIcon, contentDescription = navigationContentDescription, tint = accents.onBrand)
                }
                onMenuClick != null -> IconButton(onClick = onMenuClick) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = accents.onBrand)
                }
                else -> Spacer(Modifier.width(Spacing.md))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = accents.onBrand,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = accents.onBrandMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            actions()
        }

        if (bottomContent != null) {
            bottomContent()
        }
    }
}

/**
 * An icon action for a [MarketHeader]: white glyph, optional count badge.
 */
@Composable
fun HeaderAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: Int = 0,
) {
    val accents = LocalMarketAccents.current

    Box(modifier = modifier) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = contentDescription, tint = accents.onBrand)
        }
        if (badge > 0) {
            CountBadge(
                count = badge,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 6.dp),
            )
        }
    }
}

/** The little red counter that sits on an icon. */
@Composable
fun CountBadge(count: Int, modifier: Modifier = Modifier) {
    if (count <= 0) return

    Box(
        modifier = modifier
            .height(17.dp)
            .background(MaterialTheme.colorScheme.error, CircleShape)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            fontSize = 9.sp,
            lineHeight = 9.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onError,
            textAlign = TextAlign.Center,
        )
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
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onAction)
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ── Navigation ──────────────────────────────────────────────────────────

/** One slot in the [MarketBottomBar]. */
data class BottomTab(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val badge: Int = 0,
)

/**
 * The tab bar shared by the student and admin shells.
 *
 * Four tabs around a raised centre action - "Sell" for a student, "Scan"
 * for the store. The profile slot draws the user's avatar instead of an
 * icon, so the bar itself says who is signed in.
 */
@Composable
fun MarketBottomBar(
    tabs: List<BottomTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    centerLabel: String,
    centerIcon: ImageVector,
    onCenter: () -> Unit,
    modifier: Modifier = Modifier,
    centerSelected: Boolean = false,
    profileIndex: Int = tabs.lastIndex,
    profilePicture: String = "",
    profileInitial: String = "",
) {
    require(tabs.size == 4) { "MarketBottomBar lays out exactly four tabs around the centre action" }

    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = Elevation.flat,
            shadowElevation = Elevation.bar,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEachIndexed { index, tab ->
                        if (index == 2) Spacer(Modifier.weight(1f))

                        BottomBarItem(
                            tab = tab,
                            selected = selectedIndex == index && !centerSelected,
                            modifier = Modifier.weight(1f),
                            avatar = if (index == profileIndex) {
                                { tint ->
                                    Avatar(
                                        url = profilePicture,
                                        initial = profileInitial,
                                        size = 26.dp,
                                        ringColor = if (selectedIndex == index && !centerSelected) tint else Color.Transparent,
                                    )
                                }
                            } else null,
                            onClick = { onSelect(index) },
                        )
                    }
                }
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }

        // The centre action floats above the bar with a surface-coloured ring
        // so it reads as cut out of the bar rather than pasted onto it.
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FloatingActionButton(
                onClick = onCenter,
                modifier = Modifier
                    .size(56.dp)
                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = Elevation.floating,
                    pressedElevation = Elevation.bar,
                ),
            ) {
                Icon(centerIcon, contentDescription = centerLabel, modifier = Modifier.size(26.dp))
            }
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (centerSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (centerSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    tab: BottomTab,
    selected: Boolean,
    modifier: Modifier = Modifier,
    avatar: (@Composable (tint: Color) -> Unit)? = null,
    onClick: () -> Unit,
) {
    val tint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tabTint",
    )
    val pill by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "tabPill",
    )

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box {
            Box(
                modifier = Modifier
                    .background(pill, CircleShape)
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (avatar != null) {
                    avatar(tint)
                } else {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = tab.label,
                        tint = tint,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            if (tab.badge > 0) {
                CountBadge(
                    count = tab.badge,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp),
                )
            }
        }
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = tint,
            modifier = Modifier.padding(top = 3.dp),
            maxLines = 1,
        )
    }
}

/**
 * A row in a navigation drawer.
 */
@Composable
fun DrawerRow(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    badge: Int = 0,
) {
    val container = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val content = if (selected) tint else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xxs)
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = content,
            modifier = Modifier.weight(1f),
        )
        if (badge > 0) CountBadge(badge)
    }
}

// ── People ──────────────────────────────────────────────────────────────

/**
 * A round avatar: the photo when there is one, the initial when there is
 * not, and a shimmer in between.
 */
@Composable
fun Avatar(
    url: String,
    initial: String,
    size: Dp,
    modifier: Modifier = Modifier,
    ringColor: Color = Color.Transparent,
    ringWidth: Dp = 2.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    val fallbackSize = (size.value * 0.42f).sp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(ringWidth, ringColor, CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isNotBlank()) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                loading = { ShimmerBox(Modifier.fillMaxSize(), shape = CircleShape) },
                error = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            initial.take(1).uppercase(),
                            fontSize = fallbackSize,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                        )
                    }
                },
            )
        } else {
            Text(
                initial.take(1).uppercase(),
                fontSize = fallbackSize,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
        }
    }
}

// ── Inputs ──────────────────────────────────────────────────────────────

/**
 * The app's text field: the Material outlined field with the corner radius
 * and colours settled once here, so every form looks the same.
 */
@Composable
fun MarketTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let {
            { Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        },
        trailingIcon = trailingIcon,
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        readOnly = readOnly,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

/**
 * A pill-shaped search box.
 *
 * Drawn on a surface with a soft shadow so it can float over the brand
 * gradient on the home hero, or sit on the plain background of a list page.
 */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onClear: () -> Unit = { onValueChange("") },
    onSearch: () -> Unit = {},
    elevated: Boolean = true,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (elevated) Elevation.raised else Elevation.flat,
        border = if (elevated) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .padding(start = Spacing.lg, end = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(Spacing.sm))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        inner()
                    }
                },
            )
            if (value.isNotEmpty()) {
                IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/**
 * A selectable chip - categories, filters, tabs-as-chips.
 *
 * Filled with the primary colour when selected, tonal when not, so a row of
 * them reads as one control rather than a row of buttons.
 */
@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    count: Int? = null,
) {
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainer,
        label = "chipContainer",
    )
    val content = if (selected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = CircleShape,
        color = container,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = content,
                maxLines = 1,
            )
            if (count != null && count > 0) {
                Box(
                    modifier = Modifier
                        .background(
                            if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)
                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                            CircleShape,
                        )
                        .padding(horizontal = 7.dp, vertical = 1.dp),
                ) {
                    Text(
                        if (count > 99) "99+" else count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = content,
                    )
                }
            }
        }
    }
}

// ── Tiles ───────────────────────────────────────────────────────────────

/**
 * A number with a name: dashboard statistics, the profile's points.
 */
@Composable
fun StatTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    container: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    supporting: String? = null,
) {
    val shape = MaterialTheme.shapes.medium

    Surface(
        modifier = if (onClick != null) modifier.clip(shape).clickable(onClick = onClick) else modifier,
        shape = shape,
        color = container,
        shadowElevation = Elevation.card,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(tint.copy(alpha = 0.12f), MaterialTheme.shapes.extraSmall),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (supporting != null) {
                Text(
                    supporting,
                    style = MaterialTheme.typography.labelSmall,
                    color = tint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * A square shortcut: icon in a tinted box, a short label under it.
 */
@Composable
fun QuickAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    badge: Int = 0,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(tint.copy(alpha = 0.12f), MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
            }
            if (badge > 0) {
                CountBadge(
                    count = badge,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp),
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

// ── Rows ────────────────────────────────────────────────────────────────

/**
 * A row that goes somewhere: icon, title, optional subtitle, chevron.
 *
 * Pass `trailing` to replace the chevron - a Switch, a value, a badge.
 */
@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    tint: Color = MaterialTheme.colorScheme.primary,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(tint.copy(alpha = 0.12f), MaterialTheme.shapes.extraSmall),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        when {
            trailing != null -> trailing()
            onClick != null -> Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** A label above a value, with a leading icon - the profile's identity rows. */
@Composable
fun InfoRowItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(tint.copy(alpha = 0.12f), MaterialTheme.shapes.extraSmall),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = valueColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
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
        maxLines = 1,
    )
}

/**
 * The reward preview a buyer sees on a listing.
 *
 * Gold is reserved for loyalty throughout the app, so this chip reads as
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
                    "+$points pt${if (points == 1) "" else "s"}"
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
        modifier = if (onClick != null) modifier.clip(CircleShape).clickable(onClick = onClick) else modifier,
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

/** The container/content pair for a tone, so pills and banners agree. */
@Composable
fun StatusTone.colors(): Pair<Color, Color> {
    val accents = LocalMarketAccents.current
    return when (this) {
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
}

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
    val (container, content) = tone.colors()

    Surface(modifier = modifier, shape = CircleShape, color = container) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            if (icon != null) {
                Icon(icon, null, tint = content, modifier = Modifier.size(13.dp))
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = content, maxLines = 1)
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
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    /** Shorter, for pinned strips where a full-height button crowds out the content. */
    compact: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(if (compact) 40.dp else 52.dp),
        contentPadding = if (compact) PaddingValues(horizontal = Spacing.lg) else ButtonDefaults.ContentPadding,
        enabled = enabled && !loading,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
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
        modifier = modifier.height(if (compact) 40.dp else 52.dp),
        contentPadding = if (compact) PaddingValues(horizontal = Spacing.lg) else ButtonDefaults.ContentPadding,
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

/** A tonal button - filled with the primary container, for the third-ranked action. */
@Composable
fun TonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    compact: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(if (compact) 40.dp else 52.dp),
        contentPadding = if (compact) PaddingValues(horizontal = Spacing.lg) else ButtonDefaults.ContentPadding,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
        }
        Text(
            text,
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
        )
    }
}

/** A round icon-only send/submit button. */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (enabled && !loading) containerColor else containerColor.copy(alpha = 0.5f))
            .clickable(enabled = enabled && !loading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = contentColor,
                modifier = Modifier.size(size * 0.42f),
                strokeWidth = 2.dp,
            )
        } else {
            Icon(icon, contentDescription, tint = contentColor, modifier = Modifier.size(size * 0.42f))
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
    val (container, content) = tone.colors()

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
                .size(84.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
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

/** A centred spinner with an optional line under it. */
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
            if (message != null) {
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** The screen shown when a load failed, with the one thing to do about it. */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
    onRetry: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState(
            icon = Icons.Filled.CloudOff,
            title = title,
            message = message,
            actionLabel = if (onRetry != null) "Try again" else null,
            onAction = onRetry,
        )
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
                .height(130.dp),
            shape = MaterialTheme.shapes.small,
        )
        Spacer(Modifier.height(Spacing.xs))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.75f).height(14.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(18.dp))
    }
}

/** A skeleton shaped like a list row with an avatar. */
@Composable
fun ListRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen, vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShimmerBox(Modifier.size(48.dp), shape = CircleShape)
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.weight(1f)) {
            ShimmerBox(Modifier.fillMaxWidth(0.6f).height(14.dp))
            ShimmerBox(Modifier.fillMaxWidth(0.9f).height(12.dp))
        }
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

/**
 * The brand mark: a rounded tile with the store icon, used on the splash,
 * the auth screens and the drawer.
 */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    icon: ImageVector,
    onGradient: Boolean = true,
) {
    val accents = LocalMarketAccents.current

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(
                if (onGradient) Color.White.copy(alpha = 0.16f)
                else MaterialTheme.colorScheme.primaryContainer
            )
            .border(
                1.5.dp,
                if (onGradient) accents.reward.copy(alpha = 0.9f) else Color.Transparent,
                RoundedCornerShape(size * 0.28f),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (onGradient) Color.White else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}
