package com.fati_market

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.fati_market.ui.components.Avatar
import com.fati_market.ui.components.BottomTab
import com.fati_market.ui.components.BrandMark
import com.fati_market.ui.components.ChoiceChip
import com.fati_market.ui.components.DrawerRow
import com.fati_market.ui.components.EmptyState
import com.fati_market.ui.components.ErrorState
import com.fati_market.ui.components.FittedPhoto
import com.fati_market.ui.components.HeaderAction
import com.fati_market.ui.components.InfoBanner
import com.fati_market.ui.components.ItemCardSkeleton
import com.fati_market.ui.components.ItemStatusPill
import com.fati_market.ui.components.LoadingState
import com.fati_market.ui.components.MarketBottomBar
import com.fati_market.ui.components.MarketCard
import com.fati_market.ui.components.MarketHeader
import com.fati_market.ui.components.MarketPageTopBar
import com.fati_market.ui.components.MarketTextField
import com.fati_market.ui.components.Overline
import com.fati_market.ui.components.PagerDots
import com.fati_market.ui.components.PriceSize
import com.fati_market.ui.components.PriceTag
import com.fati_market.ui.components.PrimaryButton
import com.fati_market.ui.components.RewardChip
import com.fati_market.ui.components.RoundIconButton
import com.fati_market.ui.components.SearchField
import com.fati_market.ui.components.SecondaryButton
import com.fati_market.ui.components.SectionHeader
import com.fati_market.ui.components.SoftDivider
import com.fati_market.ui.components.StatusTone
import com.fati_market.ui.components.StoreLogo
import com.fati_market.ui.components.StoreLogoIcon
import com.fati_market.ui.theme.Elevation
import com.fati_market.ui.theme.FavoriteRed
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.Spacing
import com.fati_market.ui.theme.brandGradient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit

// ── Tabs ───────────────────────────────────────────────────────────────────────

/**
 * The student's five places. Orders earned a tab of its own: it used to hide
 * in the drawer, and it is where a buyer goes right after paying.
 */
private enum class StudentTab { HOME, CHAT, ADD_ITEM, ORDERS, PROFILE }
private enum class SortOption(val label: String) {
    NEWEST("Newest"),
    PRICE_LOW_HIGH("Price: low to high"),
    PRICE_HIGH_LOW("Price: high to low"),
}

// ── HTTP client ────────────────────────────────────────────────────────────────

private val studentHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

