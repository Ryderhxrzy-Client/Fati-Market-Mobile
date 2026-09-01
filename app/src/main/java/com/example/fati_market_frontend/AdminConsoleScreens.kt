package com.fati_market

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The two admin drawer pages that were never built.
 *
 * Both used to fall through to the "Coming soon" placeholder: categories could
 * only be read by the API, and the activity endpoint did not exist at all. The
 * server now serves both, and these are the screens for them.
 */

// ── Categories ───────────────────────────────────────────────────────────────

/**
 * Manage the categories every item is filed under.
 *
 * A category with items in it cannot be deleted - the server refuses, because
 * deleting would drop those listings out of every filter - so the row says what
 * it holds before the admin reaches for the bin.
 */
@Composable
internal fun AdminCategoriesContent(
    onMenuClick: () -> Unit,
    onShowBottomBarChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token = remember { prefs.getString("auth_token", "") ?: "" }
    val scope = rememberCoroutineScope()

    var categories by remember { mutableStateOf<List<MarketCategory>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    // null = closed, a category = editing it, EMPTY_CATEGORY = adding one.
    var editing by remember { mutableStateOf<MarketCategory?>(null) }
    var confirmDelete by remember { mutableStateOf<MarketCategory?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refreshKey) {
        loading = true

        when (val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchCategories(token) }) {
            is MarketplaceApi.Result.Ok -> {
                categories = result.value
                error = null
            }
            is MarketplaceApi.Result.Failure -> error = result.message
        }

        loading = false
    }

    LaunchedEffect(Unit) { onShowBottomBarChange(true) }

    editing?.let { target ->
        CategoryEditorDialog(
            category = target,
            token = token,
            onDismiss = { editing = null },
            onSaved = { saved ->
                editing = null
                notice = if (target.categoryId == 0) {
                    "\"${saved.name}\" added."
                } else {
                    "Saved as \"${saved.name}\"."
                }
                refreshKey++
            },
        )
    }

    confirmDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            icon = { Icon(Icons.Filled.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete \"${target.name}\"?") },
            text = {
                Text(
                    if (target.itemCount > 0) {
                        "This category still holds ${target.itemCount} item(s). Move them " +
                            "somewhere else first - the server will refuse until then."
                    } else {
                        "Nothing is filed under it, so it can go."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val doomed = target
                        confirmDelete = null

                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                MarketplaceApi.deleteCategory(token, doomed.categoryId)
                            }

                            when (result) {
                                is MarketplaceApi.Result.Ok -> {
                                    notice = "\"${doomed.name}\" deleted."
                                    refreshKey++
                                }
                                is MarketplaceApi.Result.Failure -> error = result.message
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("Cancel") }
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AdminPageHeader(title = "Categories", onMenuClick = onMenuClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            PrimaryButton(
                text = "Add a category",
                onClick = { editing = EMPTY_CATEGORY },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Filled.Add,
            )

            notice?.let {
                InfoBanner(text = it, tone = StatusTone.Success, icon = Icons.Filled.CheckCircle)
            }

            error?.let {
                InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
            }
        }

        when {
            loading -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.screen),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                repeat(5) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        shape = MaterialTheme.shapes.medium,
                    )
                }
            }

            categories.isEmpty() -> EmptyState(
                icon = Icons.Filled.Category,
                title = "No categories yet",
                message = "Add the first one and it appears in every item picker.",
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.screen),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(categories, key = { it.categoryId }) { category ->
                    MarketCard {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(category.name, style = MaterialTheme.typography.titleMedium)

                                category.description?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                Text(
                                    if (category.itemCount == 1) {
                                        "1 item"
                                    } else {
                                        "${category.itemCount} items"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            IconButton(onClick = { editing = category; notice = null }) {
                                Icon(Icons.Filled.Edit, "Edit ${category.name}")
                            }

                            IconButton(onClick = { confirmDelete = category; notice = null }) {
                                Icon(
                                    Icons.Filled.DeleteOutline,
                                    "Delete ${category.name}",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** The sentinel the editor reads as "this one does not exist yet". */
private val EMPTY_CATEGORY = MarketCategory(categoryId = 0, name = "", description = null)

/** Add or rename, in one dialog - the fields are the same either way. */
@Composable
private fun CategoryEditorDialog(
    category: MarketCategory,
    token: String,
    onDismiss: () -> Unit,
    onSaved: (MarketCategory) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val isNew = category.categoryId == 0

    var name by remember(category.categoryId) { mutableStateOf(category.name) }
    var description by remember(category.categoryId) { mutableStateOf(category.description ?: "") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(if (isNew) "Add a category" else "Edit category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Name") },
                    placeholder = { Text("e.g. Textbooks") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; error = null },
                    label = { Text("Description (optional)") },
                    placeholder = { Text("What belongs in here") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                )

                error?.let {
                    InfoBanner(text = it, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !saving && name.isNotBlank(),
                onClick = {
                    scope.launch {
                        saving = true
                        error = null

                        val result = withContext(Dispatchers.IO) {
                            if (isNew) {
                                MarketplaceApi.createCategory(token, name.trim(), description)
                            } else {
                                MarketplaceApi.updateCategory(
                                    token,
                                    category.categoryId,
                                    name.trim(),
                                    description,
                                )
                            }
                        }

                        saving = false

                        when (result) {
                            is MarketplaceApi.Result.Ok -> onSaved(result.value)
                            // The server owns the rules - a duplicate name, a
                            // length limit - so its wording is what is shown.
                            is MarketplaceApi.Result.Failure -> error = result.message
                        }
                    }
                },
            ) { Text(if (saving) "Saving..." else "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") }
        },
    )
}

// ── Activity ─────────────────────────────────────────────────────────────────

/**
 * What has been happening in the store.
 *
 * The server assembles this from the rows that already record each moment, so
 * the feed is complete from the store's first day rather than starting empty
 * on the day the page shipped.
 */
@Composable
internal fun AdminActivityContent(
    onMenuClick: () -> Unit,
    onShowBottomBarChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val accents = LocalMarketAccents.current
    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token = remember { prefs.getString("auth_token", "") ?: "" }

    var entries by remember { mutableStateOf<List<ActivityEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var filter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refreshKey) {
        loading = true

        when (val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchActivity(token) }) {
            is MarketplaceApi.Result.Ok -> {
                entries = result.value
                error = null
            }
            is MarketplaceApi.Result.Failure -> error = result.message
        }

        loading = false
    }

    LaunchedEffect(Unit) { onShowBottomBarChange(true) }

    val visible = remember(entries, filter) {
        if (filter == null) entries else entries.filter { it.resourceType == filter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AdminPageHeader(title = "Activity Logs", onMenuClick = onMenuClick)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            listOf(null to "All", "item" to "Items", "order" to "Orders", "points" to "Points")
                .forEach { (value, label) ->
                    FilterChip(
                        selected = filter == value,
                        onClick = { filter = value },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    )
                }
        }

        when {
            loading -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.screen),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                repeat(6) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = MaterialTheme.shapes.medium,
                    )
                }
            }

            error != null -> EmptyState(
                icon = Icons.Filled.CloudOff,
                title = "Could not load the activity",
                message = error!!,
                actionLabel = "Try again",
                onAction = { refreshKey++ },
            )

            visible.isEmpty() -> EmptyState(
                icon = Icons.Filled.History,
                title = "Nothing here yet",
                message = "Listings, orders and points all show up on this page as they happen.",
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.screen),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(visible) { entry ->
                    val (icon, tint) = when (entry.action) {
                        "create" -> Icons.Filled.AddCircleOutline to accents.info
                        "purchase" -> Icons.Filled.ShoppingCart to accents.success
                        "delete" -> Icons.Filled.Cancel to MaterialTheme.colorScheme.error
                        else -> Icons.Filled.EditNote to accents.reward
                    }

                    MarketCard {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.description, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    entry.user +
                                        (Dates.short(entry.timestamp)?.let { " · $it" } ?: ""),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            StatusPill(
                                label = entry.resourceType.replaceFirstChar { it.uppercaseChar() },
                                tone = StatusTone.Neutral,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Notifications ────────────────────────────────────────────────────────────

/**
 * What the bell opens.
 *
 * Both bells - the admin marketplace header and the student one - used to be
 * `onClick = { }`. Push notifications and the in-app banner already worked, but
 * a banner is gone the moment it is dismissed, so anything that arrived while
 * the app was closed was simply lost. This is the standing list of it.
 *
 * It reads what the app already records rather than a new inbox table: Admin
 * gets the store's activity feed, and a student gets the state of their own
 * orders, which is what their notifications are always about.
 */
@Composable
internal fun NotificationsDialog(
    onDismiss: () -> Unit,
    /**
     * Where an order notification leads. A line saying an order moved is only
     * half an answer if it cannot be opened; passing null leaves the rows as
     * plain reading, which is what Admin gets - they have the orders screen.
     */
    onOpenOrder: ((Int) -> Unit)? = null,
) {
    val context = LocalContext.current
    val accents = LocalMarketAccents.current
    val prefs = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token = remember { prefs.getString("auth_token", "") ?: "" }
    val isAdmin = remember { (prefs.getString("user_role", "") ?: "").equals("admin", true) }

    var lines by remember { mutableStateOf<List<ActivityEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isAdmin) {
        loading = true

        val result = withContext(Dispatchers.IO) {
            if (isAdmin) {
                MarketplaceApi.fetchActivity(token, limit = 50)
            } else {
                // A buyer's notifications are their orders moving: the same
                // rows My Orders draws, read as a timeline.
                when (val mine = MarketplaceApi.fetchMyTransactions(token)) {
                    is MarketplaceApi.Result.Ok -> MarketplaceApi.Result.Ok(
                        mine.value.map { order ->
                            ActivityEntry(
                                action = if (order.isTerminal) "update" else "purchase",
                                user = paymentMethodLabel(order.paymentMethod),
                                description = order.itemTitle + " · " +
                                    transactionStatusLabel(order.status),
                                resourceType = "order",
                                resourceId = order.transactionId,
                                timestamp = order.completedAt ?: order.createdAt,
                            )
                        },
                    )
                    is MarketplaceApi.Result.Failure -> mine
                }
            }
        }

        when (result) {
            is MarketplaceApi.Result.Ok -> { lines = result.value; error = null }
            is MarketplaceApi.Result.Failure -> error = result.message
        }

        loading = false
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                MarketPageTopBar(
                    title = "Notifications",
                    onBack = onDismiss,
                    navigationIcon = Icons.Filled.Close,
                    navigationContentDescription = "Close",
                )

                when {
                    loading -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.screen),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        repeat(5) {
                            ShimmerBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp),
                                shape = MaterialTheme.shapes.medium,
                            )
                        }
                    }

                    error != null -> EmptyState(
                        icon = Icons.Filled.CloudOff,
                        title = "Could not load your notifications",
                        message = error!!,
                    )

                    lines.isEmpty() -> EmptyState(
                        icon = Icons.Filled.NotificationsNone,
                        title = "Nothing to catch up on",
                        message = if (isAdmin) {
                            "Listings, orders and points all appear here as they happen."
                        } else {
                            "Updates about your orders will show up here."
                        },
                    )

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.screen),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        items(lines) { line ->
                            val handler = onOpenOrder

                            val open: (() -> Unit)? =
                                if (handler != null && line.resourceType == "order") {
                                    { handler(line.resourceId) }
                                } else {
                                    null
                                }

                            MarketCard(onClick = open) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        if (line.action == "purchase") {
                                            Icons.Filled.ShoppingCart
                                        } else {
                                            Icons.Filled.Notifications
                                        },
                                        null,
                                        tint = accents.info,
                                        modifier = Modifier.size(22.dp),
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            line.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        Text(
                                            line.user +
                                                (Dates.short(line.timestamp)?.let { " · $it" } ?: ""),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )

                                        if (open != null) {
                                            Text(
                                                "Tap to open this order",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