// ── Student Dashboard ──────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentDashboard(isDarkMode: Boolean, onThemeToggle: () -> Unit, onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", 0) }

    var userFirstName    by remember { mutableStateOf(prefs.getString("user_first_name", "") ?: "") }
    var userLastName     by remember { mutableStateOf(prefs.getString("user_last_name",  "") ?: "") }
    val userEmail        = remember { prefs.getString("user_email", "") ?: "" }
    val userRole         = remember { prefs.getString("user_role", "student") ?: "student" }
    val token            = remember { prefs.getString("auth_token", "") ?: "" }
    val userWalletPoints = remember { prefs.getInt("user_wallet_points", 0) }
    var userProfilePic   by remember { mutableStateOf(prefs.getString("user_profile_picture", "") ?: "") }

    var selectedTab      by remember { mutableStateOf(StudentTab.HOME) }
    var chatConversation by remember { mutableStateOf<Conversation?>(null) }
    var showMyListings   by remember { mutableStateOf(false) }
    val tabPagerState    = rememberPagerState { StudentTab.values().size }

    // ── Global favorites state (shared across all pages) ──────────────────────
    var favoritedIds    by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showFavorites   by remember { mutableStateOf(false) }
    var selectedFavItem by remember { mutableStateOf<Item?>(null) }

    // Checkout is a full-screen overlay so the buyer is not distracted by the
    // tab bar while committing to a purchase.
    var checkoutItem    by remember { mutableStateOf<Item?>(null) }
    var showMyOrders    by remember { mutableStateOf(false) }
    val currentUserId   = remember { prefs.getInt("user_id", 0) }

    // The school account is lent: once it is disabled, an account keyed only to
    // it is unreachable. This asks once for a personal address that outlives
    // graduation, and stops asking as soon as one is linked or declined.
    var linkedPersonalEmail by remember { mutableStateOf(prefs.getString("personal_email", "").orEmpty()) }
    var promptedForEmail by remember {
        mutableStateOf(prefs.getBoolean("personal_email_prompt_hidden", false))
    }
    var askingPersonalEmail by remember { mutableStateOf(false) }

    LaunchedEffect(linkedPersonalEmail, promptedForEmail) {
        if (linkedPersonalEmail.isBlank() && !promptedForEmail) askingPersonalEmail = true
    }

    if (askingPersonalEmail) {
        PersonalEmailDialog(
            onDismiss = {
                askingPersonalEmail = false
                promptedForEmail = true
                prefs.edit().putBoolean("personal_email_prompt_hidden", true).apply()
            },
            onLinked = { linkedPersonalEmail = it; askingPersonalEmail = false },
        )
    }
    val drawerState  = rememberDrawerState(DrawerValue.Closed)
    val scope        = rememberCoroutineScope()
    val openDrawer   = { scope.launch { drawerState.open() } }

    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }

    // Back from a secondary tab returns to Home instead of exiting the app.
    // Chat detail still gets priority so its own back action remains intact.
    BackHandler(enabled = !drawerState.isOpen && (selectedTab != StudentTab.HOME || showMyListings || chatConversation != null)) {
        when {
            chatConversation != null -> chatConversation = null
            showMyListings -> showMyListings = false
            selectedTab != StudentTab.HOME -> selectedTab = StudentTab.HOME
        }
    }

    fun selectStudentTab(tab: StudentTab) {
        selectedTab = tab
        showMyListings = false
        chatConversation = null
    }

    // Keep button navigation and swipe navigation on the same page.
    LaunchedEffect(selectedTab) {
        if (tabPagerState.currentPage != selectedTab.ordinal) {
            tabPagerState.animateScrollToPage(selectedTab.ordinal)
        }
    }
    LaunchedEffect(tabPagerState.settledPage) {
        val settledTab = StudentTab.values()[tabPagerState.settledPage]
        if (settledTab != selectedTab && !showMyListings) {
            selectedTab = settledTab
            chatConversation = null
        }
    }

    LaunchedEffect(Unit) {
        requestNotificationPermissionAndRegister(context)
        favoritedIds = withContext(Dispatchers.IO) { fetchFavoriteIds(token) }
    }

    // ── Favorites overlays (accessible from any page) ─────────────────────────
    selectedFavItem?.let { item ->
        ItemDetailDialog(
            item             = item,
            token            = token,
            isFavorited      = favoritedIds.contains(item.itemId),
            onFavoriteToggle = { itemId, nowFav ->
                favoritedIds = if (nowFav) favoritedIds + itemId else favoritedIds - itemId
            },
            onGoToChat       = { selectedFavItem = null; selectedTab = StudentTab.CHAT },
            onDismiss        = { selectedFavItem = null },
            onBuyNow         = { buyItem -> selectedFavItem = null; checkoutItem = buyItem },
            currentUserId    = currentUserId
        )
    }
    // Checkout draws over the tab content so the buyer is not distracted by
    // navigation while committing to a purchase.
    checkoutItem?.let { buyItem ->
        CheckoutScreen(
            item   = buyItem,
            onBack = { checkoutItem = null },
        )
    }

    if (showMyOrders) {
        MyOrdersScreen(onDismiss = { showMyOrders = false })
    }

    if (showFavorites) {
        FavoritesScreen(
            token             = token,
            onFavoriteRemoved = { itemId -> favoritedIds = favoritedIds - itemId },
            onItemClick       = { item -> selectedFavItem = item },
            onDismiss         = { showFavorites = false }
        )
    }

    val drawerShowing = drawerState.isOpen || drawerState.targetValue == DrawerValue.Open

    ModalNavigationDrawer(
        drawerState   = drawerState,
        // Swiping the drawer open works only on Home, where no other screen
        // wants that edge. Once it is open, though, every page has to be able
        // to close it - and these same gestures are what make a tap on the
        // scrim outside the drawer close it, so they stay on while it shows.
        gesturesEnabled = drawerShowing ||
            (selectedTab == StudentTab.HOME && !showMyListings && chatConversation == null),
        drawerContent = {
            // Composed after the page, so while the drawer shows, Back closes
            // it instead of reaching the page underneath.
            BackHandler(enabled = drawerShowing) { scope.launch { drawerState.close() } }

            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 0.dp,
                drawerShape          = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                windowInsets         = WindowInsets(0),
                modifier             = Modifier.width(292.dp)
            ) {
                StudentDrawerContent(
                    onClose        = { scope.launch { drawerState.close() } },
                    showMyListings = showMyListings,
                    userFirstName  = userFirstName,
                    userLastName   = userLastName,
                    userEmail      = userEmail,
                    userProfilePic = userProfilePic,
                    favoritesCount = favoritedIds.size,
                    isDarkMode     = isDarkMode,
                    onThemeToggle  = onThemeToggle,
                    onHome         = {
                        showMyListings = false
                        selectedTab    = StudentTab.HOME
                        scope.launch { drawerState.close() }
                    },
                    onMyOrders     = {
                        selectStudentTab(StudentTab.ORDERS)
                        scope.launch { drawerState.close() }
                    },
                    onMyListings   = {
                        showMyListings = true
                        scope.launch { drawerState.close() }
                    },
                    onFavorites    = {
                        showFavorites = true
                        scope.launch { drawerState.close() }
                    },
                    onSell         = {
                        selectStudentTab(StudentTab.ADD_ITEM)
                        scope.launch { drawerState.close() }
                    },
                    onLogout       = onLogout
                )
            }
        }
    ) {
        val chatIsOpen = selectedTab == StudentTab.CHAT && chatConversation != null

        Scaffold(
            bottomBar = {
                if (!chatIsOpen) {
                    MarketBottomBar(
                        tabs = listOf(
                            BottomTab("Home", Icons.Outlined.Home, Icons.Filled.Home),
                            BottomTab("Chat", Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble),
                            BottomTab("Orders", Icons.Outlined.ReceiptLong, Icons.Filled.ReceiptLong),
                            BottomTab("Profile", Icons.Outlined.Person, Icons.Filled.Person),
                        ),
                        selectedIndex = when (selectedTab) {
                            StudentTab.HOME -> 0
                            StudentTab.CHAT -> 1
                            StudentTab.ORDERS -> 2
                            StudentTab.PROFILE -> 3
                            StudentTab.ADD_ITEM -> -1
                        },
                        onSelect = { index ->
                            selectStudentTab(
                                when (index) {
                                    0 -> StudentTab.HOME
                                    1 -> StudentTab.CHAT
                                    2 -> StudentTab.ORDERS
                                    else -> StudentTab.PROFILE
                                }
                            )
                        },
                        centerLabel = "Sell",
                        centerIcon = Icons.Filled.Add,
                        centerSelected = selectedTab == StudentTab.ADD_ITEM,
                        onCenter = { selectStudentTab(StudentTab.ADD_ITEM) },
                        profilePicture = userProfilePic,
                        profileInitial = userFirstName.firstOrNull()?.uppercaseChar()?.toString() ?: "S",
                    )
                }
            },
            contentWindowInsets = WindowInsets(0),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (chatIsOpen) 0.dp else innerPadding.calculateBottomPadding())
            ) {
                if (showMyListings) {
                    StudentMyListingsContent(
                        onMenuClick      = { openDrawer() },
                        onGoToChat       = { showMyListings = false; selectedTab = StudentTab.CHAT },
                        onSell           = { selectStudentTab(StudentTab.ADD_ITEM) },
                    )
                } else {
                    HorizontalPager(
                        state = tabPagerState,
                        modifier = Modifier.fillMaxSize(),
                        // The pager handles left/right tab swipes on every tab.
                        // ModalNavigationDrawer remains enabled on Home so its
                        // right-edge swipe can take priority for opening the drawer.
                        userScrollEnabled = !chatIsOpen
                    ) { page ->
                        when (StudentTab.values()[page]) {
                            StudentTab.HOME     -> StudentHomeContent(
                                firstName            = userFirstName,
                                onBuyNow             = { checkoutItem = it },
                                currentUserId        = currentUserId,
                                onMenuClick          = { openDrawer() },
                                favoritedIds         = favoritedIds,
                                onFavoritedIdsChange = { favoritedIds = it },
                                onFavoritesClick     = { showFavorites = true },
                                onGoToChat           = { selectedTab = StudentTab.CHAT },
                                onOpenOrders         = { selectStudentTab(StudentTab.ORDERS) },
                                onOpenProfile        = { selectStudentTab(StudentTab.PROFILE) },
                                onSell               = { selectStudentTab(StudentTab.ADD_ITEM) },
                            )
                            StudentTab.CHAT     -> AdminChatContent(
                                onMenuClick          = { openDrawer() },
                                selectedConversation = chatConversation,
                                onSelectConversation = { chatConversation = it },
                                favoritesCount       = favoritedIds.size,
                                onFavoritesClick     = { showFavorites = true },
                                isAdmin              = false
                            )
                            StudentTab.ADD_ITEM -> StudentAddItemContent(
                                onMenuClick      = { openDrawer() },
                                onItemPosted     = { selectStudentTab(StudentTab.CHAT) },
                                onOpenListings   = { showMyListings = true },
                            )
                            StudentTab.ORDERS   -> MyOrdersContent(
                                topBar = {
                                    MarketHeader(
                                        title = "My Orders",
                                        subtitle = "Payments, pickups and receipts",
                                        onMenuClick = { openDrawer() },
                                    )
                                }
                            )
                            StudentTab.PROFILE  -> AdminProfileContent(
                                onMenuClick         = { openDrawer() },
                                firstName           = userFirstName,
                                lastName            = userLastName,
                                email               = userEmail,
                                role                = userRole,
                                walletPoints        = userWalletPoints,
                                profilePic          = userProfilePic,
                                onProfilePicUpdated = { path -> userProfilePic = path },
                                favoritesCount      = favoritedIds.size,
                                onFavoritesClick    = { showFavorites = true },
                                onMyOrders          = { selectStudentTab(StudentTab.ORDERS) },
                                onMySales           = { showMyListings = true },
                                isDarkMode          = isDarkMode,
                                onThemeToggle       = onThemeToggle,
                                onLogout            = onLogout,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Home ───────────────────────────────────────────────────────────────────────

// Item lives in MarketplaceModels.kt, shared with the admin dashboard, and
// carries peso prices rather than point values.

@Composable
private fun StudentHomeContent(
    firstName: String,
    onBuyNow: (Item) -> Unit,
    currentUserId: Int,
    onMenuClick: () -> Unit,
    favoritedIds: Set<Int>,
    onFavoritedIdsChange: (Set<Int>) -> Unit,
    onFavoritesClick: () -> Unit,
    onGoToChat: () -> Unit = {},
    onOpenOrders: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onSell: () -> Unit = {},
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token   = remember { prefs.getString("auth_token", "") ?: "" }
    val scope   = rememberCoroutineScope()

    var allItems         by remember { mutableStateOf<List<Item>>(emptyList()) }
    var categories       by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading        by remember { mutableStateOf(true) }
    var errorMessage     by remember { mutableStateOf<String?>(null) }
    var searchQuery      by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Int?>(null) }
    var sortOption       by remember { mutableStateOf(SortOption.NEWEST) }
    var selectedItem     by remember { mutableStateOf<Item?>(null) }

    // Points, read once here and shared by the hero and the reward strip -
    // not polled: the balance only moves when an order completes.
    var walletPoints    by remember { mutableStateOf(prefs.getInt("user_wallet_points", 0)) }
    var isPointsVisible by remember { mutableStateOf(prefs.getBoolean("points_visibility", false)) }

    fun loadData() {
        scope.launch {
            isLoading    = true
            errorMessage = null
            try {
                // Items and categories are independent, so fetch them together
                // rather than one after the other.
                coroutineScope {
                    val itemsResult = async(Dispatchers.IO) { fetchItems(token, "public") }
                    val catsResult  = async(Dispatchers.IO) { fetchCategories(token) }
                    allItems   = itemsResult.await()
                    categories = catsResult.await()
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load items"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadData() }

    LaunchedEffect(token) {
        if (token.isBlank()) return@LaunchedEffect
        val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchWalletPoints(token) }
        if (result is MarketplaceApi.Result.Ok) {
            walletPoints = result.value
            prefs.edit().putInt("user_wallet_points", result.value).apply()
        }
    }

    LaunchedEffect(isPointsVisible) {
        prefs.edit().putBoolean("points_visibility", isPointsVisible).apply()
    }

    // "See all" under the featured listings: the whole catalog, with the sort bar.
    var showAll by remember { mutableStateOf(false) }

    val categoryMap  = remember(categories) { categories.associateBy { it.id } }
    val isFiltered   = showAll || searchQuery.isNotBlank() || selectedCategory != null || sortOption != SortOption.NEWEST

    fun clearFilters() {
        searchQuery      = ""
        selectedCategory = null
        sortOption       = SortOption.NEWEST
        showAll          = false
    }

    // Back from a filtered view returns to the home page, not out of the app.
    BackHandler(enabled = isFiltered) { clearFilters() }

    val displayItems = remember(allItems, searchQuery, selectedCategory, sortOption) {
        allItems
            .filter { item ->
                (selectedCategory == null || item.categoryId == selectedCategory) &&
                (searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true))
            }
            .let { filtered ->
                when (sortOption) {
                    SortOption.NEWEST         -> filtered
                    // Price sorting uses the public cash selling price.
                    SortOption.PRICE_LOW_HIGH -> filtered.sortedBy { Money.sortKey(it.publicPrice) }
                    SortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { Money.sortKey(it.publicPrice) }
                }
            }
    }

    // The home page's own pick, across every category: the newest arrivals.
    // The catalog already comes back newest first.
    val featuredItems = remember(allItems) { allItems.take(FEATURED_COUNT) }

    fun toggleFavorite(item: Item) {
        scope.launch {
            val nowFav = !favoritedIds.contains(item.itemId)
            val ok = withContext(Dispatchers.IO) {
                if (nowFav) addFavorite(token, item.itemId)
                else removeFavorite(token, item.itemId)
            }
            if (ok) onFavoritedIdsChange(if (nowFav) favoritedIds + item.itemId else favoritedIds - item.itemId)
        }
    }

    // ── Item detail overlay (home page items) ─────────────────────────────────
    selectedItem?.let { item ->
        ItemDetailDialog(
            item             = item,
            token            = token,
            isFavorited      = favoritedIds.contains(item.itemId),
            onFavoriteToggle = { itemId, nowFav ->
                onFavoritedIdsChange(if (nowFav) favoritedIds + itemId else favoritedIds - itemId)
            },
            onGoToChat       = { selectedItem = null; onGoToChat() },
            onDismiss        = { selectedItem = null },
            onBuyNow         = { buyItem -> selectedItem = null; onBuyNow(buyItem) },
            currentUserId    = currentUserId
        )
    }

    var showNotifications by remember { mutableStateOf(false) }
    if (showNotifications) {
        NotificationsDialog(
            onDismiss = { showNotifications = false },
            // A line saying an order moved should take the buyer to it.
            onOpenOrder = { showNotifications = false; onOpenOrders() },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HomeHero(
            firstName        = firstName,
            searchQuery      = searchQuery,
            onSearchChange   = { searchQuery = it },
            favoritesCount   = favoritedIds.size,
            onFavoritesClick = onFavoritesClick,
            onNotifications  = { showNotifications = true },
            onMenuClick      = onMenuClick,
        )

        // ── Content ───────────────────────────────────────────────────────────
        // One grid for the whole page: the category circles and the headers
        // span both columns, the listings fill them two by two.
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> LazyVerticalGrid(
                    columns               = GridCells.Fixed(2),
                    contentPadding        = PaddingValues(Spacing.screen),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalArrangement   = Arrangement.spacedBy(Spacing.md),
                    userScrollEnabled     = false,
                ) {
                    items(6) { ItemCardSkeleton() }
                }

                errorMessage != null -> ErrorState(
                    title   = "Could not load the marketplace",
                    message = errorMessage ?: "",
                    onRetry = { loadData() },
                )

                else -> LazyVerticalGrid(
                    columns               = GridCells.Fixed(2),
                    contentPadding        = PaddingValues(
                        start  = Spacing.screen,
                        end    = Spacing.screen,
                        top    = Spacing.sm,
                        bottom = Spacing.xl,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalArrangement   = Arrangement.spacedBy(Spacing.md),
                    modifier              = Modifier.fillMaxSize()
                ) {
                    // ── Points, above everything else ─────────────────────────
                    // Always shown, so the circles below never jump when a
                    // filter goes on or off.
                    item(span = { GridItemSpan(maxLineSpan) }, key = "points") {
                        PointsStrip(
                            points    = walletPoints,
                            visible   = isPointsVisible,
                            onToggle  = { isPointsVisible = !isPointsVisible },
                            onClick   = onOpenProfile,
                        )
                    }

                    // ── Categories, as round icons ────────────────────────────
                    if (categories.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "categories") {
                            CategoryCircles(
                                categories = categories,
                                selectedId = selectedCategory,
                                onSelect   = { selectedCategory = it },
                            )
                        }
                    }

                    when {
                        // ── A category, a search, or "See all" ───────────────
                        isFiltered -> {
                            item(span = { GridItemSpan(maxLineSpan) }, key = "results-header") {
                                val count = "${displayItems.size} item${if (displayItems.size == 1) "" else "s"}"
                                val scopeName = categoryMap[selectedCategory]?.name
                                    ?: if (showAll && searchQuery.isBlank()) "All items" else null

                                ResultsBar(
                                    label        = listOfNotNull(scopeName, count).joinToString(" · "),
                                    sortOption   = sortOption,
                                    onSortChange = { sortOption = it },
                                    onClear      = { clearFilters() },
                                )
                            }

                            if (displayItems.isEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }, key = "no-results") {
                                    EmptyState(
                                        icon        = Icons.Filled.SearchOff,
                                        title       = "No items match",
                                        message     = "Try a different word, or clear the filters to see everything.",
                                        actionLabel = "Clear filters",
                                        onAction    = { clearFilters() },
                                        modifier    = Modifier.padding(top = Spacing.lg),
                                    )
                                }
                            } else {
                                items(displayItems, key = { it.itemId }) { item ->
                                    PublicItemCard(
                                        item             = item,
                                        categoryName     = categoryMap[item.categoryId]?.name ?: "",
                                        isFavorited      = favoritedIds.contains(item.itemId),
                                        onFavoriteToggle = { toggleFavorite(item) },
                                        onItemClick      = { selectedItem = item }
                                    )
                                }
                            }
                        }

                        allItems.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                            EmptyState(
                                icon        = StoreLogoIcon,
                                title       = "The shelves are empty",
                                message     = "Nothing is on sale right now. Have something you no longer need? Offer it to Ofelia's Store.",
                                actionLabel = "Sell an item",
                                onAction    = onSell,
                                modifier    = Modifier.padding(top = Spacing.lg),
                            )
                        }

                        // ── The home page itself ──────────────────────────────
                        else -> {
                            // The newest arrivals across every category - not
                            // grouped by it; the circles above are for that.
                            item(span = { GridItemSpan(maxLineSpan) }, key = "featured-header") {
                                SectionHeader(
                                    title       = "Featured Listings",
                                    subtitle    = "Fresh on the shelves, from every category",
                                    actionLabel = "See all",
                                    onAction    = { showAll = true },
                                    modifier    = Modifier.padding(top = Spacing.xs),
                                )
                            }

                            items(featuredItems, key = { it.itemId }) { item ->
                                PublicItemCard(
                                    item             = item,
                                    categoryName     = categoryMap[item.categoryId]?.name ?: "",
                                    isFavorited      = favoritedIds.contains(item.itemId),
                                    onFavoriteToggle = { toggleFavorite(item) },
                                    onItemClick      = { selectedItem = item }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Home hero ──────────────────────────────────────────────────────────────────

/**
 * The top of the marketplace: a greeting, the two things a buyer reaches for
 * (favourites, notifications), and the search box floating over the bottom
 * edge of the brand gradient.
 */
@Composable
private fun HomeHero(
    firstName: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    favoritesCount: Int,
    onFavoritesClick: () -> Unit,
    onNotifications: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val accents = LocalMarketAccents.current
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
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
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = accents.onBrand)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "$greeting${if (firstName.isNotBlank()) ", $firstName" else ""}",
                        style = MaterialTheme.typography.titleLarge,
                        color = accents.onBrand,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "Find what you need for class",
                        style = MaterialTheme.typography.labelSmall,
                        color = accents.onBrandMuted,
                    )
                }
                HeaderAction(
                    icon = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favourites",
                    onClick = onFavoritesClick,
                    badge = favoritesCount,
                )
                HeaderAction(
                    icon = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    onClick = onNotifications,
                )
            }

            // Room for the search box to overlap the edge.
            Spacer(Modifier.height(34.dp))
        }

        SearchField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = "Search scrubs, books, uniforms…",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = Spacing.screen)
                .offset(y = 24.dp),
        )
    }
    // The overlap above pushes the search box below the gradient's edge, so
    // the next row needs to start beneath it.
    Spacer(Modifier.height(24.dp))
}

/**
 * "Books · 5 items · Sort: Newest" - the line above a list of results. Sits
 * inside the already padded catalog grid, so it adds no side padding of its own.
 */
@Composable
private fun ResultsBar(
    label: String,
    sortOption: SortOption,
    onSortChange: (SortOption) -> Unit,
    /** Back to the home page; the button shows only when this is set. */
    onClear: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (onClear != null) {
            TextButton(onClick = onClear) { Text("Clear") }
        }
        Box {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { expanded = true }
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Icon(
                    Icons.Filled.SwapVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    sortOption.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                SortOption.values().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = { onSortChange(option); expanded = false },
                        leadingIcon = {
                            if (sortOption == option) {
                                Icon(
                                    Icons.Filled.Check, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

/**
 * The buyer's points, in one line: the balance and what it is worth in pesos.
 * Tapping it opens the profile, where the full rewards breakdown lives.
 */
@Composable
private fun PointsStrip(
    points: Int,
    visible: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accents = LocalMarketAccents.current
    val worth = Money.format(LoyaltyRules.discountFor(points).toPlainString())

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = accents.rewardContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(accents.reward.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Stars, null, tint = accents.onRewardContainer, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (visible) "$points point${if (points == 1) "" else "s"}" else "•••• points",
                    style = MaterialTheme.typography.titleMedium,
                    color = accents.onRewardContainer,
                )
                Text(
                    if (visible) "Worth $worth off your next purchase" else "Tap the eye to show your balance",
                    style = MaterialTheme.typography.bodySmall,
                    color = accents.onRewardContainer.copy(alpha = 0.85f),
                )
            }
            IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
                Icon(
                    if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (visible) "Hide points" else "Show points",
                    tint = accents.onRewardContainer,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ── My Listings ────────────────────────────────────────────────────────────────

/**
 * Everything the student has offered to the store.
 *
 * Offers that are still open keep the full card: edit, remove, message Ofelia,
 * show the turnover QR. Everything past that point - handed over, on sale,
 * sold, or declined - stays on as the seller's history under "Recent
 * listings", in a compact card of its own.
 */
@Composable
private fun StudentMyListingsContent(
    onMenuClick: () -> Unit,
    onGoToChat: () -> Unit = {},
    onSell: () -> Unit = {},
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token   = remember { prefs.getString("auth_token", "") ?: "" }
    val scope   = rememberCoroutineScope()

    var itemList     by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading    by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editingItem  by remember { mutableStateOf<Item?>(null) }

    fun loadItems() {
        scope.launch {
            isLoading    = true
            errorMessage = null
            when (val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchMyItems(token) }) {
                is MarketplaceApi.Result.Ok      -> itemList = result.value
                is MarketplaceApi.Result.Failure -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadItems() }

    // Still open - under review, or accepted and waiting to be brought in -
    // so the seller can still act on it. The rest is history.
    val openOffers = remember(itemList) { itemList.filter { it.isPending } }
    val history    = remember(itemList) { itemList.filterNot { it.isPending } }

    editingItem?.let { item ->
        EditItemDialog(
            item      = item,
            token     = token,
            onDismiss = { editingItem = null },
            onSuccess = { editingItem = null; loadItems() }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MarketHeader(
            title = "My Listings",
            subtitle = "Items you offered to Ofelia's Store",
            onMenuClick = onMenuClick,
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> LoadingState(message = "Loading your listings…")

                errorMessage != null -> ErrorState(
                    title   = "Could not load your listings",
                    message = errorMessage ?: "",
                    onRetry = { loadItems() },
                )

                itemList.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon        = Icons.Filled.Sell,
                        title       = "Nothing listed yet",
                        message     = "Offer a scrub suit, a book or any school supply you no longer need. Ofelia reviews it and pays you in cash.",
                        actionLabel = "Sell an item",
                        onAction    = onSell,
                    )
                }

                else -> LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = Spacing.screen, vertical = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    if (openOffers.isNotEmpty()) {
                        item(key = "open-header") {
                            SectionHeader(
                                title    = "Open offers",
                                subtitle = "Waiting on Ofelia's review or on your hand-over",
                            )
                        }
                        items(openOffers, key = { it.itemId }) { item ->
                            PrivateItemCard(
                                item       = item,
                                token      = token,
                                onEdit     = { editingItem = item },
                                onDelete   = { itemList = itemList.filter { it.itemId != item.itemId } },
                                onGoToChat = onGoToChat
                            )
                        }
                    }

                    if (history.isNotEmpty()) {
                        item(key = "history-header") {
                            SectionHeader(
                                title    = "Recent listings",
                                subtitle = "What you have handed over, and what became of it",
                                modifier = Modifier.padding(top = if (openOffers.isNotEmpty()) Spacing.sm else 0.dp),
                            )
                        }
                        items(history, key = { it.itemId }) { item ->
                            ListingHistoryCard(item = item)
                        }
                    }

                    item { Spacer(Modifier.height(Spacing.sm)) }
                }
            }
        }
    }
}

// ── Public item card (2-column grid, marketplace style) ────────────────────────

@Composable
private fun PublicItemCard(
    item: Item,
    categoryName: String = "",
    modifier: Modifier = Modifier,
    isFavorited: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onItemClick: () -> Unit = {}
) {
    val shape = MaterialTheme.shapes.medium

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onItemClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = Elevation.card,
    ) {
        Column {
            // -- Photo, with the affordances floating over it --------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                if (item.photos.isNotEmpty()) {
                    FittedPhoto(
                        model = item.photos.first(),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Outlined.Image, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(38.dp).align(Alignment.Center)
                    )
                }

                // Favourite toggle
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.sm)
                        .size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shadowElevation = Elevation.card,
                    onClick = onFavoriteToggle,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorited) "Remove from favourites" else "Add to favourites",
                            tint = if (isFavorited) FavoriteRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                if (categoryName.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(Spacing.sm),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.55f)
                    ) {
                        Text(
                            categoryName,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // -- Details ---------------------------------------------------
            Column(
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    PriceTag(item.displayPrice, size = PriceSize.Small, modifier = Modifier.weight(1f, fill = false))
                    RewardChip(points = item.rewardPoints, compact = true)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    StoreLogo(Modifier.size(12.dp))
                    Text(
                        // The catalog sells the store's stock, not the
                        // consigning student's.
                        "Ofelia's Store",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ── Private item card (full-width, editable) ───────────────────────────────────

@Composable
private fun PrivateItemCard(item: Item, token: String, onEdit: () -> Unit, onDelete: () -> Unit, onGoToChat: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    var chatText          by remember { mutableStateOf("") }
    var isSendingChat     by remember { mutableStateOf(false) }
    var chatError         by remember { mutableStateOf<String?>(null) }
    var chatSent          by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isDeleting        by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon  = { Icon(Icons.Filled.DeleteOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp)) },
            title = { Text("Remove listing?") },
            text  = { Text("\"${item.title}\" will be taken off your listings. This cannot be undone.") },
            confirmButton = {
                PrimaryButton(
                    text = "Remove",
                    compact = true,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            isDeleting = true
                            val ok = withContext(Dispatchers.IO) { deleteItem(token, item.itemId) }
                            isDeleting = false
                            if (ok) onDelete()
                        }
                    },
                )
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Keep it") } }
        )
    }

    if (chatSent) {
        AlertDialog(
            onDismissRequest = { chatSent = false },
            icon  = { Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
            title = { Text("Message sent") },
            text  = { Text("Ofelia's Store will reply in your chat.") },
            confirmButton = {
                PrimaryButton(text = "Open chat", compact = true, onClick = { chatSent = false; onGoToChat() })
            },
            dismissButton = {
                TextButton(onClick = { chatSent = false }) { Text("Close") }
            }
        )
    }

    fun sendChat() {
        if (chatText.isBlank()) { chatError = "Please enter a message."; return }
        scope.launch {
            isSendingChat = true
            chatError = null
            val ok = withContext(Dispatchers.IO) {
                sendMessageToAdmin(token, item.itemId, chatText.trim())
            }
            isSendingChat = false
            if (ok) { chatSent = true; chatText = "" }
            else chatError = "Failed to send. Please try again."
        }
    }

    MarketCard(contentPadding = PaddingValues(0.dp)) {
        // Photo with the status floating over it
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            val photoUrl = item.photos.firstOrNull() ?: ""
            if (photoUrl.isNotBlank()) {
                FittedPhoto(
                    model    = photoUrl,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Outlined.Image, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(48.dp).align(Alignment.Center)
                )
            }
            ItemStatusPill(
                item.status,
                offerAccepted = item.offerAccepted,
                modifier = Modifier.align(Alignment.TopStart).padding(Spacing.md),
            )
        }

        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = Spacing.md)) {
                    Text(
                        item.title,
                        style      = MaterialTheme.typography.titleLarge,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                    )
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                // The seller sees their own asking price only. Reward points
                // are a buyer-side figure and must not appear here.
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Asking",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PriceTag(item.displayAskingPrice, size = PriceSize.Small)
                }
            }

            // Tell the seller exactly where their listing stands. Nothing
            // here mentions points - the reward is a buyer-side figure.
            when {
                // Accepted but not yet handed over: the QR window.
                item.isPending && item.offerAccepted -> {
                    InfoBanner(
                        title = "Offer accepted - ${Money.format(item.acquisitionPrice)}",
                        text  = buildString {
                            append("Bring the item to Ofelia's Store and show your QR code. ")
                            append(
                                item.meetupSchedule?.let { Dates.short(it) }
                                    ?.let { "Meet-up: $it." }
                                    ?: "Ofelia will message you the meet-up schedule."
                            )
                        },
                        tone  = StatusTone.Success,
                        icon  = Icons.Filled.CheckCircle
                    )

                    ItemQrButton(item = item, modifier = Modifier.fillMaxWidth())
                }
                item.isPending -> InfoBanner(
                    title = "Waiting for review",
                    text  = "Ofelia will message you here to agree a price and arrange " +
                            "the hand-over. You will be paid in cash once she has checked the item.",
                    tone  = StatusTone.Warning,
                    icon  = Icons.Filled.HourglassTop
                )
                item.isRejected -> InfoBanner(
                    title = "Not accepted",
                    text  = item.rejectedReason ?: "Ofelia did not accept this item.",
                    tone  = StatusTone.Danger,
                    icon  = Icons.Filled.ErrorOutline
                )
                item.sellerIsPaid -> InfoBanner(
                    title = "You have been paid",
                    text  = "${Money.format(item.sellerPayoutAmount)} was handed over for this item.",
                    tone  = StatusTone.Success,
                    icon  = Icons.Filled.CheckCircle
                )
                item.isTurnoverVerified -> InfoBanner(
                    title = "Item received",
                    text  = "Ofelia has the item. Your cash payout is being prepared.",
                    tone  = StatusTone.Info,
                    icon  = Icons.Filled.Inventory
                )
            }

            SoftDivider()

            // ── Chat to Ofelia Store ───────────────────────────────────────
            Overline("Message Ofelia's Store")
            chatError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                OutlinedTextField(
                    value           = chatText,
                    onValueChange   = { chatText = it; chatError = null; chatSent = false },
                    placeholder     = { Text("Ask about this listing…", style = MaterialTheme.typography.bodyMedium) },
                    modifier        = Modifier.weight(1f),
                    shape           = CircleShape,
                    maxLines        = 2,
                    textStyle       = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (!isSendingChat) sendChat() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                )
                RoundIconButton(
                    icon = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    onClick = { sendChat() },
                    loading = isSendingChat,
                )
            }

            // ── Action buttons ────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                SecondaryButton(
                    text = "Edit",
                    icon = Icons.Outlined.Edit,
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    compact = true,
                )
                SecondaryButton(
                    text = "Remove",
                    icon = Icons.Outlined.DeleteOutline,
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f),
                    compact = true,
                    loading = isDeleting,
                    contentColor = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

// ── Edit Item dialog ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditItemDialog(
    item: Item,
    token: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var title             by remember { mutableStateOf(item.title) }
    var description       by remember { mutableStateOf(item.description) }
    var askingPrice       by remember { mutableStateOf(Money.formatPlain(item.sellerAskingPrice).replace(",", "")) }
    var selectedCategory  by remember { mutableStateOf<Category?>(null) }
    var categories        by remember { mutableStateOf<List<Category>>(emptyList()) }
    var categoriesLoading by remember { mutableStateOf(true) }
    var newUris           by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isLoading         by remember { mutableStateOf(false) }
    var errorMessage      by remember { mutableStateOf<String?>(null) }
    var showSuccess       by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val cats = withContext(Dispatchers.IO) { fetchCategories(token) }
        categories       = cats
        selectedCategory = cats.find { it.id == item.categoryId }
        categoriesLoading = false
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        newUris = (newUris + uris).distinctBy { it.toString() }.take(5)
    }

    fun validate(): String? {
        if (title.isBlank())                        return "Title is required"
        if (description.isBlank())                  return "Description is required"
        if (selectedCategory == null)               return "Category is required"
        if (askingPrice.isBlank())                  return "Asking price is required"
        if (Money.normalizeInput(askingPrice) == null) return "Enter a valid peso amount, e.g. 200 or 199.50"
        return null
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

            if (showSuccess) {
                AlertDialog(
                    onDismissRequest = onSuccess,
                    icon    = { Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp)) },
                    title   = { Text("Listing updated") },
                    text    = { Text("Your changes have been saved.") },
                    confirmButton = {
                        PrimaryButton(text = "Done", compact = true, onClick = onSuccess)
                    }
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                MarketPageTopBar(title = "Edit listing", onBack = onDismiss)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeAreaBottom()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.screen, vertical = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    // ── Photos ────────────────────────────────────────────
                    FormSection(title = "Photos", trailing = "${newUris.size} / 5 new") {
                        if (item.photos.isNotEmpty() && newUris.isEmpty()) {
                            Text(
                                "Current photos",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                items(item.photos) { url -> PhotoTile(model = url) }
                            }
                        }
                        PhotoPickerRow(
                            uris = newUris,
                            onAdd = { photoPicker.launch("image/*") },
                            onRemove = { uri -> newUris = newUris - uri },
                            addLabel = if (newUris.isEmpty()) "Replace photos" else "Add more",
                        )
                        Text(
                            "Leave this empty to keep the current photos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // ── Details ───────────────────────────────────────────
                    FormSection(title = "Details") {
                        MarketTextField(
                            value          = title,
                            onValueChange  = { if (it.length <= 255) title = it },
                            label          = "Title",
                            leadingIcon    = Icons.Outlined.Title,
                            supportingText = "${title.length}/255",
                        )
                        MarketTextField(
                            value          = description,
                            onValueChange  = { if (it.length <= 1000) description = it },
                            label          = "Description",
                            leadingIcon    = Icons.Outlined.Description,
                            singleLine     = false,
                            minLines       = 4,
                            maxLines       = 6,
                            supportingText = "${description.length}/1000",
                        )
                        CategoryDropdown(
                            categories = categories,
                            loading = categoriesLoading,
                            selected = selectedCategory,
                            onSelect = { selectedCategory = it },
                        )
                    }

                    // ── Price ─────────────────────────────────────────────
                    FormSection(title = "Asking price") {
                        MarketTextField(
                            value           = askingPrice,
                            onValueChange   = { v -> if (Money.isValidPriceInput(v)) askingPrice = v },
                            label           = "Amount in pesos (${Money.PESO})",
                            leadingIcon     = Icons.Outlined.Payments,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        )
                    }

                    errorMessage?.let { err ->
                        InfoBanner(text = err, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
                    }

                    PrimaryButton(
                        text = "Save changes",
                        icon = Icons.Filled.Check,
                        loading = isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val err = validate()
                            if (err != null) { errorMessage = err; return@PrimaryButton }
                            errorMessage = null
                            scope.launch {
                                isLoading = true
                                try {
                                    val photoFiles = if (newUris.isNotEmpty()) {
                                        withContext(Dispatchers.IO) {
                                            newUris.mapIndexedNotNull { index, uri ->
                                                val rawMime  = context.contentResolver.getType(uri) ?: "image/jpeg"
                                                val mimeType = if (rawMime.contains("png")) "image/png" else "image/jpeg"
                                                val ext      = if (mimeType == "image/png") "png" else "jpg"
                                                val input    = context.contentResolver.openInputStream(uri) ?: return@mapIndexedNotNull null
                                                val file     = File(context.cacheDir, "edit_photo_$index.$ext")
                                                input.use { src -> file.outputStream().use { dst -> src.copyTo(dst) } }
                                                if (file.length() > 5 * 1024 * 1024) { file.delete(); return@mapIndexedNotNull null }
                                                Pair(file, mimeType)
                                            }
                                        }
                                    } else emptyList()

                                    val (success, msg) = withContext(Dispatchers.IO) {
                                        updateItem(
                                            token       = token,
                                            itemId      = item.itemId,
                                            title       = title.trim(),
                                            description = description.trim(),
                                            categoryId  = selectedCategory!!.id,
                                            askingPrice = Money.normalizeInput(askingPrice)!!,
                                            photoFiles  = photoFiles
                                        )
                                    }
                                    if (success) showSuccess = true else errorMessage = msg
                                } catch (e: Exception) {
                                    errorMessage = "Unexpected error: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                    )

                    Spacer(Modifier.height(Spacing.sm))
                }
            }
        }
    }
}

// ── Form building blocks ───────────────────────────────────────────────────────

/** A titled block of a form, so a long form reads as a few short ones. */
@Composable
private fun FormSection(
    title: String,
    trailing: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (trailing != null) {
                Text(
                    trailing,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        content()
    }
}

/** One picked photo, 92dp square. */
@Composable
private fun PhotoTile(model: Any, onRemove: (() -> Unit)? = null) {
    Box {
        AsyncImage(
            model              = model,
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .size(92.dp)
                .clip(MaterialTheme.shapes.small)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
        )
        if (onRemove != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Spacing.xs)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Close, "Remove photo", tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

/** The picked photos followed by a dashed "add" tile while there is room. */
@Composable
private fun PhotoPickerRow(
    uris: List<Uri>,
    onAdd: () -> Unit,
    onRemove: (Uri) -> Unit,
    addLabel: String = "Add photo",
    max: Int = 5,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        items(uris, key = { it.toString() }) { uri ->
            PhotoTile(model = uri, onRemove = { onRemove(uri) })
        }
        if (uris.size < max) {
            item(key = "add") {
                Column(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                        .clickable(onClick = onAdd),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.Outlined.AddAPhoto, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        addLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<Category>,
    loading: Boolean,
    selected: Category?,
    onSelect: (Category) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { if (!loading) expanded = it }
    ) {
        MarketTextField(
            value         = selected?.name ?: "",
            onValueChange = {},
            readOnly      = true,
            label         = "Category",
            placeholder   = if (loading) "Loading categories…" else "Choose a category",
            leadingIcon   = Icons.Outlined.Category,
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier      = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (categories.isEmpty()) {
                DropdownMenuItem(
                    text    = { Text("No categories available", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = { expanded = false }
                )
            } else {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text    = { Text(cat.name) },
                        onClick = { onSelect(cat); expanded = false },
                        leadingIcon = {
                            if (selected?.id == cat.id) {
                                Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        },
                    )
                }
            }
        }
    }
}

// ── Item Detail Dialog ─────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ItemDetailDialog(
    item: Item,
    token: String,
    isFavorited: Boolean,
    onFavoriteToggle: (itemId: Int, nowFav: Boolean) -> Unit,
    onGoToChat: () -> Unit,
    onDismiss: () -> Unit,
    /** Opens checkout. Absent for surfaces where buying does not apply. */
    onBuyNow: (Item) -> Unit = {},
    /** Used to stop a seller buying their own listing. */
    currentUserId: Int = 0
) {
    val scope = rememberCoroutineScope()
    var detailItem        by remember { mutableStateOf(item) }
    var isLoading         by remember { mutableStateOf(true) }
    var isFav             by remember { mutableStateOf(isFavorited) }
    var isToggling        by remember { mutableStateOf(false) }
    var showImageViewer   by remember { mutableStateOf(false) }
    var messageText       by remember { mutableStateOf("Available pa ba?") }
    var isSending         by remember { mutableStateOf(false) }
    var showSentDialog    by remember { mutableStateOf(false) }
    var sendError         by remember { mutableStateOf<String?>(null) }
    val accents = LocalMarketAccents.current

    LaunchedEffect(item.itemId) {
        val detail    = withContext(Dispatchers.IO) { fetchItemDetail(token, item.itemId) }
        val favStatus = withContext(Dispatchers.IO) { checkFavorite(token, item.itemId) }
        if (detail != null) {
            detailItem = detail
        }
        isFav     = favStatus
        isLoading = false
    }

    val photoPager = rememberPagerState(pageCount = { detailItem.photos.size.coerceAtLeast(1) })

    fun sendMessage() {
        if (messageText.isBlank()) {
            sendError = "Please enter a message first."
            return
        }
        scope.launch {
            isSending = true
            sendError = null
            val ok = withContext(Dispatchers.IO) {
                sendMessageToAdmin(token, detailItem.itemId, messageText.trim())
            }
            isSending = false
            if (ok) { showSentDialog = true; messageText = "Available pa ba?" }
            else sendError = "Failed to send. Please try again."
        }
    }

    // ── Success dialog ──────────────────────────────────────────────────────────
    if (showSentDialog) {
        AlertDialog(
            onDismissRequest = { showSentDialog = false },
            icon = {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title   = { Text("Message sent") },
            text    = { Text("Ofelia's Store will reply in your chat.") },
            confirmButton = {
                PrimaryButton(text = "Open chat", compact = true, onClick = { showSentDialog = false; onGoToChat() })
            },
            dismissButton = {
                TextButton(onClick = { showSentDialog = false }) { Text("Close") }
            }
        )
    }

    // Full-screen image viewer. Tapping the main image opens this view.
    if (showImageViewer && detailItem.photos.isNotEmpty()) {
        Dialog(
            onDismissRequest = { showImageViewer = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = detailItem.photos[photoPager.currentPage.coerceIn(0, detailItem.photos.lastIndex)],
                        contentDescription = "Full-screen item image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showImageViewer = false }
                    )
                    IconButton(
                        onClick = { showImageViewer = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .safeAreaTop()
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Filled.Close, "Close image", tint = Color.White)
                    }
                    if (detailItem.photos.size > 1) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .safeAreaBottom()
                                .padding(bottom = 24.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = CircleShape
                        ) {
                            Text(
                                "${photoPager.currentPage + 1} / ${detailItem.photos.size}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Top bar ────────────────────────────────────────────────────
                MarketPageTopBar(title = "Item details", onBack = onDismiss) {
                    if (!isLoading) {
                        IconButton(
                            onClick = {
                                if (!isToggling) {
                                    isToggling = true
                                    scope.launch {
                                        val nowFav = !isFav
                                        val ok = withContext(Dispatchers.IO) {
                                            if (nowFav) addFavorite(token, detailItem.itemId)
                                            else removeFavorite(token, detailItem.itemId)
                                        }
                                        if (ok) { isFav = nowFav; onFavoriteToggle(detailItem.itemId, nowFav) }
                                        isToggling = false
                                    }
                                }
                            }
                        ) {
                            Icon(
                                if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isFav) "Remove from favourites" else "Add to favourites",
                                tint = if (isFav) FavoriteRed else accents.onBrand
                            )
                        }
                    }
                }

                if (isLoading) {
                    LoadingState()
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // ── Images ─────────────────────────────────────────────
                        if (detailItem.photos.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                HorizontalPager(state = photoPager, modifier = Modifier.fillMaxSize()) { page ->
                                    AsyncImage(
                                        model              = detailItem.photos[page],
                                        contentDescription = null,
                                        contentScale       = ContentScale.Crop,
                                        modifier           = Modifier
                                            .fillMaxSize()
                                            .clickable { showImageViewer = true }
                                    )
                                }
                                if (detailItem.photos.size > 1) {
                                    PagerDots(
                                        count = detailItem.photos.size,
                                        selectedIndex = photoPager.currentPage,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = Spacing.md)
                                            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                                            .padding(horizontal = Spacing.sm, vertical = 5.dp),
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(Spacing.md),
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.45f),
                                ) {
                                    Icon(
                                        Icons.Filled.Fullscreen, "Open full screen",
                                        tint = Color.White,
                                        modifier = Modifier.padding(6.dp).size(18.dp),
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Image, null,
                                    tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(64.dp))
                            }
                        }

                        // ── Info ───────────────────────────────────────────────
                        Column(
                            modifier            = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                ItemStatusPill(detailItem.status)
                                RewardChip(points = detailItem.rewardPoints, compact = true)
                            }

                            Text(detailItem.title, style = MaterialTheme.typography.headlineMedium)

                            PriceTag(detailItem.displayPrice, size = PriceSize.Large)

                            // -- Availability ---------------------------------
                            // Buying itself lives on the pinned bar below. The
                            // store owns a published item, so even its
                            // original seller may buy it back.
                            if (detailItem.isReserved && detailItem.reservedByMe) {
                                InfoBanner(
                                    title = "You reserved this item",
                                    text  = "It is being held for you. Finish paying in " +
                                            "My Orders, then show your pickup code at the store.",
                                    tone = StatusTone.Success,
                                    icon = Icons.Filled.Lock
                                )
                            } else if (detailItem.isReserved) {
                                InfoBanner(
                                    text = "Someone is already checking this item out. " +
                                           "It becomes available again if their reservation expires.",
                                    tone = StatusTone.Warning,
                                    icon = Icons.Filled.Lock
                                )
                            } else if (detailItem.isSold) {
                                InfoBanner(
                                    text = "This item has been sold.",
                                    tone = StatusTone.Neutral,
                                    icon = Icons.Filled.Inventory
                                )
                            }

                            SoftDivider()

                            // Seller. Once the store has acquired the item it
                            // is the store selling it - the student who
                            // consigned it is not the counterparty a buyer
                            // deals with.
                            val storeOwned = detailItem.isAcquired || detailItem.isPublic ||
                                detailItem.isReserved || detailItem.isSold

                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                if (storeOwned) {
                                    StoreLogo(Modifier.size(40.dp), shape = CircleShape)
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Filled.Person,
                                            null,
                                            tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        if (storeOwned) "Ofelia's Store" else detailItem.sellerEmail,
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Text(
                                        if (storeOwned) "Pick up at the store on campus" else "Student seller",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            SoftDivider()

                            // Description
                            Text("Description", style = MaterialTheme.typography.titleMedium)
                            Text(
                                detailItem.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            SoftDivider()

                            // ── Send message ──────────────────────────────────
                            Text("Ask Ofelia's Store", style = MaterialTheme.typography.titleMedium)
                            sendError?.let { err ->
                                Text(
                                    text  = err,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                OutlinedTextField(
                                    value         = messageText,
                                    onValueChange = { messageText = it; sendError = null },
                                    placeholder   = { Text("Available pa ba?") },
                                    modifier      = Modifier.weight(1f),
                                    shape         = RoundedCornerShape(22.dp),
                                    maxLines      = 3,
                                    textStyle     = MaterialTheme.typography.bodyMedium,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { if (!isSending) sendMessage() }),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    ),
                                )
                                RoundIconButton(
                                    icon = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    onClick = { sendMessage() },
                                    loading = isSending,
                                    size = 48.dp,
                                )
                            }

                            Spacer(Modifier.height(Spacing.sm))
                        }

                        // ── Buy ──────────────────────────────────────────
                        // The last thing on the page rather than a bar pinned
                        // to the window, so it can never end up under the
                        // system's navigation buttons.
                        if (detailItem.isPublic) {
                            SoftDivider(Modifier.padding(horizontal = Spacing.screen))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Spacing.screen, vertical = Spacing.lg),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                            ) {
                                Column {
                                    Text(
                                        "Total",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    PriceTag(detailItem.displayPrice, size = PriceSize.Medium)
                                }
                                PrimaryButton(
                                    text = "Buy now",
                                    icon = Icons.Filled.ShoppingBag,
                                    onClick = { onBuyNow(detailItem) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        SafeAreaBottomSpacer()
                    }
                }
            }
        }
    }
}

// ── Favorites Screen ───────────────────────────────────────────────────────────

@Composable
private fun FavoritesScreen(
    token: String,
    onFavoriteRemoved: (Int) -> Unit,
    onItemClick: (Item) -> Unit,
    onDismiss: () -> Unit
) {
    var favoriteItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }
    var removingId    by remember { mutableStateOf<Int?>(null) }
    var pendingRemove by remember { mutableStateOf<Item?>(null) }
    var categories    by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            coroutineScope {
                val favs = async(Dispatchers.IO) { fetchFavoriteItems(token) }
                val cats = async(Dispatchers.IO) { fetchCategories(token) }
                favoriteItems = favs.await()
                categories = cats.await()
            }
        } catch (_: Exception) {}
        isLoading = false
    }

    val filteredItems = remember(favoriteItems, selectedCategoryId) {
        if (selectedCategoryId == null) favoriteItems
        else favoriteItems.filter { it.categoryId == selectedCategoryId }
    }
    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    // Undo-style: brief confirmation before actually removing
    pendingRemove?.let { toRemove ->
        LaunchedEffect(toRemove) {
            delay(1200)
            if (pendingRemove?.itemId == toRemove.itemId) {
                removingId = toRemove.itemId
                val ok = withContext(Dispatchers.IO) { removeFavorite(token, toRemove.itemId) }
                if (ok) {
                    favoriteItems = favoriteItems.filter { it.itemId != toRemove.itemId }
                    onFavoriteRemoved(toRemove.itemId)
                }
                removingId    = null
                pendingRemove = null
            }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                MarketPageTopBar(title = "Favourites", onBack = onDismiss)

                // ── Category Filter ───────────────────────────────────────────
                if (!isLoading && favoriteItems.isNotEmpty() && categories.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.screen, vertical = Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        item(key = "all") {
                            ChoiceChip(
                                label = "All",
                                selected = selectedCategoryId == null,
                                onClick = { selectedCategoryId = null },
                                count = favoriteItems.size,
                            )
                        }
                        items(categories.filter { cat -> favoriteItems.any { it.categoryId == cat.id } }, key = { it.id }) { cat ->
                            ChoiceChip(
                                label = cat.name,
                                selected = selectedCategoryId == cat.id,
                                onClick = {
                                    selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id
                                },
                            )
                        }
                    }
                }

                // ── Main Content Area ───────────────────────────────────────────
                when {
                    isLoading -> LoadingState(message = "Loading your favourites…")

                    favoriteItems.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Filled.Favorite,
                            title = "Nothing saved yet",
                            message = "Tap the heart on any item to keep it here for later.",
                            actionLabel = "Browse the marketplace",
                            onAction = onDismiss,
                        )
                    }

                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(filteredItems, key = { it.itemId }) { item ->
                            FavoriteItemCard(
                                item = item,
                                categoryName = categoryMap[item.categoryId]?.name ?: "",
                                isRemoving = removingId == item.itemId || pendingRemove?.itemId == item.itemId,
                                onClick = { onItemClick(item) },
                                onRemove = { pendingRemove = item }
                            )
                        }
                        item(span = { GridItemSpan(2) }) { Spacer(Modifier.safeAreaBottom().height(Spacing.lg)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteItemCard(
    item: Item,
    categoryName: String,
    isRemoving: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Box {
        PublicItemCard(
            item = item,
            categoryName = categoryName,
            isFavorited = true,
            onFavoriteToggle = { if (!isRemoving) onRemove() },
            onItemClick = { if (!isRemoving) onClick() },
        )
        AnimatedVisibility(visible = isRemoving, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Removing…",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Add Item ───────────────────────────────────────────────────────────────────

/** A catalog category. Shared with the home page's category circles. */
internal data class Category(val id: Int, val name: String)

private fun fetchCategories(token: String): List<Category> {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/categories")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    studentHttpClient.newCall(request).execute().use { response ->
        val raw = response.body?.string() ?: return emptyList()
        return try {
            // Resolve the array regardless of whether the response is wrapped in an
            // object or is a plain JSON array — preserves the exact database order.
            val arr = try {
                val obj = JSONObject(raw)
                when {
                    obj.has("data")       -> obj.getJSONArray("data")
                    obj.has("categories") -> obj.getJSONArray("categories")
                    else                  -> obj.getJSONArray("data")
                }
            } catch (_: Exception) {
                org.json.JSONArray(raw) // plain array response
            }
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                // API may use "category_id" or "id"
                val catId = if (obj.has("category_id")) obj.optInt("category_id") else obj.optInt("id")
                Category(id = catId, name = obj.optString("name"))
            }.sortedBy { it.id }
        } catch (_: Exception) { emptyList() }
    }
}

private fun fetchItems(token: String, status: String): List<Item> {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/items?status=$status")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    studentHttpClient.newCall(request).execute().use { response ->
        val raw = response.body?.string() ?: return emptyList()
        return try {
            val json = JSONObject(raw)
            val arr  = json.getJSONArray("data")
            (0 until arr.length()).map { i -> parseItem(arr.getJSONObject(i)) }
        } catch (_: Exception) { emptyList() }
    }
}

@Composable
private fun StudentAddItemContent(
    onMenuClick: () -> Unit = {},
    /**
     * Called once the listing is up. The server has already opened the item's
     * conversation with the offer, so the seller is taken straight there to
     * see it sitting with Ofelia's store.
     */
    onItemPosted: () -> Unit = {},
    onOpenListings: () -> Unit = {},
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token   = remember { prefs.getString("auth_token", "") ?: "" }
    val scope   = rememberCoroutineScope()

    var title              by remember { mutableStateOf("") }
    var description        by remember { mutableStateOf("") }
    var selectedCategory   by remember { mutableStateOf<Category?>(null) }
    var askingPrice        by remember { mutableStateOf("") }
    var selectedUris       by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var categories         by remember { mutableStateOf<List<Category>>(emptyList()) }
    var categoriesLoading  by remember { mutableStateOf(true) }
    var isLoading          by remember { mutableStateOf(false) }
    var errorMessage       by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        categories        = withContext(Dispatchers.IO) { fetchCategories(token) }
        categoriesLoading = false
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedUris = (selectedUris + uris)
            .distinctBy { it.toString() }
            .take(5)
    }

    fun validate(): String? {
        if (selectedUris.isEmpty())                  return "Add at least one photo"
        if (title.isBlank())                         return "Title is required"
        if (title.length > 255)                      return "Title must be under 255 characters"
        if (description.isBlank())                   return "Description is required"
        if (description.length > 1000)               return "Description must be under 1000 characters"
        if (selectedCategory == null)                return "Category is required"
        if (askingPrice.isBlank())                   return "Asking price is required"
        if (Money.normalizeInput(askingPrice) == null) return "Enter a valid peso amount, e.g. 200 or 199.50"
        return null
    }

    fun resetForm() {
        title            = ""
        description      = ""
        selectedCategory = null
        askingPrice      = ""
        selectedUris     = emptyList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MarketHeader(
            title = "Sell an item",
            subtitle = "Offer it to Ofelia's Store",
            onMenuClick = onMenuClick,
            actions = {
                HeaderAction(
                    icon = Icons.Outlined.ListAlt,
                    contentDescription = "My listings",
                    onClick = onOpenListings,
                )
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl),
        ) {
            // ── How it works ──────────────────────────────────────────────
            InfoBanner(
                title = "How selling works",
                text  = "Post your item with photos and an asking price. Ofelia reviews it in chat, " +
                        "you drop it off at the store, and she pays you in cash.",
                tone  = StatusTone.Brand,
                icon  = Icons.Outlined.Info,
            )

            // ── Photos ────────────────────────────────────────────────────
            FormSection(title = "Photos", trailing = "${selectedUris.size} / 5") {
                PhotoPickerRow(
                    uris = selectedUris,
                    onAdd = { photoPicker.launch("image/*") },
                    onRemove = { uri -> selectedUris = selectedUris - uri },
                )
                Text(
                    "JPG or PNG  ·  up to 5 MB each  ·  the first photo is the cover",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ── Details ───────────────────────────────────────────────────
            FormSection(title = "Details") {
                MarketTextField(
                    value         = title,
                    onValueChange = { if (it.length <= 255) title = it },
                    label         = "Title",
                    placeholder   = "e.g. Nursing scrub suit, size M",
                    leadingIcon   = Icons.Outlined.Title,
                    supportingText = "${title.length}/255",
                )
                MarketTextField(
                    value         = description,
                    onValueChange = { if (it.length <= 1000) description = it },
                    label         = "Description",
                    placeholder   = "Condition, size, edition, anything a buyer should know",
                    leadingIcon   = Icons.Outlined.Description,
                    singleLine    = false,
                    minLines      = 4,
                    maxLines      = 6,
                    supportingText = "${description.length}/1000",
                )
                CategoryDropdown(
                    categories = categories,
                    loading = categoriesLoading,
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it },
                )
            }

            // ── Asking Price ──────────────────────────────────────────────
            FormSection(title = "Asking price") {
                MarketTextField(
                    value         = askingPrice,
                    onValueChange = { v -> if (Money.isValidPriceInput(v)) askingPrice = v },
                    label         = "Amount in pesos (${Money.PESO})",
                    placeholder   = "0.00",
                    leadingIcon   = Icons.Outlined.Payments,
                    supportingText = "What you would like for it. Ofelia may offer a different price in chat.",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }

            errorMessage?.let { err ->
                InfoBanner(text = err, tone = StatusTone.Danger, icon = Icons.Filled.ErrorOutline)
            }

            // ── Submit ────────────────────────────────────────────────────
            PrimaryButton(
                text = "Post item",
                icon = Icons.Filled.Send,
                loading = isLoading,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val err = validate()
                    if (err != null) { errorMessage = err; return@PrimaryButton }
                    errorMessage = null
                    scope.launch {
                        isLoading = true
                        try {
                            val photoFiles = withContext(Dispatchers.IO) {
                                selectedUris.mapIndexedNotNull { index, uri ->
                                    // Normalise to jpeg or png — avoids sending heic/webp
                                    val rawMime = context.contentResolver.getType(uri) ?: "image/jpeg"
                                    val mimeType = if (rawMime.contains("png")) "image/png" else "image/jpeg"
                                    val ext      = if (mimeType == "image/png") "png" else "jpg"
                                    val input = context.contentResolver.openInputStream(uri)
                                        ?: return@mapIndexedNotNull null
                                    val file = File(context.cacheDir, "photo_$index.$ext")
                                    input.use { src -> file.outputStream().use { dst -> src.copyTo(dst) } }
                                    if (file.length() > 5 * 1024 * 1024) {
                                        file.delete()
                                        return@mapIndexedNotNull null
                                    }
                                    Pair(file, mimeType)
                                }
                            }
                            if (photoFiles.size < selectedUris.size) {
                                errorMessage = "One or more photos exceed 5 MB. Please remove them and try again."
                                isLoading = false
                                return@launch
                            }
                            val (success, msg) = withContext(Dispatchers.IO) {
                                submitItem(
                                    token       = token,
                                    title       = title.trim(),
                                    description = description.trim(),
                                    categoryId  = selectedCategory!!.id,
                                    askingPrice = Money.normalizeInput(askingPrice)!!,
                                    photoFiles  = photoFiles
                                )
                            }
                            if (success) {
                                // No dialog to dismiss: the offer is already
                                // in the chat, so go show it there.
                                resetForm()
                                onItemPosted()
                            } else errorMessage = msg
                        } catch (e: Exception) {
                            errorMessage = "Unexpected error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
            )

            Spacer(Modifier.height(Spacing.sm))
        }
    }
}

// ── Network ────────────────────────────────────────────────────────────────────

private fun submitItem(
    token: String,
    title: String,
    description: String,
    categoryId: Int,
    askingPrice: String,
    photoFiles: List<Pair<File, String>>
): Pair<Boolean, String> {
    val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
    builder.addFormDataPart("title", title)
    builder.addFormDataPart("description", description)
    builder.addFormDataPart("category_id", categoryId.toString())
    builder.addFormDataPart("seller_asking_price", askingPrice)
    photoFiles.forEach { (file, mimeType) ->
        builder.addFormDataPart(
            "photos[]",
            file.name,
            file.asRequestBody(mimeType.toMediaType())
        )
    }
    val body = builder.build()

    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/items")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(body)
        .build()

    studentHttpClient.newCall(request).execute().use { response ->
        val raw = response.body?.string() ?: ""
        return if (response.isSuccessful) {
            Pair(true, "")
        } else {
            val msg = try {
                val json   = JSONObject(raw)
                val errors = json.optJSONObject("errors")
                if (errors != null) {
                    val msgs = mutableListOf<String>()
                    errors.keys().forEach { key ->
                        errors.optJSONArray(key)?.let { arr ->
                            (0 until arr.length()).forEach { i -> msgs.add(arr.getString(i)) }
                        }
                    }
                    if (msgs.isNotEmpty()) msgs.joinToString("\n")
                    else json.optString("message", "Failed to post item")
                } else {
                    json.optString("message", "Failed to post item")
                }
            } catch (_: Exception) {
                if (raw.isNotBlank()) "Server error: ${raw.take(200)}" else "Failed to post item"
            }
            Pair(false, msg)
        }
    }
}

private fun updateItem(
    token: String,
    itemId: Int,
    title: String,
    description: String,
    categoryId: Int,
    askingPrice: String,
    photoFiles: List<Pair<File, String>>
): Pair<Boolean, String> {
    val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
    builder.addFormDataPart("_method", "PUT")
    builder.addFormDataPart("title", title)
    builder.addFormDataPart("description", description)
    builder.addFormDataPart("category_id", categoryId.toString())
    builder.addFormDataPart("seller_asking_price", askingPrice)
    photoFiles.forEach { (file, mimeType) ->
        builder.addFormDataPart("photos[]", file.name, file.asRequestBody(mimeType.toMediaType()))
    }
    val body = builder.build()

    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/items/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(body)
        .build()

    studentHttpClient.newCall(request).execute().use { response ->
        val raw = response.body?.string() ?: ""
        return if (response.isSuccessful) {
            Pair(true, "")
        } else {
            val msg = try {
                val json   = JSONObject(raw)
                val errors = json.optJSONObject("errors")
                if (errors != null) {
                    val msgs = mutableListOf<String>()
                    errors.keys().forEach { key ->
                        errors.optJSONArray(key)?.let { arr ->
                            (0 until arr.length()).forEach { i -> msgs.add(arr.getString(i)) }
                        }
                    }
                    if (msgs.isNotEmpty()) msgs.joinToString("\n")
                    else json.optString("message", "Failed to update item")
                } else {
                    json.optString("message", "Failed to update item")
                }
            } catch (_: Exception) {
                if (raw.isNotBlank()) "Server error: ${raw.take(200)}" else "Failed to update item"
            }
            Pair(false, msg)
        }
    }
}

private fun deleteItem(token: String, itemId: Int): Boolean {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/items/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .delete()
        .build()
    return try {
        studentHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

// ── Favorites network ──────────────────────────────────────────────────────────

private fun fetchFavoriteIds(token: String): Set<Int> {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/favorites")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    studentHttpClient.newCall(request).execute().use { response ->
        val raw = response.body?.string() ?: return emptySet()
        return try {
            val arr = try {
                JSONObject(raw).getJSONArray("data")
            } catch (_: Exception) {
                org.json.JSONArray(raw)
            }
            (0 until arr.length()).mapNotNull { i ->
                arr.optJSONObject(i)?.optInt("item_id")
            }.toSet()
        } catch (_: Exception) { emptySet() }
    }
}

private fun fetchFavoriteItems(token: String): List<Item> {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/favorites")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    studentHttpClient.newCall(request).execute().use { response ->
        val raw = response.body?.string() ?: return emptyList()
        return try {
            val arr = try {
                JSONObject(raw).getJSONArray("data")
            } catch (_: Exception) {
                org.json.JSONArray(raw)
            }
            (0 until arr.length()).mapNotNull { i ->
                arr.optJSONObject(i)?.let { parseItem(it) }
            }
        } catch (_: Exception) { emptyList() }
    }
}

private fun addFavorite(token: String, itemId: Int): Boolean {
    val body = "{\"item_id\":$itemId}".toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/favorites")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(body)
        .build()
    return try {
        studentHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

private fun removeFavorite(token: String, itemId: Int): Boolean {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/favorites/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .delete()
        .build()
    return try {
        studentHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

private fun checkFavorite(token: String, itemId: Int): Boolean {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/favorites/$itemId/check")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    return try {
        studentHttpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string() ?: return false
            try {
                val json = JSONObject(raw)
                json.optBoolean("is_favorited", false)
                    || json.optJSONObject("data")?.optBoolean("is_favorited", false) ?: false
            } catch (_: Exception) { false }
        }
    } catch (_: Exception) { false }
}

private fun fetchItemDetail(token: String, itemId: Int): Item? {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/items/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    return try {
        studentHttpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string() ?: return null
            val json = JSONObject(raw)
            parseItem(json.optJSONObject("data") ?: json)
        }
    } catch (_: Exception) { null }
}

private fun sendMessageToAdmin(token: String, itemId: Int, message: String): Boolean {
    val payload = "{\"receiver_id\":1,\"message\":${JSONObject.quote(message)}}"
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/messages/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .post(payload.toRequestBody("application/json".toMediaType()))
        .build()
    return try {
        studentHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

// ── Student Drawer ─────────────────────────────────────────────────────────────

@Composable
private fun StudentDrawerContent(
    /** The X in the header: shuts the drawer without choosing anything. */
    onClose: () -> Unit,
    showMyListings: Boolean,
    userFirstName: String,
    userLastName: String,
    userEmail: String,
    userProfilePic: String,
    favoritesCount: Int,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onHome: () -> Unit,
    onMyListings: () -> Unit,
    /** Opens the order history, which is also where receipts live. */
    onMyOrders: () -> Unit,
    onFavorites: () -> Unit,
    onSell: () -> Unit,
    onLogout: () -> Unit
) {
    val accents  = LocalMarketAccents.current
    val fullName = "$userFirstName $userLastName".trim().ifBlank { "Student" }
    val initial  = userFirstName.firstOrNull()?.uppercaseChar()?.toString() ?: "S"

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp)) },
            title = { Text("Log out?") },
            text  = { Text("You will need to sign in again to browse and sell.") },
            confirmButton = {
                PrimaryButton(
                    text = "Log out",
                    compact = true,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    onClick = { showLogoutDialog = false; onLogout() },
                )
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxHeight().navigationBarsPadding()) {
        // ── Header ─────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(brandGradient())
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.xl),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(
                        url = userProfilePic,
                        initial = initial,
                        size = 56.dp,
                        ringColor = Color.White.copy(alpha = 0.5f),
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White,
                    )
                    Spacer(Modifier.width(Spacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            fullName,
                            style = MaterialTheme.typography.titleMedium,
                            color = accents.onBrand,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            userEmail.ifBlank { "Student" },
                            style = MaterialTheme.typography.bodySmall,
                            color = accents.onBrandMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Close menu", tint = accents.onBrand)
                    }
                }
                Spacer(Modifier.height(Spacing.md))
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.16f)) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        Icon(Icons.Filled.School, null, tint = accents.onBrand, modifier = Modifier.size(14.dp))
                        Text(
                            "OLFU student",
                            style = MaterialTheme.typography.labelSmall,
                            color = accents.onBrand,
                        )
                    }
                }
            }
        }

        // ── Navigation items (scroll if the screen is short) ──────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            DrawerRow(icon = StoreLogoIcon, label = "Marketplace", selected = !showMyListings, onClick = onHome)
            DrawerRow(icon = Icons.Outlined.Sell, label = "Sell an item", selected = false, onClick = onSell)
            DrawerRow(icon = Icons.Outlined.ListAlt, label = "My listings", selected = showMyListings, onClick = onMyListings)
            DrawerRow(icon = Icons.Outlined.ReceiptLong, label = "My orders", selected = false, onClick = onMyOrders)
            DrawerRow(icon = Icons.Outlined.FavoriteBorder, label = "Favourites", selected = false, onClick = onFavorites, badge = favoritesCount)

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                color    = MaterialTheme.colorScheme.outlineVariant
            )

            // ── Appearance ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.xxs)
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onThemeToggle)
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Icon(
                    if (isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    "Dark mode",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = isDarkMode, onCheckedChange = { onThemeToggle() })
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            color    = MaterialTheme.colorScheme.outlineVariant
        )

        // ── Logout ─────────────────────────────────────────────────────────────
        DrawerRow(
            icon = Icons.AutoMirrored.Outlined.Logout,
            label = "Log out",
            selected = false,
            onClick = { showLogoutDialog = true },
            tint = MaterialTheme.colorScheme.error,
        )

        Spacer(modifier = Modifier.height(Spacing.lg))
    }
}
