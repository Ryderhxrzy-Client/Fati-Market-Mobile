package com.fati_market

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import coil.compose.SubcomposeAsyncImage
import com.fati_market.ui.components.Avatar
import com.fati_market.ui.components.BottomTab
import com.fati_market.ui.components.ChoiceChip
import com.fati_market.ui.components.DrawerRow
import com.fati_market.ui.components.EmptyState
import com.fati_market.ui.components.ErrorState
import com.fati_market.ui.components.HeaderAction
import com.fati_market.ui.components.InfoRowItem
import com.fati_market.ui.components.ListRowSkeleton
import com.fati_market.ui.components.LoadingState
import com.fati_market.ui.components.MarketBottomBar
import com.fati_market.ui.components.MarketHeader
import com.fati_market.ui.components.MarketPageTopBar
import com.fati_market.ui.components.QuickAction
import com.fati_market.ui.components.RowDivider
import com.fati_market.ui.components.SearchField
import com.fati_market.ui.components.MarketTextField
import com.fati_market.ui.components.SettingsGroup
import com.fati_market.ui.components.SettingsRow
import com.fati_market.ui.components.StatTile
import com.fati_market.ui.components.IconInfoRow
import com.fati_market.ui.components.InfoBanner
import com.fati_market.ui.components.ItemCardSkeleton
import com.fati_market.ui.components.ItemStatusPill
import com.fati_market.ui.components.MarketCard
import com.fati_market.ui.components.MarketPanel
import com.fati_market.ui.components.Overline
import com.fati_market.ui.components.PagerDots
import com.fati_market.ui.components.PhotoScrim
import com.fati_market.ui.components.PointsBalanceChip
import com.fati_market.ui.components.PriceSize
import com.fati_market.ui.components.PriceTag
import com.fati_market.ui.components.PrimaryButton
import com.fati_market.ui.components.StoreLogo
import com.fati_market.ui.components.StoreLogoIcon
import com.fati_market.ui.components.RewardChip
import com.fati_market.ui.components.RoundIconButton
import com.fati_market.ui.components.SecondaryButton
import com.fati_market.ui.components.SectionHeader
import com.fati_market.ui.components.ShimmerBox
import com.fati_market.ui.components.SoftDivider
import com.fati_market.ui.components.StatusPill
import com.fati_market.ui.components.StatusTone
import com.fati_market.ui.components.SummaryRow
import com.fati_market.ui.components.TransactionStatusPill
import com.fati_market.ui.theme.Elevation
import com.fati_market.ui.theme.FavoriteRed
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.brandGradient
import com.fati_market.ui.theme.PriceStyle
import com.fati_market.ui.theme.PriceStyleLarge
import com.fati_market.ui.theme.PriceStyleSmall
import com.fati_market.ui.theme.Spacing
import com.fati_market.ui.theme.DarkGreenLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.fati_market.auth.personalEmailStatus
// import com.pusher.client.Pusher
// import com.pusher.client.PusherOptions
// import android.util.Log
// import com.pusher.client.Authorizer
// import com.pusher.client.AuthorizationFailureException
// import com.pusher.client.channel.PrivateChannelEventListener
// import com.pusher.client.channel.PusherEvent
// import com.pusher.client.channel.SubscriptionEventListener
// import com.pusher.client.connection.ConnectionEventListener
// import com.pusher.client.connection.ConnectionState
// import com.pusher.client.connection.ConnectionStateChange
// import androidx.compose.foundation.Canvas
// import android.os.Handler
// import android.os.Looper
// import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

// ── Pusher global debug state (readable from any composable) ───────────────────
// private val pusherGlobalStatus = mutableStateOf("idle")
// private val pusherGlobalLog    = mutableStateOf("")

// ── Drawer Pages ───────────────────────────────────────────────────────────────

private sealed class DrawerPage(val label: String) {
    object Dashboard         : DrawerPage("Dashboard")
    object PrivateOffers     : DrawerPage("Private offers")
    object AcquiredItems     : DrawerPage("Acquired items")
    object PublicListings    : DrawerPage("Public listings")
    object ReservedItems     : DrawerPage("Reserved items")
    object SoldItems         : DrawerPage("Sold items")

    object PointsGiven       : DrawerPage("Points given")
    object PointsReceived    : DrawerPage("Points received")
    object CashTransactions  : DrawerPage("Cash transactions")
    /**
     * Trade stopped being a payment method: what the endpoint behind this
     * returns is orders the buyer's points covered in full.
     */
    object PointsOnlyOrders  : DrawerPage("Points-only orders")
    object TransactionHistory: DrawerPage("Transaction history")
    /** Buyer orders, with payment verification and completion. */
    object ManageOrders      : DrawerPage("Manage orders")
    object ProfitSummary     : DrawerPage("Profit summary")
    object TotalItemAcquired : DrawerPage("Items acquired")
    object TotalItemSold     : DrawerPage("Items sold")
    // object TotalProfit       : DrawerPage("Profit from markup")
    object MostSoldCategory  : DrawerPage("Most sold category")
    // object ActiveUsers       : DrawerPage("Active users")

    object Categories        : DrawerPage("Categories")
    object ActivityLogs      : DrawerPage("Activity logs")
}

private enum class AdminTab { HOME, CHAT, USERS, SETTINGS, PROFILE }

// ── Student model (fields match the API response exactly) ──────────────────────

private data class Student(
    val studentVerificationId: Int,
    val userId: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val profilePicture: String?,
    val verificationDocument: String?,
    val verificationType: String?,
    val isVerified: Boolean,
    val walletPoints: Int,
    val isActive: Boolean,
    val registeredDate: String?,
    val status: String,
    val reason: String?
) {
    val fullName: String get() = "$firstName $lastName".trim()
    val initial:  String get() = firstName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    /** Bridges cases where is_verified=true but the status field hasn't synced yet */
    val displayStatus: String get() = when {
        status.trim().lowercase() == "declined"              -> "declined"
        status.trim().lowercase() == "blocked"               -> "blocked"
        status.trim().lowercase() == "approved" || isVerified -> "approved"
        else                                                 -> "pending"
    }
}




// ── Network helpers ────────────────────────────────────────────────────────────

private val adminHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .writeTimeout(15, TimeUnit.SECONDS)
    .connectionPool(okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))
    .build()

private fun fetchStudents(token: String, status: String? = null): List<Student> {
    val url = if (status != null)
        "https://fati-api.alertaraqc.com/api/admin/students?status=$status"
    else
        "https://fati-api.alertaraqc.com/api/admin/students"

    val request = Request.Builder()
        .url(url)
        .header("Authorization", "Bearer $token")
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .get()
        .build()

    adminHttpClient.newCall(request).execute().use { response ->
        val body = response.body?.string() ?: "[]"
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}: $body")
        return parseStudents(body)
    }
}

/** Update a student's verification status. Returns true on success.
 *  action must be "approve" or "decline" — maps directly to the API endpoint path. */
private fun updateStudentStatus(
    token: String,
    userId: Int,
    action: String,       // "approve" or "decline"
    reason: String? = null
): Boolean {
    val payload = buildString {
        append("{")
        if (!reason.isNullOrBlank()) append("\"reason\":\"$reason\"")
        append("}")
    }
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/admin/students/$userId/$action")
        .header("Authorization", "Bearer $token")
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .put(payload.toRequestBody("application/json".toMediaType()))
        .build()

    adminHttpClient.newCall(request).execute().use { return it.isSuccessful }
}


/** Upload the admin's own profile picture. Returns the new picture URL on success, null otherwise. */
private fun uploadProfilePicture(token: String, file: File, mimeType: String = "image/jpeg"): String? {
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "profile_picture",
            file.name,
            file.asRequestBody(mimeType.toMediaType())
        )
        .build()

    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/profile/picture")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(requestBody)
        .build()

    adminHttpClient.newCall(request).execute().use { response ->
        val body = response.body?.string()
        if (!response.isSuccessful) return null
        if (body == null) return null
        return try {
            val json = JSONObject(body)
            // Try common response shapes
            json.strOrNull("profile_picture")
                ?: json.strOrNull("picture_url")
                ?: json.strOrNull("url")
                ?: json.optJSONObject("data")?.strOrNull("profile_picture")
                ?: json.optJSONObject("data")?.strOrNull("picture_url")
                ?: json.optJSONObject("user")?.strOrNull("profile_picture")
                // If upload succeeded but URL not in response, return empty string so caller knows success
                ?: ""
        } catch (_: Exception) { "" }
    }
}

private fun parseStudents(json: String): List<Student> {
    val list = mutableListOf<Student>()
    try {
        val root = JSONObject(json)
        val arr = when {
            root.has("data")     -> root.getJSONArray("data")
            root.has("students") -> root.getJSONArray("students")
            else                 -> null
        }
        arr?.let { for (i in 0 until it.length()) list.add(parseStudent(it.getJSONObject(i))) }
    } catch (_: Exception) {
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) list.add(parseStudent(arr.getJSONObject(i)))
        } catch (_: Exception) { /* empty */ }
    }
    return list
}

private fun JSONObject.strOrNull(key: String): String? {
    if (!has(key) || isNull(key)) return null
    return optString(key, "").takeIf { it.isNotEmpty() }
}

private fun parseStudent(obj: JSONObject) = Student(
    studentVerificationId = obj.optInt("student_verification_id", 0),
    userId                = obj.optInt("user_id", 0),
    email                 = obj.optString("email", ""),
    firstName             = obj.optString("first_name", ""),
    lastName              = obj.optString("last_name", ""),
    profilePicture        = obj.strOrNull("profile_picture"),
    verificationDocument  = obj.strOrNull("verification_document"),
    verificationType      = obj.strOrNull("verification_type"),
    isVerified            = obj.optBoolean("is_verified", false),
    walletPoints          = obj.optInt("wallet_points", 0),
    isActive              = try { obj.getInt("is_active") == 1 }
                            catch (_: Exception) { obj.optBoolean("is_active", false) },
    registeredDate        = obj.strOrNull("registered_date"),
    status                = obj.optString("status", "pending"),
    reason                = obj.strOrNull("reason")
)

/** "2026-02-25T05:53:47.000000Z" → "Feb 25, 2026" */
private fun formatDate(raw: String?): String {
    if (raw == null) return "N/A"
    return try {
        val date   = raw.split("T")[0].split("-")
        val months = listOf("","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        "${months[date[1].toInt()]} ${date[2].toInt()}, ${date[0]}"
    } catch (_: Exception) { raw }
}

/** Animated shimmer placeholder shown while an image is loading */
@Composable
private fun ShimmerEffect(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue  = -600f,
        targetValue   = 600f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceVariant
                ),
                start = Offset(translateX, 0f),
                end   = Offset(translateX + 600f, 0f)
            )
        )
    )
}


// ── Admin Dashboard ────────────────────────────────────────────────────────────

@Composable
fun AdminDashboard(isDarkMode: Boolean, onThemeToggle: () -> Unit, onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", 0) }

    val token            = remember { prefs.getString("auth_token", "") ?: "" }
    LaunchedEffect(token) {
        requestNotificationPermissionAndRegister(context)
    }
    var userFirstName    by remember { mutableStateOf(prefs.getString("user_first_name", "") ?: "") }
    var userLastName     by remember { mutableStateOf(prefs.getString("user_last_name",  "") ?: "") }
    val userEmail        = remember { prefs.getString("user_email", "") ?: "" }
    val userRole         = remember { prefs.getString("user_role", "admin") ?: "admin" }
    var userWalletPoints by remember { mutableStateOf(prefs.getInt("user_wallet_points", 0)) }
    var userProfilePic   by remember { mutableStateOf(prefs.getString("user_profile_picture", "") ?: "") }

    var selectedTab       by remember { mutableStateOf(AdminTab.HOME) }
    var drawerPage        by remember { mutableStateOf<DrawerPage?>(null) }

    // The walk-in scanner. The camera screen comes from zxing; a decoded code
    // opens the counter screen, which owns everything after that.
    // Published by the scanner and by the Complete / Mark acquired buttons
    // alike, so both land on the same counter screen.
    val scannedCode = AdminCounter.openCode
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { AdminCounter.open(it) }
    }

    fun launchScanner() {
        scanLauncher.launch(
            ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Point the camera at the buyer's pickup code")
                setBeepEnabled(false)
                // Our own activity is what keeps the scanner in portrait -
                // the library's default CaptureActivity is landscape. The
                // runtime lock is off so the manifest orientation wins.
                setCaptureActivity(PortraitCaptureActivity::class.java)
                setOrientationLocked(false)
            },
        )
    }
    var chatConversation  by remember { mutableStateOf<Conversation?>(null) }
    val showBottomBar     = remember { mutableStateOf(true) }
    val drawerState       = rememberDrawerState(DrawerValue.Closed)
    val scope             = rememberCoroutineScope()
    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }

    // The store's wallet balance. Refreshed on a slow cadence: it used to be
    // polled every five seconds by this screen *and* by every page header,
    // which kept the radio busy for a number that rarely moves.
    LaunchedEffect(token) {
        if (token.isBlank()) return@LaunchedEffect
        while (true) {
            val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchWalletPoints(token) }
            if (result is MarketplaceApi.Result.Ok) userWalletPoints = result.value
            delay(30_000)
        }
    }

    // Handle back button - redirect to dashboard when drawer or chat is open
    BackHandler(enabled = scannedCode != null) { AdminCounter.close() }

    BackHandler(enabled = drawerPage != null || (selectedTab == AdminTab.CHAT && chatConversation != null)) {
        if (drawerPage != null) {
            drawerPage = null
            selectedTab = AdminTab.HOME
        } else if (selectedTab == AdminTab.CHAT && chatConversation != null) {
            chatConversation = null
        }
    }

    fun selectAdminTab(tab: AdminTab) {
        selectedTab      = tab
        drawerPage       = null
        chatConversation = null
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Composed after the page, so while the drawer shows, Back closes
            // it instead of reaching the page underneath.
            BackHandler(enabled = drawerState.isOpen || drawerState.targetValue == DrawerValue.Open) {
                scope.launch { drawerState.close() }
            }

            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 0.dp,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                windowInsets = WindowInsets(0),
                modifier = Modifier.width(300.dp)
            ) {
                AdminDrawerContent(
                    onClose       = { scope.launch { drawerState.close() } },
                    currentPage   = drawerPage,
                    userFirstName = userFirstName,
                    userLastName  = userLastName,
                    userEmail     = userEmail,
                    userProfilePic = userProfilePic,
                    isDarkMode    = isDarkMode,
                    onThemeToggle = onThemeToggle,
                    onPageSelect  = { page ->
                        if (page == DrawerPage.Dashboard) {
                            drawerPage = null
                            selectedTab = AdminTab.HOME
                        } else {
                            drawerPage = page
                        }
                        scope.launch { drawerState.close() }
                    },
                    onScan = {
                        scope.launch { drawerState.close() }
                        launchScanner()
                    },
                    onLogout = onLogout
                )
            }
        }
    ) {
        val chatIsOpen = selectedTab == AdminTab.CHAT && chatConversation != null
        Scaffold(
            bottomBar = {
                if (!chatIsOpen && drawerPage == null && scannedCode == null && showBottomBar.value) {
                    // Students moved onto the bar: approving accounts is a
                    // daily task for the store, and it used to take two taps
                    // through the profile to reach. Settings folded into the
                    // profile to make room.
                    MarketBottomBar(
                        tabs = listOf(
                            BottomTab("Home", Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
                            BottomTab("Chat", Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble),
                            BottomTab("Students", Icons.Outlined.Group, Icons.Filled.Group),
                            BottomTab("Profile", Icons.Outlined.Person, Icons.Filled.Person),
                        ),
                        selectedIndex = when (selectedTab) {
                            AdminTab.HOME -> 0
                            AdminTab.CHAT -> 1
                            AdminTab.USERS -> 2
                            AdminTab.PROFILE, AdminTab.SETTINGS -> 3
                        },
                        onSelect = { index ->
                            selectAdminTab(
                                when (index) {
                                    0 -> AdminTab.HOME
                                    1 -> AdminTab.CHAT
                                    2 -> AdminTab.USERS
                                    else -> AdminTab.PROFILE
                                }
                            )
                        },
                        centerLabel = "Scan",
                        centerIcon = Icons.Filled.QrCodeScanner,
                        onCenter = { launchScanner() },
                        profilePicture = userProfilePic,
                        profileInitial = userFirstName.firstOrNull()?.uppercaseChar()?.toString() ?: "A",
                    )
                }
            },
            contentWindowInsets = WindowInsets(0),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (chatIsOpen || drawerPage != null || scannedCode != null || !showBottomBar.value) 0.dp else innerPadding.calculateBottomPadding())
            ) {
                if (scannedCode != null) {
                    // One scanner, two code families: an item turnover QR
                    // opens the acquire screen, an order pickup QR opens the
                    // completion screen.
                    if (scannedCode!!.startsWith("FMITEM1.")) {
                        AdminAcquireScreen(
                            scannedCode = scannedCode!!,
                            onClose = { AdminCounter.close() },
                        )
                    } else {
                        AdminScanScreen(
                            scannedCode = scannedCode!!,
                            onClose = { AdminCounter.close() },
                        )
                    }
                } else if (drawerPage != null) {
                    // Drawer pages run without the bottom bar and without the
                    // Scaffold's padding, so the inset is applied here once
                    // for all of them.
                    Box(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
                        DrawerPageContent(
                            page                 = drawerPage!!,
                            onMenuClick          = openDrawer,
                            onGoToChat           = { drawerPage = null; selectedTab = AdminTab.CHAT },
                            onNavigateToPage     = { drawerPage = it },
                            onShowBottomBarChange = { showBottomBar.value = it }
                        )
                    }
                } else {
                    when (selectedTab) {
                        AdminTab.HOME     -> AdminHomeContent(
                            onMenuClick   = openDrawer,
                            firstName     = userFirstName,
                            onOpenPage    = { drawerPage = it },
                            onOpenStudents = { selectAdminTab(AdminTab.USERS) },
                            onOpenChat    = { selectAdminTab(AdminTab.CHAT) },
                            onScan        = { launchScanner() },
                        )
                        AdminTab.CHAT     -> AdminChatContent(
                            onMenuClick          = openDrawer,
                            selectedConversation = chatConversation,
                            onSelectConversation = { chatConversation = it }
                        )
                        AdminTab.USERS    -> AdminUsersContent(onMenuClick = openDrawer)
                        AdminTab.SETTINGS -> AdminSettingsContent(isDarkMode, onThemeToggle, onMenuClick = openDrawer)
                        AdminTab.PROFILE  -> AdminProfileContent(
                            onMenuClick        = openDrawer,
                            firstName          = userFirstName,
                            lastName           = userLastName,
                            email              = userEmail,
                            role               = userRole,
                            walletPoints       = userWalletPoints,
                            profilePic         = userProfilePic,
                            onProfilePicUpdated = { path ->
                                userProfilePic = path
                            },
                            onManageOrders     = { drawerPage = DrawerPage.ManageOrders },
                            onManageStudents   = { selectAdminTab(AdminTab.USERS) },
                            isDarkMode         = isDarkMode,
                            onThemeToggle      = onThemeToggle,
                            onLogout           = onLogout,
                        )
                    }
                }
            }
        }
    }
}

// ── Drawer ─────────────────────────────────────────────────────────────────────

@Composable
private fun AdminDrawerContent(
    currentPage: DrawerPage?,
    userFirstName: String,
    userLastName: String,
    userEmail: String,
    userProfilePic: String,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onPageSelect: (DrawerPage) -> Unit,
    onScan: () -> Unit,
    onLogout: () -> Unit,
    /** The X in the header: shuts the drawer without choosing anything. */
    onClose: () -> Unit,
) {
    val accents  = LocalMarketAccents.current
    val fullName = "$userFirstName $userLastName".trim().ifBlank { "Ofelia's Store" }
    val initial  = userFirstName.firstOrNull()?.uppercaseChar()?.toString() ?: "O"

    var showLogoutDialog     by remember { mutableStateOf(false) }
    var inventoryExpanded    by remember { mutableStateOf(false) }
    var transactionsExpanded by remember { mutableStateOf(false) }
    var reportsExpanded      by remember { mutableStateOf(false) }

    // ── Logout confirmation dialog ────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(Icons.AutoMirrored.Filled.Logout, null,
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
            },
            title = { Text("Log out?") },
            text  = { Text("You will need to sign in again to manage the store.") },
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

    LaunchedEffect(currentPage) {
        when (currentPage) {
            is DrawerPage.PrivateOffers, is DrawerPage.AcquiredItems,
            is DrawerPage.PublicListings, is DrawerPage.ReservedItems,
            is DrawerPage.SoldItems -> inventoryExpanded = true

            is DrawerPage.PointsGiven, is DrawerPage.PointsReceived,
            is DrawerPage.CashTransactions, is DrawerPage.PointsOnlyOrders,
            is DrawerPage.TransactionHistory, is DrawerPage.ProfitSummary,
            is DrawerPage.ManageOrders -> transactionsExpanded = true
            is DrawerPage.TotalItemAcquired, is DrawerPage.TotalItemSold,
            is DrawerPage.MostSoldCategory -> reportsExpanded = true

            else -> {}
        }
    }

    Column(modifier = Modifier.fillMaxHeight()) {
        // Fixed header (doesn't scroll)
        Column(
            modifier = Modifier.fillMaxWidth().background(brandGradient())
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.xl, vertical = Spacing.xl),
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
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            fullName,
                            style = MaterialTheme.typography.titleMedium,
                            color = accents.onBrand,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            userEmail.ifBlank { "Administrator" },
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
                        StoreLogo(Modifier.size(14.dp))
                        Text(
                            "Store administrator",
                            style = MaterialTheme.typography.labelSmall,
                            color = accents.onBrand,
                        )
                    }
                }
            }
        }

        // Scrollable content
        Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            DrawerRow(
                icon = Icons.Outlined.Dashboard, label = "Dashboard",
                selected = currentPage == null || currentPage == DrawerPage.Dashboard,
                onClick = { onPageSelect(DrawerPage.Dashboard) },
            )
            DrawerRow(
                icon = Icons.Outlined.QrCodeScanner, label = "Scan a pickup code",
                selected = false,
                onClick = onScan,
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.outlineVariant)

            DrawerSectionHeader(Icons.Outlined.Inventory2, "Inventory", inventoryExpanded) { inventoryExpanded = !inventoryExpanded }
            AnimatedVisibility(visible = inventoryExpanded) {
                Column {
                    DrawerSubItem("Private offers",  currentPage == DrawerPage.PrivateOffers)  { onPageSelect(DrawerPage.PrivateOffers) }
                    DrawerSubItem("Acquired items",  currentPage == DrawerPage.AcquiredItems)  { onPageSelect(DrawerPage.AcquiredItems) }
                    DrawerSubItem("Public listings", currentPage == DrawerPage.PublicListings) { onPageSelect(DrawerPage.PublicListings) }
                    DrawerSubItem("Reserved items",  currentPage == DrawerPage.ReservedItems)  { onPageSelect(DrawerPage.ReservedItems) }
                    DrawerSubItem("Sold items",      currentPage == DrawerPage.SoldItems)      { onPageSelect(DrawerPage.SoldItems) }
                }
            }

            DrawerSectionHeader(Icons.Outlined.ReceiptLong, "Transactions", transactionsExpanded) { transactionsExpanded = !transactionsExpanded }
            AnimatedVisibility(visible = transactionsExpanded) {
                Column {
                    // The website's Transactions menu copies this list, word for word.
                    DrawerSubItem("Manage orders",       currentPage == DrawerPage.ManageOrders)       { onPageSelect(DrawerPage.ManageOrders) }
                    DrawerSubItem("Points given",        currentPage == DrawerPage.PointsGiven)        { onPageSelect(DrawerPage.PointsGiven) }
                    DrawerSubItem("Points received",     currentPage == DrawerPage.PointsReceived)     { onPageSelect(DrawerPage.PointsReceived) }
                    DrawerSubItem("Cash transactions",   currentPage == DrawerPage.CashTransactions)   { onPageSelect(DrawerPage.CashTransactions) }
                    DrawerSubItem("Points-only orders",  currentPage == DrawerPage.PointsOnlyOrders)  { onPageSelect(DrawerPage.PointsOnlyOrders) }
                    DrawerSubItem("Transaction history", currentPage == DrawerPage.TransactionHistory) { onPageSelect(DrawerPage.TransactionHistory) }
                    DrawerSubItem("Profit summary",      currentPage == DrawerPage.ProfitSummary)      { onPageSelect(DrawerPage.ProfitSummary) }
                }
            }

            DrawerSectionHeader(Icons.Outlined.BarChart, "Reports", reportsExpanded) { reportsExpanded = !reportsExpanded }
            AnimatedVisibility(visible = reportsExpanded) {
                Column {
                    DrawerSubItem("Items acquired",       currentPage == DrawerPage.TotalItemAcquired) { onPageSelect(DrawerPage.TotalItemAcquired) }
                    DrawerSubItem("Items sold",           currentPage == DrawerPage.TotalItemSold)     { onPageSelect(DrawerPage.TotalItemSold) }
                    // DrawerSubItem("Profit from markup",   currentPage == DrawerPage.TotalProfit)       { onPageSelect(DrawerPage.TotalProfit) }
                    DrawerSubItem("Most sold category",   currentPage == DrawerPage.MostSoldCategory)  { onPageSelect(DrawerPage.MostSoldCategory) }
                    // DrawerSubItem("Active users",         currentPage == DrawerPage.ActiveUsers)       { onPageSelect(DrawerPage.ActiveUsers) }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.outlineVariant)
            DrawerRow(icon = Icons.Outlined.Category, label = "Categories", selected = currentPage == DrawerPage.Categories, onClick = { onPageSelect(DrawerPage.Categories) })
            DrawerRow(icon = Icons.Outlined.History, label = "Activity logs", selected = currentPage == DrawerPage.ActivityLogs, onClick = { onPageSelect(DrawerPage.ActivityLogs) })

            HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.outlineVariant)

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
            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Logout, pinned at the foot of the drawer
        Column(modifier = Modifier.navigationBarsPadding()) {
            Spacer(Modifier.height(Spacing.xs))
            DrawerRow(
                icon = Icons.AutoMirrored.Outlined.Logout,
                label = "Log out",
                selected = false,
                onClick = { showLogoutDialog = true },
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun DrawerSectionHeader(icon: ImageVector, label: String, expanded: Boolean, onClick: () -> Unit) {
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "chevron")

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xxs)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Filled.ExpandMore,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = rotation },
        )
    }
}

@Composable
private fun DrawerSubItem(label: String, selected: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 44.dp, end = Spacing.md, top = 1.dp, bottom = 1.dp)
            .clip(MaterialTheme.shapes.small)
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape)
            .background(if (selected) primary else MaterialTheme.colorScheme.outline))
        Spacer(modifier = Modifier.width(Spacing.md))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DrawerPageContent(
    page: DrawerPage,
    onMenuClick: () -> Unit,
    onGoToChat: () -> Unit = {},
    onNavigateToPage: (DrawerPage) -> Unit = {},
    onShowBottomBarChange: (Boolean) -> Unit = {}
) {
    when (page) {
        DrawerPage.PrivateOffers  -> AdminPrivateOffersContent(
            onMenuClick           = onMenuClick,
            onGoToChat            = onGoToChat,
            onNavigateToPage      = onNavigateToPage,
            onShowBottomBarChange = onShowBottomBarChange
        )
        DrawerPage.AcquiredItems  -> AdminItemListContent(
            title                 = "Acquired items",
            status                = "acquired",
            emptyText             = "No acquired items at the moment.",
            showActions           = true,
            onMenuClick           = onMenuClick,
            onGoToChat            = onGoToChat,
            onNavigateToPage      = onNavigateToPage,
            onShowBottomBarChange = onShowBottomBarChange
        )
        DrawerPage.PublicListings -> AdminItemListContent(
            title                 = "Public listings",
            status                = "public",
            emptyText             = "No public listings at the moment.",
            showActions           = false,
            onMenuClick           = onMenuClick,
            onGoToChat            = onGoToChat,
            onNavigateToPage      = onNavigateToPage,
            onShowBottomBarChange = onShowBottomBarChange
        )
        DrawerPage.ReservedItems  -> AdminItemListContent(
            title                 = "Reserved items",
            status                = "reserved",
            emptyText             = "No reserved items at the moment.",
            showActions           = false,
            onMenuClick           = onMenuClick,
            onGoToChat            = onGoToChat,
            onNavigateToPage      = onNavigateToPage,
            onShowBottomBarChange = onShowBottomBarChange
        )
        DrawerPage.SoldItems      -> AdminItemListContent(
            title                 = "Sold items",
            status                = "sold",
            emptyText             = "No sold items at the moment.",
            showActions           = false,
            onMenuClick           = onMenuClick,
            onGoToChat            = onGoToChat,
            onNavigateToPage      = onNavigateToPage,
            onShowBottomBarChange = onShowBottomBarChange
        )

        DrawerPage.PointsGiven -> PointsTransactionContent(title = "Points given", endpoint = "/api/points/given", onMenuClick = onMenuClick, onGoToChat = onGoToChat, onNavigateToPage = onNavigateToPage, onShowBottomBarChange = onShowBottomBarChange)
        DrawerPage.PointsReceived -> PointsTransactionContent(title = "Points received", endpoint = "/api/points/received", onMenuClick = onMenuClick, onGoToChat = onGoToChat, onNavigateToPage = onNavigateToPage, onShowBottomBarChange = onShowBottomBarChange)
        DrawerPage.CashTransactions -> TransactionsContent(title = "Cash transactions", endpoint = "/api/admin/transactions/cash", onMenuClick = onMenuClick, onGoToChat = onGoToChat, onNavigateToPage = onNavigateToPage, onShowBottomBarChange = onShowBottomBarChange)
        DrawerPage.PointsOnlyOrders -> TransactionsContent(title = "Points-only orders", endpoint = "/api/admin/transactions/trade", onMenuClick = onMenuClick, onGoToChat = onGoToChat, onNavigateToPage = onNavigateToPage, onShowBottomBarChange = onShowBottomBarChange)
        DrawerPage.TransactionHistory -> TransactionsContent(title = "Transaction history", endpoint = "/api/admin/transactions", onMenuClick = onMenuClick, onGoToChat = onGoToChat, onNavigateToPage = onNavigateToPage, onShowBottomBarChange = onShowBottomBarChange)
        DrawerPage.ManageOrders -> AdminTransactionsContent(
            onMenuClick = onMenuClick,
            onOpenChat = { onGoToChat() }
        )
        DrawerPage.ProfitSummary -> ProfitSummaryContent(onMenuClick = onMenuClick, onGoToChat = onGoToChat, onNavigateToPage = onNavigateToPage, onShowBottomBarChange = onShowBottomBarChange)
        // Both read the inventory itself, the way the website's reports do: only
        // items in that status, with the figures that matter for it.
        DrawerPage.TotalItemAcquired -> ItemsReportContent(title = "Items acquired", status = "acquired", onMenuClick = onMenuClick)
        DrawerPage.TotalItemSold -> ItemsReportContent(title = "Items sold", status = "sold", onMenuClick = onMenuClick)
        // DrawerPage.TotalProfit -> ProfitReportContent(onMenuClick = onMenuClick, onGoToChat = onGoToChat, onNavigateToPage = onNavigateToPage, onShowBottomBarChange = onShowBottomBarChange)
        DrawerPage.MostSoldCategory -> CategoryReportContent(onMenuClick = onMenuClick, onGoToChat = onGoToChat, onNavigateToPage = onNavigateToPage, onShowBottomBarChange = onShowBottomBarChange)
        // DrawerPage.ActiveUsers -> UserReportContent(onMenuClick = onMenuClick, onGoToChat = onGoToChat, onNavigateToPage = onNavigateToPage, onShowBottomBarChange = onShowBottomBarChange)

        // Both of these used to fall through to "Coming soon" - there was no
        // API behind either of them until now.
        DrawerPage.Categories -> AdminCategoriesContent(
            onMenuClick = onMenuClick,
            onShowBottomBarChange = onShowBottomBarChange,
        )
        DrawerPage.ActivityLogs -> AdminActivityContent(
            onMenuClick = onMenuClick,
            onShowBottomBarChange = onShowBottomBarChange,
        )


        else -> Column(modifier = Modifier.fillMaxSize()) {
            AdminPageHeader(title = page.label, onMenuClick = onMenuClick)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Filled.Construction,
                    title = page.label,
                    message = "Coming soon",
                )
            }
        }
    }
}

private enum class ItemStatus(val displayName: String) {
    PRIVATE("private"),
    ACQUIRED("acquired"),
    PUBLIC("public"),
    RESERVED("reserved"),
    SOLD("sold");
    
    companion object {
        fun fromString(value: String): ItemStatus {
            return when (value.lowercase()) {
                "private" -> PRIVATE
                "acquired" -> ACQUIRED
                "public" -> PUBLIC
                "reserved" -> RESERVED
                "sold" -> SOLD
                else -> PRIVATE
            }
        }
    }
}

// ── Private Offers ─────────────────────────────────────────────────────────────
@Composable
private fun AdminPrivateOffersContent(
    onMenuClick: () -> Unit,
    onGoToChat: () -> Unit = {},
    onNavigateToPage: (DrawerPage) -> Unit = {},
    onShowBottomBarChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token   = remember { prefs.getString("auth_token", "") ?: "" }
    val scope   = rememberCoroutineScope()

    var itemList     by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading    by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editingItem  by remember { mutableStateOf<Item?>(null) }
    // Tapping an offer opens it in full, the way every other inventory
    // screen opens its rows.
    var selectedItem by remember { mutableStateOf<Item?>(null) }

    fun loadItems() {
        scope.launch {
            isLoading    = true
            errorMessage = null
            try {
                itemList = withContext(Dispatchers.IO) { fetchItems(token, "private") }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load items"
            } finally {
                isLoading = false
            }
        }
    }

    // First load, and again whenever the counter screen closes - an offer
    // acquired there leaves this list for the acquired one.
    LaunchedEffect(AdminCounter.openCode) {
        if (AdminCounter.openCode == null) loadItems()
    }

    // Pass editing state up to hide bottom bar in AdminDashboard
    val showBar = remember(editingItem, selectedItem) { editingItem == null && selectedItem == null }
    LaunchedEffect(showBar) {
        onShowBottomBarChange(showBar)
    }

    // Back closes the open item first, then the editor.
    BackHandler(enabled = selectedItem != null) { selectedItem = null }

    if (selectedItem != null) {
        AdminItemDetailPage(item = selectedItem!!, onBack = { selectedItem = null })
        return
    }

    CompositionLocalProvider(
        LocalProvidesBottomBar provides showBar
    ) {
        AnimatedContent(
            targetState = editingItem != null,
            label = "EditItemTransition"
        ) { isEditing ->
            if (isEditing && editingItem != null) {
                EditItemPageForList(
                    item = editingItem!!,
                    token = token,
                    onBack = { editingItem = null },
                    onItemUpdated = { updatedItem ->
                        if (updatedItem.status.lowercase() !in listOf("private", "pending")) {
                            val targetPage = when (updatedItem.status.lowercase()) {
                                "acquired" -> DrawerPage.AcquiredItems
                                "public"   -> DrawerPage.PublicListings
                                "reserved" -> DrawerPage.ReservedItems
                                "sold"     -> DrawerPage.SoldItems
                                else       -> null
                            }
                            if (targetPage != null) onNavigateToPage(targetPage)
                        } else {
                            itemList = itemList.map {
                                if (it.itemId == updatedItem.itemId) {
                                    // updatedItem is a ChatItem, so carry the
                                    // changed fields over rather than replacing
                                    // the Item wholesale.
                                    it.copy(
                                        status             = updatedItem.status,
                                        acquisitionPrice   = updatedItem.acquisitionPrice,
                                        publicPrice        = updatedItem.publicPrice,
                                        rewardPoints       = updatedItem.rewardPoints,
                                        sellerPayoutStatus = updatedItem.sellerPayoutStatus,
                                        isTurnoverVerified = updatedItem.isTurnoverVerified,
                                        photos             = updatedItem.photos
                                    )
                                } else it
                            }
                        }
                        editingItem = null
                    }
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    AdminPageHeader(
                        title = "Private offers",
                        subtitle = when {
                            isLoading -> "Items students want to sell you"
                            itemList.size == 1 -> "1 offer waiting for a decision"
                            else -> "${itemList.size} offers waiting for a decision"
                        },
                        onMenuClick = onMenuClick,
                    )

                    when {
                        isLoading -> LoadingState(message = "Loading offers…")
                        errorMessage != null -> ErrorState(
                            title = "Could not load offers",
                            message = errorMessage ?: "",
                            onRetry = { loadItems() },
                        )
                        itemList.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Icons.Outlined.LocalOffer,
                                title = "No offers right now",
                                message = "When a student offers an item to the store, it shows up here for you to review.",
                            )
                        }
                        else -> LazyColumn(
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(itemList, key = { it.itemId }) { item ->
                                AdminPrivateOfferCard(
                                    item          = item,
                                    token         = token,
                                    onStatusSaved = { newStatus ->
                                        itemList = itemList.map {
                                            if (it.itemId == item.itemId) it.copy(status = newStatus) else it
                                        }
                                    },
                                    onGoToChat    = onGoToChat,
                                    onEditClick   = { editingItem = it },
                                    onPointsSent  = { loadItems() },
                                    onOpenDetail  = { selectedItem = item }
                                )
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// ── Generic Item List (Acquired / Public / Reserved / Sold) ────────────────────
@Composable
private fun AdminItemListContent(
    title: String,
    status: String,
    emptyText: String,
    showActions: Boolean,
    onMenuClick: () -> Unit,
    onGoToChat: () -> Unit = {},
    onNavigateToPage: (DrawerPage) -> Unit = {},
    onShowBottomBarChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token   = remember { prefs.getString("auth_token", "") ?: "" }
    val scope   = rememberCoroutineScope()

    var itemList     by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading    by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editingItem  by remember { mutableStateOf<Item?>(null) }
    var selectedItem by remember { mutableStateOf<Item?>(null) }

    // hide bottom bar whenever we are in edit OR detail mode
    val showBar = remember(editingItem, selectedItem) {
        editingItem == null && selectedItem == null
    }

    fun loadItems() {
        scope.launch {
            isLoading    = true
            errorMessage = null
            try {
                itemList = withContext(Dispatchers.IO) { fetchItems(token, status) }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load items"
            } finally {
                isLoading = false
            }
        }
    }

    // The first load, and every return from the counter screen. A turnover
    // done there moves an item between these lists and settles its markup, so
    // reloading on the way back beats showing what was true before the
    // handover until someone opens the item to edit it.
    LaunchedEffect(AdminCounter.openCode) {
        if (AdminCounter.openCode == null) loadItems()
    }

    LaunchedEffect(showBar) {
        onShowBottomBarChange(showBar)
    }

    // Back-press: close detail first, then edit, then bubble up
    BackHandler(enabled = selectedItem != null || editingItem != null) {
        when {
            selectedItem != null -> selectedItem = null
            editingItem  != null -> editingItem  = null
        }
    }

    CompositionLocalProvider(LocalProvidesBottomBar provides showBar) {
        when {
            // ── Edit page ───────────────────────────────────────────────────────
            editingItem != null -> {
                EditItemPageForList(
                    item = editingItem!!,
                    token = token,
                    onBack = { editingItem = null },
                    onItemUpdated = { updatedItem ->
                        if (updatedItem.status.lowercase() != status.lowercase()) {
                            val targetPage = when (updatedItem.status.lowercase()) {
                                "private"  -> DrawerPage.PrivateOffers
                                "acquired" -> DrawerPage.AcquiredItems
                                "public"   -> DrawerPage.PublicListings
                                "reserved" -> DrawerPage.ReservedItems
                                "sold"     -> DrawerPage.SoldItems
                                else       -> null
                            }
                            if (targetPage != null) onNavigateToPage(targetPage)
                        } else {
                            itemList = itemList.map {
                                if (it.itemId == updatedItem.itemId)
                                    it.copy(
                                        status             = updatedItem.status,
                                        acquisitionPrice   = updatedItem.acquisitionPrice,
                                        publicPrice        = updatedItem.publicPrice,
                                        rewardPoints       = updatedItem.rewardPoints,
                                        sellerPayoutStatus = updatedItem.sellerPayoutStatus,
                                        isTurnoverVerified = updatedItem.isTurnoverVerified,
                                        photos             = updatedItem.photos
                                    )
                                else it
                            }
                        }
                        editingItem = null
                    }
                )
            }

            // ── Item detail page (view-only pages) ──────────────────────────────
            selectedItem != null -> {
                AdminItemDetailPage(
                    item   = selectedItem!!,
                    onBack = { selectedItem = null }
                )
            }

            // ── List ────────────────────────────────────────────────────────────
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    AdminPageHeader(
                        title = title,
                        subtitle = if (isLoading) null else "${itemList.size} item${if (itemList.size == 1) "" else "s"}",
                        onMenuClick = onMenuClick,
                    )

                    when {
                        isLoading -> LoadingState(message = "Loading items…")
                        errorMessage != null -> ErrorState(
                            title = "Could not load items",
                            message = errorMessage ?: "",
                            onRetry = { loadItems() },
                        )
                        itemList.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Icons.Outlined.Inventory2,
                                title = "Nothing here yet",
                                message = emptyText,
                            )
                        }
                        else -> LazyColumn(
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(itemList, key = { it.itemId }) { item ->
                                if (showActions) {
                                    AdminPrivateOfferCard(
                                        item          = item,
                                        token         = token,
                                        onStatusSaved = { newStatus ->
                                            itemList = itemList.map {
                                                if (it.itemId == item.itemId) it.copy(status = newStatus) else it
                                            }
                                        },
                                        onGoToChat  = onGoToChat,
                                        onEditClick = { editingItem = it },
                                        onPointsSent = { loadItems() },
                                        onOpenDetail = { selectedItem = item }
                                    )
                                } else {
                                    AdminViewOnlyItemCard(
                                        item         = item,
                                        onClick      = { selectedItem = item },
                                        onEditClick  = { editingItem = it }
                                    )
                                }
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// ── View-only card (Public / Reserved / Sold — no Edit or Chat buttons) ────────
@Composable
private fun AdminViewOnlyItemCard(item: Item, onClick: () -> Unit = {}, onEditClick: (Item) -> Unit = {}) {
    MarketCard(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(Spacing.md)) {
            // ── Photo ─────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                val photoUrl = item.photos.firstOrNull() ?: ""
                if (photoUrl.isNotBlank()) {
                    AsyncImage(
                        model              = photoUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Outlined.Image, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp))
                }
            }

            Spacer(Modifier.width(Spacing.md))

            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(
                        item.title,
                        style      = MaterialTheme.typography.titleMedium,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f).padding(end = Spacing.sm)
                    )
                    ItemStatusPill(item.status)
                }

                // Published items show the selling price; earlier
                // stages show what the student asked for.
                PriceTag(
                    if (item.publicPrice != null) item.displayPrice else item.displayAskingPrice,
                    size = PriceSize.Small,
                )

                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(Icons.Outlined.Person, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp))
                    Text(item.sellerEmail, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                // ── Markup info row ───────────────────────────────────────────
                if (item.markup != null) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Icon(Icons.Filled.TrendingUp, null,
                            tint = LocalMarketAccents.current.success,
                            modifier = Modifier.size(14.dp))
                        // Profit = public price - acquisition price.
                        Text("Markup ${Money.format(item.markup)}", style = MaterialTheme.typography.labelSmall,
                            color = LocalMarketAccents.current.success)
                    }
                }

                // The sale, on a sold row. A sold listing used to show what
                // the store hoped to charge and nothing about what was
                // actually handed over: no buyer, no method, no amount, and
                // no sign of the points the buyer earned back.
                item.sale?.let { sale -> SoldRowFacts(sale) }
            }

            // A sold item is history, and the server refuses to set that
            // status by hand, so there is nothing here to edit.
            if (!item.isSold) {
                IconButton(onClick = { onEditClick(item) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Edit, "Edit item",
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ── Item Detail Page (full-screen, view-only) ──────────────────────────────────
@Composable
private fun AdminItemDetailPage(item: Item, onBack: () -> Unit) {
    var currentPhotoIndex by remember { mutableStateOf(0) }

    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        val statusColor = when (item.status.lowercase()) {
            "sold"     -> MaterialTheme.colorScheme.error
            "reserved" -> LocalMarketAccents.current.warning
            "public"   -> MaterialTheme.colorScheme.primary
            "acquired" -> LocalMarketAccents.current.info
            else       -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        MarketPageTopBar(title = item.title, onBack = onBack) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.padding(end = Spacing.md)
            ) {
                Text(
                    item.status.replaceFirstChar { it.uppercaseChar() },
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }

        // ── Scrollable content ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Photo carousel ────────────────────────────────────────────────
            if (item.photos.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    AsyncImage(
                        model              = item.photos[currentPhotoIndex],
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                    // Photo counter
                    if (item.photos.size > 1) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp),
                            shape  = RoundedCornerShape(50),
                            color  = Color.Black.copy(alpha = 0.55f)
                        ) {
                            Text(
                                "${currentPhotoIndex + 1} / ${item.photos.size}",
                                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color      = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        // Prev / Next buttons
                        if (currentPhotoIndex > 0) {
                            IconButton(
                                onClick  = { currentPhotoIndex-- },
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 6.dp)
                                    .size(36.dp)
                                    .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                            ) {
                                Icon(Icons.Filled.ChevronLeft, "Prev",
                                    tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                        if (currentPhotoIndex < item.photos.lastIndex) {
                            IconButton(
                                onClick  = { currentPhotoIndex++ },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 6.dp)
                                    .size(36.dp)
                                    .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                            ) {
                                Icon(Icons.Filled.ChevronRight, "Next",
                                    tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    // Photo thumbnail strip
                    if (item.photos.size > 1) {
                        LazyRow(
                            modifier              = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 10.dp),
                            contentPadding        = PaddingValues(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(item.photos) { idx, url ->
                                Box(
                                    modifier = Modifier
                                        .size(if (idx == currentPhotoIndex) 36.dp else 28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            width = if (idx == currentPhotoIndex) 2.dp else 0.dp,
                                            color = Color.White,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { currentPhotoIndex = idx }
                                ) {
                                    AsyncImage(
                                        model        = url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier     = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Photo, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp))
                }
            }

            // ── Details card ──────────────────────────────────────────────────
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape     = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title
                    Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)

                    // Status + price row
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = statusColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                item.status.replaceFirstChar { it.uppercaseChar() },
                                modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color      = statusColor
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.MonetizationOn, null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text(
                                if (item.publicPrice != null) item.displayPrice else item.displayAskingPrice,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.headlineSmall,
                                color      = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Seller
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Seller", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(item.sellerEmail, style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Description
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Description", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp)
                        Text(item.description, style = MaterialTheme.typography.bodyMedium)
                    }

                    // Markup (public price minus acquisition price)
                    if (item.markup != null) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(LocalMarketAccents.current.info.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.TrendingUp, null,
                                    tint = LocalMarketAccents.current.info, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Markup", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(Money.format(item.markup), style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold, color = LocalMarketAccents.current.info)
                            }
                        }
                    }

                    // Listed on
                    if (item.createdAt.isNotBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.CalendarToday, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("Listed on", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatDate(item.createdAt), style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // The figures and the proof, as the website's item window lays
            // them out. This page used to stop at the markup, so the agreed
            // price, the payout, the counter's photographs and the sale
            // itself could only be read on a computer.
            AdminItemFactsCard(item)

            Spacer(Modifier.height(24.dp))
        }
    }
}

private val LocalProvidesBottomBar = compositionLocalOf { true }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminPrivateOfferCard(
    item: Item,
    token: String,
    onStatusSaved: (String) -> Unit,
    onGoToChat: () -> Unit = {},
    onEditClick: (Item) -> Unit = {},
    onPointsSent: () -> Unit = {},
    /** Opens the full item page. Offers and acquired items had no way in. */
    onOpenDetail: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showChatDialog    by remember { mutableStateOf(false) }
    var chatText          by remember { mutableStateOf("") }
    var isSendingChat     by remember { mutableStateOf(false) }
    var chatError         by remember { mutableStateOf<String?>(null) }
    var chatSent          by remember { mutableStateOf(false) }

    // Physical turnover and the seller cash payout. These replace the old
    // "Send Points & Finalize" flow, which paid sellers in wallet points.
    var showTurnoverDialog by remember { mutableStateOf(false) }
    var showPayoutDialog   by remember { mutableStateOf(false) }
    var showAcceptDialog   by remember { mutableStateOf(false) }
    var showPublishDialog  by remember { mutableStateOf(false) }
    var showDeleteDialog   by remember { mutableStateOf(false) }

    if (showAcceptDialog) {
        AcceptOfferDialog(
            item = item,
            token = token,
            onDismiss = { showAcceptDialog = false },
            onAccepted = { onStatusSaved(item.status) },
        )
    }

    if (showPublishDialog) {
        PublishItemDialog(
            item = item,
            token = token,
            onDismiss = { showPublishDialog = false },
            onPublished = { onStatusSaved("public") },
            onEditDetails = { onEditClick(item) },
        )
    }

    if (showDeleteDialog) {
        DeleteOfferDialog(
            item = item,
            token = token,
            onDismiss = { showDeleteDialog = false },
            onDeleted = { onPointsSent() },
        )
    }
    var acquisitionInput   by remember(item.itemId) {
        mutableStateOf(Money.formatPlain(item.acquisitionPrice ?: item.sellerAskingPrice).replace(",", ""))
    }
    var turnoverNotes      by remember(item.itemId) { mutableStateOf("") }
    var isWorking          by remember { mutableStateOf(false) }
    var actionError        by remember { mutableStateOf<String?>(null) }

    // -- Verify physical turnover --------------------------------------
    if (showTurnoverDialog) {
        // The same Mark Acquired the conversation uses: proof photos captured
        // in place, the agreed price settled here when chat never fixed one,
        // and one wording everywhere.
        AcquireOfferDialog(
            listing = item,
            token = token,
            onDismiss = { showTurnoverDialog = false },
            onFinished = { error ->
                showTurnoverDialog = false

                if (error == null) {
                    onStatusSaved("acquired")
                } else {
                    android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                }
            },
        )
    }

    // -- Record the seller cash payout ---------------------------------
    if (showPayoutDialog) {
        val payable = item.sellerPayoutAmount ?: item.acquisitionPrice

        AlertDialog(
            onDismissRequest = { if (!isWorking) { showPayoutDialog = false; actionError = null } },
            title = { Text("Record Cash Payout", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Confirm you have paid ${Money.format(payable)} in cash to ${item.sellerEmail}.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "This records the seller payout only. It does not give the seller any points.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    actionError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Mark as Paid",
                    compact = true,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    onClick = {
                        scope.launch {
                            isWorking = true
                            actionError = null
                            val result = withContext(Dispatchers.IO) {
                                MarketplaceApi.recordSellerPayout(token, item.itemId, null)
                            }
                            isWorking = false
                            when (result) {
                                is MarketplaceApi.Result.Ok -> {
                                    showPayoutDialog = false
                                    onPointsSent()
                                }
                                is MarketplaceApi.Result.Failure -> actionError = result.message
                            }
                        }
                    },
                    loading = isWorking,
                )
            },
            dismissButton = {
                TextButton(onClick = { showPayoutDialog = false }, enabled = !isWorking) { Text("Cancel") }
            }
        )
    }

    // ── Send Points Dialog ────────────────────────────────────────────────────

    // ── Chat dialog ───────────────────────────────────────────────────────────
    if (showChatDialog) {
        AlertDialog(
            onDismissRequest = {
                showChatDialog = false; chatText = ""; chatError = null; chatSent = false
            },
            title = {
                Text("Message Seller", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Send a message to ${item.sellerEmail}",
                        style = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (chatSent) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.CheckCircle, null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                            Text(
                                "Message sent successfully!",
                                color      = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign  = TextAlign.Center
                            )
                            Text(
                                "Would you like to go to the chat?",
                                style = MaterialTheme.typography.bodySmall,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        chatError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedTextField(
                            value         = chatText,
                            onValueChange = { chatText = it; chatError = null },
                            placeholder   = { Text("Type your message...") },
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp),
                            maxLines      = 4
                        )
                    }
                }
            },
            confirmButton = {
                if (chatSent) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            showChatDialog = false; chatText = ""; chatSent = false
                        }) {
                            Text("Close")
                        }
                        PrimaryButton(
                            text = "Go to Chat",
                            icon = Icons.Filled.Chat,
                            compact = true,
                            onClick = {
                                showChatDialog = false; chatText = ""; chatSent = false
                                onGoToChat()
                            },
                        )
                    }
                } else {
                    PrimaryButton(
                        text = "Send",
                        compact = true,
                        onClick = {
                            if (chatText.isBlank()) { chatError = "Please enter a message."; return@PrimaryButton }
                            scope.launch {
                                isSendingChat = true
                                val ok = withContext(Dispatchers.IO) {
                                    sendMessage(token, item.itemId, item.sellerId, chatText.trim())
                                }
                                isSendingChat = false
                                if (ok) chatSent = true else chatError = "Failed to send. Please try again."
                            }
                        },
                        loading = isSendingChat,
                    )
                }
            },
            dismissButton = if (!chatSent) {
                { TextButton(onClick = { showChatDialog = false; chatText = ""; chatError = null }) { Text("Cancel") } }
            } else {
                null
            }
        )
    }

    MarketCard(onClick = onOpenDetail, contentPadding = PaddingValues(0.dp)) {
        // ── Photo with the status floating over it ────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            val photoUrl = item.photos.firstOrNull() ?: ""
            if (photoUrl.isNotBlank()) {
                AsyncImage(
                    model              = photoUrl,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Outlined.Image, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(44.dp))
            }
            ItemStatusPill(
                item.status,
                offerAccepted = item.offerAccepted,
                modifier = Modifier.align(Alignment.TopStart).padding(Spacing.md),
            )
        }

        Column(
            modifier            = Modifier.fillMaxWidth().padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // ── Title, seller, asking price ───────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Text(
                        item.title,
                        style      = MaterialTheme.typography.titleLarge,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                    )
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Icon(Icons.Outlined.Person, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp))
                        Text(item.sellerEmail, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Asking", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    PriceTag(item.displayAskingPrice, size = PriceSize.Small)
                }
            }

            Text(item.description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3, overflow = TextOverflow.Ellipsis)

            // -- Negotiation and turnover state --------------------
            if (item.acquisitionPrice != null || item.isTurnoverVerified) {
                MarketPanel {
                    item.acquisitionPrice?.let {
                        SummaryRow(label = "Agreed price", value = Money.format(it), valueColor = LocalMarketAccents.current.info)
                    }
                    if (item.isTurnoverVerified) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            Icon(Icons.Filled.CheckCircle, null,
                                tint = LocalMarketAccents.current.success, modifier = Modifier.size(16.dp))
                            Text("Item received and verified", style = MaterialTheme.typography.bodySmall,
                                color = LocalMarketAccents.current.success)
                        }
                    }
                    SummaryRow(
                        label = "Seller payout",
                        value = if (item.sellerIsPaid) "Paid" else "Unpaid",
                        valueColor = if (item.sellerIsPaid) LocalMarketAccents.current.success else LocalMarketAccents.current.warning,
                    )
                }
            }

            SoftDivider()

            // ── Action buttons ─────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                // -- Turnover, then cash payout ----------------------
                // The seller is paid in cash after Ofelia verifies the
                // physical item. No points change hands here.
                if (!item.isTurnoverVerified && !item.isSold) {
                    PrimaryButton(
                        text = "Mark as acquired",
                        icon = Icons.Outlined.Inventory2,
                        onClick = { showTurnoverDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (item.isTurnoverVerified && !item.sellerIsPaid) {
                    PrimaryButton(
                        text = "Pay seller ${Money.format(item.sellerPayoutAmount ?: item.acquisitionPrice)}",
                        icon = Icons.Outlined.Payments,
                        onClick = { showPayoutDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = LocalMarketAccents.current.success,
                        contentColor = Color.White,
                    )
                }

                if (item.isTurnoverVerified && !item.isPublic && !item.isSold) {
                    PrimaryButton(
                        text = "Set selling price and publish",
                        icon = StoreLogoIcon,
                        onClick = { showPublishDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = LocalMarketAccents.current.info,
                        contentColor = Color.White,
                    )
                }

                // Accepting an offer is agreeing the price. It used to live
                // only in the chat, so the offers list could receive an item
                // nobody had answered yet.
                if (item.isPending && !item.offerAccepted) {
                    PrimaryButton(
                        text = "Accept offer",
                        icon = Icons.Outlined.Check,
                        onClick = { showAcceptDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    SecondaryButton(
                        text = "Chat seller",
                        icon = Icons.Outlined.ChatBubbleOutline,
                        onClick = { showChatDialog = true },
                        modifier = Modifier.weight(1f),
                        compact = true,
                    )
                    SecondaryButton(
                        text = "Edit item",
                        icon = Icons.Outlined.Edit,
                        onClick = { onEditClick(item) },
                        modifier = Modifier.weight(1f),
                        compact = true,
                    )
                }

                // Only for a listing the store has not taken in: once it
                // holds the item the row is inventory and history, and the
                // server refuses anyway.
                if ((item.isPending || item.isRejected) && !item.isTurnoverVerified) {
                    SecondaryButton(
                        text = "Delete offer",
                        icon = Icons.Outlined.DeleteOutline,
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        compact = true,
                        contentColor = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

// ── Home ───────────────────────────────────────────────────────────────────────

/** One line of the dashboard's activity feed. */
private data class DashboardLine(val title: String, val subtitle: String)

/**
 * The store's front page.
 *
 * Leads with what needs Ofelia's hands today - students waiting for approval,
 * offers waiting for a price, items on hold - each a tap from the page that
 * deals with it. The wall of statistics that used to be the whole page now
 * sits underneath, where it belongs.
 */
@Composable
private fun AdminHomeContent(
    onMenuClick: () -> Unit,
    firstName: String = "",
    onOpenPage: (DrawerPage) -> Unit = {},
    onOpenStudents: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onScan: () -> Unit = {},
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val token   = remember { prefs.getString("auth_token", "") ?: "" }
    val accents = LocalMarketAccents.current

    // Users statistics
    var totalStudents by remember { mutableStateOf(0) }
    var activeStudents by remember { mutableStateOf(0) }
    var verifiedStudents by remember { mutableStateOf(0) }

    // Items statistics
    var totalProducts by remember { mutableStateOf(0) }
    var privateItems by remember { mutableStateOf(0) }
    var publicItems by remember { mutableStateOf(0) }
    var acquiredItems by remember { mutableStateOf(0) }
    var reservedItems by remember { mutableStateOf(0) }
    var soldItems by remember { mutableStateOf(0) }

    // Recent activities
    var recentRegistrations by remember { mutableStateOf<List<DashboardLine>>(emptyList()) }
    var recentItemsList by remember { mutableStateOf<List<DashboardLine>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    // Fetch dashboard data from API
    LaunchedEffect(token, refreshKey) {
        if (token.isBlank()) return@LaunchedEffect
        isLoading = true
        loadError = null
        try {
            val data = withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url("https://fati-api.alertaraqc.com/api/admin/dashboard")
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "application/json")
                    .get()
                    .build()
                adminHttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) response.body?.string() ?: "" else ""
                }
            }

            if (data.isNotEmpty()) {
                val json = JSONObject(data)
                val dataObj = json.optJSONObject("data")

                if (dataObj != null) {
                    val usersObj = dataObj.optJSONObject("users")
                    totalStudents = usersObj?.optInt("total_students", 0) ?: 0
                    activeStudents = usersObj?.optInt("active_students", 0) ?: 0
                    verifiedStudents = usersObj?.optInt("verified_students", 0) ?: 0

                    val itemsObj = dataObj.optJSONObject("items")
                    totalProducts = itemsObj?.optInt("total_items", 0) ?: 0
                    privateItems = itemsObj?.optInt("private_items", 0) ?: 0
                    publicItems = itemsObj?.optInt("public_items", 0) ?: 0
                    acquiredItems = itemsObj?.optInt("acquired_items", 0) ?: 0
                    reservedItems = itemsObj?.optInt("reserved_items", 0) ?: 0
                    soldItems = itemsObj?.optInt("sold_items", 0) ?: 0

                    val activitiesObj = dataObj.optJSONObject("recent_activities")
                    if (activitiesObj != null) {
                        val registrationsArr = activitiesObj.optJSONArray("recent_registrations")
                        recentRegistrations = (0 until (registrationsArr?.length() ?: 0)).map { i ->
                            val obj = registrationsArr!!.getJSONObject(i)
                            DashboardLine(
                                title = obj.optString("name", ""),
                                subtitle = obj.optString("email", ""),
                            )
                        }

                        val itemsArr = activitiesObj.optJSONArray("recent_items")
                        recentItemsList = (0 until (itemsArr?.length() ?: 0)).map { i ->
                            val obj = itemsArr!!.getJSONObject(i)
                            DashboardLine(
                                title = obj.optString("title", ""),
                                subtitle = "${obj.optString("seller", "")} · ${obj.optString("status", "")}",
                            )
                        }
                    }
                }
            } else {
                loadError = "The server did not return dashboard data."
            }
        } catch (e: Exception) {
            loadError = e.message ?: "Could not load the dashboard."
        } finally {
            isLoading = false
        }
    }

    val greeting = remember {
        when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    // Approvals are not this screen's business any more, so they no longer
    // count as something waiting on you.
    val attentionCount = privateItems + reservedItems

    var showNotifications by remember { mutableStateOf(false) }
    if (showNotifications) {
        NotificationsDialog(onDismiss = { showNotifications = false })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MarketHeader(
            title = "$greeting${if (firstName.isNotBlank()) ", $firstName" else ""}",
            subtitle = when {
                isLoading -> "Loading today's numbers…"
                attentionCount == 0 -> "All caught up. Nothing is waiting on you."
                attentionCount == 1 -> "1 thing needs your attention"
                else -> "$attentionCount things need your attention"
            },
            onMenuClick = onMenuClick,
            actions = {
                HeaderAction(
                    icon = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    onClick = { showNotifications = true },
                )
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            loadError?.let { err ->
                InfoBanner(
                    title = "Could not refresh",
                    text = err,
                    tone = StatusTone.Danger,
                    icon = Icons.Filled.ErrorOutline,
                )
                SecondaryButton(text = "Try again", compact = true, onClick = { refreshKey++ })
            }

            // ── Needs attention ───────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                SectionHeader(title = "Needs attention", subtitle = "Tap a card to deal with it")
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StatTile(
                        label = "Offers to review",
                        value = if (isLoading) "–" else privateItems.toString(),
                        icon = Icons.Filled.LocalOffer,
                        tint = if (privateItems > 0) accents.warning else MaterialTheme.colorScheme.primary,
                        onClick = { onOpenPage(DrawerPage.PrivateOffers) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StatTile(
                        label = "Reserved for pickup",
                        value = if (isLoading) "–" else reservedItems.toString(),
                        icon = Icons.Filled.Schedule,
                        tint = accents.info,
                        onClick = { onOpenPage(DrawerPage.ReservedItems) },
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = "In stock, unlisted",
                        value = if (isLoading) "–" else acquiredItems.toString(),
                        icon = Icons.Filled.Inventory2,
                        tint = accents.info,
                        onClick = { onOpenPage(DrawerPage.AcquiredItems) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Quick actions ─────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                SectionHeader(title = "Quick actions")
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = Elevation.card,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm, horizontal = Spacing.xs),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        QuickAction(label = "Scan code", icon = Icons.Filled.QrCodeScanner, onClick = onScan, modifier = Modifier.weight(1f))
                        QuickAction(label = "Orders", icon = Icons.Filled.ReceiptLong, onClick = { onOpenPage(DrawerPage.ManageOrders) }, modifier = Modifier.weight(1f))
                        QuickAction(label = "Listings", icon = StoreLogoIcon, onClick = { onOpenPage(DrawerPage.PublicListings) }, modifier = Modifier.weight(1f))
                        QuickAction(label = "Messages", icon = Icons.Filled.ChatBubble, onClick = onOpenChat, modifier = Modifier.weight(1f))
                    }
                }
            }

            // BOOKING/SCHEDULE DISABLED - no longer required
            // // ── Meet-ups ──────────────────────────────────────────────────
            // // Who is coming in with an item, and when - booked from the offer
            // // chats, rescheduled from here.
            // AdminMeetupsCard(token = token)

            // ── Overview ──────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                SectionHeader(title = "Store overview")
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = Elevation.card,
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        Overline("Items")
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            OverviewFigure("Total", totalProducts, isLoading, Modifier.weight(1f))
                            OverviewFigure("On sale", publicItems, isLoading, Modifier.weight(1f))
                            OverviewFigure("Sold", soldItems, isLoading, Modifier.weight(1f))
                        }
                        SoftDivider()
                        Overline("Students")
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            OverviewFigure("Registered", totalStudents, isLoading, Modifier.weight(1f))
                            OverviewFigure("Verified", verifiedStudents, isLoading, Modifier.weight(1f))
                            OverviewFigure("Active", activeStudents, isLoading, Modifier.weight(1f))
                        }
                    }
                }
            }

            // ── Recent activity ───────────────────────────────────────────
            if (recentItemsList.isNotEmpty()) {
                ActivityGroup(
                    title = "Recent items",
                    lines = recentItemsList.take(3),
                    icon = Icons.Filled.ShoppingBag,
                    tint = MaterialTheme.colorScheme.primary,
                    actionLabel = "All offers",
                    onAction = { onOpenPage(DrawerPage.PrivateOffers) },
                )
            }

            if (recentRegistrations.isNotEmpty()) {
                ActivityGroup(
                    title = "New students",
                    lines = recentRegistrations.take(3),
                    icon = Icons.Filled.PersonAdd,
                    tint = accents.info,
                    actionLabel = "All students",
                    onAction = onOpenStudents,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))
        }
    }
}

/** A figure inside the overview card. */
@Composable
private fun OverviewFigure(label: String, value: Int, loading: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            if (loading) "–" else value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A titled list of recent lines with a link to the page that holds them all. */
@Composable
private fun ActivityGroup(
    title: String,
    lines: List<DashboardLine>,
    icon: ImageVector,
    tint: Color,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SectionHeader(title = title, actionLabel = actionLabel, onAction = onAction)
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = Elevation.card,
        ) {
            Column {
                lines.forEachIndexed { index, line ->
                    if (index > 0) RowDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(tint.copy(alpha = 0.12f), MaterialTheme.shapes.extraSmall),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                line.title.ifBlank { "—" },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                line.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Chat data models ────────────────────────────────────────────────────────────

data class ChatItem(
    val itemId: Int,
    val title: String,
    val description: String,
    /** The student's asking price, in pesos. */
    val askingPrice: String?,
    /** What Admin agreed to pay the seller, once negotiated. */
    val acquisitionPrice: String?,
    /** The buyer-facing selling price, once published. */
    val publicPrice: String?,
    val rewardPoints: Int,
    val sellerPayoutStatus: String,
    val isTurnoverVerified: Boolean,
    val sellerId: Int,
    val sellerEmail: String,
    val status: String,
    val photos: List<String>
)

data class Conversation(
    val otherUserId: Int,
    val otherUserEmail: String,
    val firstName: String,
    val lastName: String,
    val profilePicture: String,
    val itemId: Int,
    /** Admin has priced the offer - "Negotiating" becomes "Offer Accepted". */
    val itemOfferAccepted: Boolean = false,
    val itemTitle: String,
    val itemStatus: String = "",
    val itemPhoto: String = "",
    val latestMessage: String,
    val lastMessageAt: String,
    val messageCount: Int,
    val unreadCount: Int = 0,
    val lastMessageSenderId: Int = 0,
    /** This person's own name for the thread; blank keeps the item's title. */
    val customName: String = "",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
) {
    val displayTitle: String get() = customName.ifBlank { itemTitle }
}

/** The line a reply answers - who said it and what, drawn above the bubble. */
internal data class ReplyPreview(
    val messageId: Int,
    val senderId: Int,
    val senderName: String,
    val text: String,
    val kind: String = "text",
)

internal data class ChatMessage(
    val messageId: Int,
    val itemId: Int,
    val itemTitle: String,
    val senderId: Int,
    val senderName: String,
    val senderProfilePicture: String,
    val receiverId: Int,
    val receiverName: String,
    val receiverProfilePicture: String,
    val message: String,
    val sentAt: String,

    /**
     * What this message is: "text" for something a person typed, or one of the
     * order kinds, which are drawn as a card instead of a bubble.
     */
    val kind: String = "text",

    /**
     * The order the card describes, as the server currently sees it.
     *
     * It is re-read on every poll, so a card posted at checkout shows the
     * payment as unpaid and then as paid once Admin verifies it - the buyer
     * never has to look anywhere else to know where their money stands.
     */
    val order: MarketTransaction? = null,

    /** The listing behind an "item_listed" or "item_acquired" message. */
    val listedItem: Item? = null,

    /**
     * What was true when this line was written.
     *
     * A card used to render the order's CURRENT state, so every card in a
     * thread said the same thing - "checking payment" on the order card and
     * on the receipt card alike. These keep each line in its own moment, the
     * way a conversation should read.
     */
    val paymentStatusAt: String? = null,
    val orderStatusAt: String? = null,

    /** The line this one answers, drawn as a quote above the bubble. */
    val replyTo: ReplyPreview? = null,
) {
    val isOrderCard: Boolean get() = kind != "text" && order != null

    val isItemCard: Boolean get() = kind == "item_listed" && listedItem != null

    /** The turnover receipt: the item arrived and the seller was settled. */
    val isAcquiredCard: Boolean get() = kind == "item_acquired" && listedItem != null
}

// ── Chat API ────────────────────────────────────────────────────────────────────

private fun fetchConversations(token: String): List<Conversation> {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/conversations")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val list = mutableListOf<Conversation>()
            val arr = when {
                body.trimStart().startsWith("[") -> org.json.JSONArray(body)
                else -> {
                    val obj = org.json.JSONObject(body)
                    obj.optJSONArray("data")
                        ?: obj.optJSONArray("conversations")
                        ?: obj.optJSONArray("messages")
                        ?: return emptyList()
                }
            }
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                // Try every plausible field name the API might use for "the other person"
                val userId = obj.optInt("other_user_id").takeIf { it != 0 }
                    ?: obj.optInt("sender_id").takeIf { it != 0 }
                    ?: obj.optInt("receiver_id").takeIf { it != 0 }
                    ?: obj.optInt("admin_id").takeIf { it != 0 }
                    ?: obj.optInt("user_id", 0)
                val itemId = obj.optInt("item_id").takeIf { it != 0 }
                    ?: obj.optInt("item", 0)
                Log.d("FetchConversations", "Conv $i: userId=$userId, itemId=$itemId")
                list.add(Conversation(
                    otherUserId      = userId,
                    otherUserEmail   = obj.optString("other_user_email").ifBlank { obj.optString("email") },
                    firstName        = obj.optString("first_name"),
                    lastName         = obj.optString("last_name"),
                    profilePicture   = obj.optString("profile_picture"),
                    itemId           = itemId,
                    itemOfferAccepted = obj.optBoolean("item_offer_accepted", false),
                    itemTitle        = obj.optString("item_title").ifBlank { obj.optString("title") },
                    itemStatus       = obj.optString("item_status").ifBlank { obj.optString("status") },
                    itemPhoto        = obj.optString("item_photo"),
                    latestMessage    = obj.optString("latest_message").ifBlank { obj.optString("last_message") },
                    lastMessageAt    = obj.optString("last_message_at").ifBlank { obj.optString("updated_at") },
                    messageCount     = obj.optInt("message_count"),
                    unreadCount      = obj.optInt("unread_count", 0),
                    lastMessageSenderId = obj.optInt("last_message_sender_id").takeIf { it != 0 }
                                        ?: obj.optInt("sender_id", 0),
                    customName       = obj.optString("custom_name").takeIf { it != "null" }.orEmpty(),
                    isPinned         = obj.optBoolean("is_pinned", false),
                    isArchived       = obj.optBoolean("is_archived", false),
                ))
            }
            // Keep each unique user+item pair as its own conversation
            list.distinctBy { "${it.otherUserId}_${it.itemId}" }
        }
    } catch (_: Exception) { emptyList() }
}

/**
 * Returns the message list on success.
 * Throws an Exception with the HTTP status + response body on failure so the
 * caller can display a meaningful error (instead of silently returning null).
 */
private fun fetchMessages(token: String, itemId: Int, otherUserId: Int = 0): ChatThread {
    val base = "https://fati-api.alertaraqc.com/api/messages/$itemId"
    val url  = if (otherUserId != 0) "$base?other_user_id=$otherUserId" else base
    Log.d("FetchMessages", "URL: $url, itemId: $itemId, otherUserId: $otherUserId")
    val request = Request.Builder()
        .url(url)
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    adminHttpClient.newCall(request).execute().use { response ->
        val body = response.body?.string() ?: ""
        if (!response.isSuccessful) {
            // Truncate body so the toast/error text stays readable
            val preview = body.take(200).replace("\n", " ")
            throw Exception("HTTP ${response.code} – $preview")
        }
        val list = mutableListOf<ChatMessage>()
        // Handle: bare array, {"data":[...]}, {"messages":[...]},
        //         {"data":{"messages":[...]}}, {"data":{"data":[...]}}
        val arr: org.json.JSONArray? = when {
            body.trimStart().startsWith("[") -> org.json.JSONArray(body)
            else -> {
                val obj = org.json.JSONObject(body)
                obj.optJSONArray("data")
                    ?: obj.optJSONArray("messages")
                    ?: obj.optJSONArray("chat_messages")
                    ?: obj.optJSONObject("data")?.let { d ->
                        d.optJSONArray("messages")
                            ?: d.optJSONArray("data")
                            ?: d.optJSONArray("chat_messages")
                    }
            }
        }
        arr ?: return ChatThread(emptyList())   // valid 200 body but no message array → truly empty
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val senderObj   = obj.optJSONObject("sender")
            val receiverObj = obj.optJSONObject("receiver")
            val senderId = obj.optInt("sender_id").takeIf { it != 0 }
                ?: senderObj?.optInt("id", 0) ?: obj.optInt("from_id", 0)
            val receiverId = obj.optInt("receiver_id").takeIf { it != 0 }
                ?: receiverObj?.optInt("id", 0) ?: obj.optInt("to_id", 0)
            list.add(ChatMessage(
                messageId              = obj.optInt("message_id").takeIf { it != 0 }
                                         ?: obj.optInt("id").takeIf { it != 0 } ?: (i + 1),
                itemId                 = obj.optInt("item_id").takeIf { it != 0 } ?: itemId,
                itemTitle              = obj.optString("item_title"),
                senderId               = senderId,
                senderName             = obj.optString("sender_name").ifBlank {
                                         senderObj?.optString("name") ?: obj.optString("from_name") },
                senderProfilePicture   = obj.optString("sender_profile_picture").ifBlank {
                                         senderObj?.optString("profile_picture") ?: "" },
                receiverId             = receiverId,
                receiverName           = obj.optString("receiver_name").ifBlank {
                                         receiverObj?.optString("name") ?: obj.optString("to_name") },
                receiverProfilePicture = obj.optString("receiver_profile_picture").ifBlank {
                                         receiverObj?.optString("profile_picture") ?: "" },
                message                = obj.optString("message").ifBlank { obj.optString("content") },
                sentAt                 = obj.optString("sent_at").ifBlank { obj.optString("created_at") },
                kind                   = obj.optString("kind").ifBlank { "text" },
                order                  = obj.optJSONObject("order")?.let { parseTransaction(it) },
                listedItem             = obj.optJSONObject("item_card")?.let { parseItem(it) },
                paymentStatusAt        = obj.optString("payment_status_at")
                                            .takeIf { it.isNotBlank() && it != "null" },
                orderStatusAt          = obj.optString("order_status_at")
                                            .takeIf { it.isNotBlank() && it != "null" },
                replyTo                = obj.optJSONObject("reply_to")?.let { quote ->
                    ReplyPreview(
                        messageId  = quote.optInt("message_id"),
                        senderId   = quote.optInt("sender_id"),
                        senderName = quote.optString("sender_name").trim(),
                        text       = quote.optString("message"),
                        kind       = quote.optString("kind").ifBlank { "text" },
                    )
                },
            ))
        }

        // The lines the thread keeps about itself - a rename so far. They
        // used to be written to this phone's own storage, so a rename made on
        // the website never reached the app, and reinstalling lost them.
        val events = mutableListOf<ThreadEvent>()
        val eventsArr = runCatching { JSONObject(body).optJSONArray("events") }.getOrNull()

        for (i in 0 until (eventsArr?.length() ?: 0)) {
            val obj = eventsArr!!.getJSONObject(i)
            val at = Dates.parse(obj.optString("at"))?.toEpochMilli() ?: continue

            events.add(
                ThreadEvent(
                    name = if (obj.isNull("name")) "" else obj.optString("name"),
                    at = at,
                )
            )
        }

        return ChatThread(messages = list, events = events)
    }
}

private fun sendMessage(
    token: String,
    itemId: Int,
    receiverId: Int,
    message: String,
    replyToMessageId: Int? = null,
): Boolean {
    val json = JSONObject().apply {
        put("receiver_id", receiverId)
        put("message", message)
        replyToMessageId?.let { put("reply_to_message_id", it) }
    }.toString()
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/messages/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(json.toRequestBody("application/json".toMediaType()))
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

/**
 * Name, pin or archive one thread - for this person only. Fields left null
 * are not touched; an empty name clears the custom one.
 */
private fun updateConversationSettings(
    token: String,
    itemId: Int,
    otherUserId: Int,
    customName: String? = null,
    isPinned: Boolean? = null,
    isArchived: Boolean? = null,
): Boolean {
    val json = JSONObject().apply {
        customName?.let { put("custom_name", it) }
        isPinned?.let { put("is_pinned", it) }
        isArchived?.let { put("is_archived", it) }
    }.toString()
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/conversations/$itemId/$otherUserId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .patch(json.toRequestBody("application/json".toMediaType()))
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

/** "Delete for me": hides the thread's history for this person only. */
private fun clearConversation(token: String, itemId: Int, otherUserId: Int): Boolean {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/conversations/$itemId/$otherUserId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .delete()
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}


private fun sendPointsToUser(token: String, userId: Int, points: Int, reason: String, itemId: Int = 0): Pair<Boolean, String?> {
    val json = JSONObject().apply {
        put("user_id", userId)
        put("points", points)
        put("reason", reason)
        if (itemId > 0) {
            put("related_item_id", itemId)
        }
    }.toString()
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/admin/send-points")
        .header("Authorization", "Bearer $token")
        .header("Content-Type", "application/json")
        .post(json.toRequestBody("application/json".toMediaType()))
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                Pair(true, null)
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                Pair(false, "Server error (${response.code}): $errorBody")
            }
        }
    } catch (e: Exception) {
        Pair(false, "Connection error: ${e.message}")
    }
}

private fun checkPointsStatus(token: String, itemId: Int): Boolean {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/admin/item/$itemId/points-status")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@use false
                val json = JSONObject(body)
                json.optBoolean("points_sent", false)
            } else {
                false
            }
        }
    } catch (e: Exception) {
        false
    }
}

private fun insertTransaction(token: String, itemId: Int, points: Int, reason: String = "purchase"): Boolean {
    val json = JSONObject().apply {
        put("item_id", itemId)
        put("payment_method", "points")
        put("points_used", points)
        put("reason", reason)
    }.toString()
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/transactions")
        .header("Authorization", "Bearer $token")
        .header("Content-Type", "application/json")
        .post(json.toRequestBody("application/json".toMediaType()))
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

private fun insertAdminTransaction(token: String, itemId: Int, points: Int, reason: String = "purchase"): Boolean {
    val json = JSONObject().apply {
        put("item_id", itemId)
        put("payment_method", "points")
        put("points_used", points)
        put("reason", reason)
    }.toString()
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/transactions")
        .header("Authorization", "Bearer $token")
        .header("Content-Type", "application/json")
        .post(json.toRequestBody("application/json".toMediaType()))
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

private fun markMessagesRead(token: String, itemId: Int): Boolean {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/messages/$itemId/read")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post("{}".toRequestBody("application/json".toMediaType()))
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { it.isSuccessful }
    } catch (_: Exception) { false }
}

private data class EmojiItem(
    val name: String,
    val category: String,
    val htmlCode: List<String>
)

private fun htmlCodeToChar(htmlCode: String): String {
    val code = htmlCode.removePrefix("&#").removeSuffix(";").toIntOrNull() ?: return ""
    return runCatching { String(Character.toChars(code)) }.getOrElse { "" }
}

// Hardcoded categories so we can show tabs instantly without a "fetch all" call.
// Each maps to the EmojiHub API slug used in /api/all/category/{slug}.
private val emojiCategories = listOf(
    "smileys and people"  to "smileys-and-people",
    "animals and nature"  to "animals-and-nature",
    "food and drink"      to "food-and-drink",
    "travel and places"   to "travel-and-places",
    "activities"          to "activities",
    "objects"             to "objects",
    "symbols"             to "symbols",
    "flags"               to "flags"
)

// Module-level cache: survives recompositions but is cleared when the process dies.
private val emojiCache = mutableMapOf<String, List<EmojiItem>>()

private suspend fun fetchEmojisByCategory(slug: String): List<EmojiItem> = withContext(Dispatchers.IO) {
    emojiCache[slug]?.let { return@withContext it }
    val request = Request.Builder()
        .url("https://emojihub.yurace.pro/api/all/category/$slug")
        .build()
    val body = adminHttpClient.newCall(request).execute().use { it.body?.string() ?: "[]" }
    val arr = JSONArray(body)
    val result = buildList {
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val htmlArr = obj.getJSONArray("htmlCode")
            val codes = buildList { for (j in 0 until htmlArr.length()) add(htmlArr.getString(j)) }
            add(EmojiItem(obj.optString("name"), obj.optString("category"), codes))
        }
    }
    emojiCache[slug] = result
    result
}

internal fun timeAgo(dateStr: String): String {
    return try {
        val date = if (dateStr.contains("T")) {
            val cleaned = dateStr.replace(Regex("\\.\\d+Z?$"), "")
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                .parse(cleaned)
        } else {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).parse(dateStr)
        } ?: return dateStr
        val diff    = System.currentTimeMillis() - date.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours   = minutes / 60
        val days    = hours / 24
        when {
            seconds < 60 -> "just now"
            minutes < 60 -> "${minutes}m ago"
            hours   < 24 -> "${hours}h ago"
            days    < 7  -> "${days}d ago"
            else         -> java.text.SimpleDateFormat("MMM d", java.util.Locale.US).format(date)
        }
    } catch (_: Exception) { dateStr }
}

// ── Chat ───────────────────────────────────────────────────────────────────────

@Composable
fun AdminChatContent(
    onMenuClick: () -> Unit,
    selectedConversation: Conversation?,
    onSelectConversation: (Conversation?) -> Unit,
    favoritesCount: Int = 0,
    onFavoritesClick: () -> Unit = {},
    isAdmin: Boolean = true
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE) }
    val token         = remember { prefs.getString("auth_token", "") ?: "" }
    val currentUserId = remember { prefs.getInt("user_id", 0) }

    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }
    var loadError     by remember { mutableStateOf(false) }
    var searchQuery   by remember { mutableStateOf("") }
    // "all", "unread", one of the item stages, or "archived" - the website's shelves.
    var listFilter    by remember { mutableStateOf("all") }
    val scope         = rememberCoroutineScope()

    // Long-press on a row: pin, rename, archive, delete.
    var actionTarget  by remember { mutableStateOf<Conversation?>(null) }
    var renameTarget  by remember { mutableStateOf<Conversation?>(null) }
    var deleteTarget  by remember { mutableStateOf<Conversation?>(null) }

    fun replaceConversation(updated: Conversation) {
        conversations = conversations.map {
            if (it.otherUserId == updated.otherUserId && it.itemId == updated.itemId) updated else it
        }
    }

    // Optimistic: the row moves at once, and only moves back if the server refused.
    fun changeSetting(conv: Conversation, name: String? = null, pinned: Boolean? = null, archived: Boolean? = null) {
        val updated = conv.copy(
            customName = name ?: conv.customName,
            isPinned = pinned ?: (if (archived == true) false else conv.isPinned),
            isArchived = archived ?: conv.isArchived,
        )
        replaceConversation(updated)
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                updateConversationSettings(token, conv.itemId, conv.otherUserId, name, pinned, archived)
            }
            if (!ok) replaceConversation(conv)
        }
    }

    fun removeConversation(conv: Conversation) {
        conversations = conversations.filterNot { it.otherUserId == conv.otherUserId && it.itemId == conv.itemId }
        scope.launch { withContext(Dispatchers.IO) { clearConversation(token, conv.itemId, conv.otherUserId) } }
    }

    val filteredConversations = remember(conversations, searchQuery, listFilter) {
        var list = if (searchQuery.isBlank()) conversations
        else {
            val q = searchQuery.trim().lowercase()
            conversations.filter { c ->
                c.firstName.lowercase().contains(q) ||
                        c.lastName.lowercase().contains(q) ||
                        c.itemTitle.lowercase().contains(q) ||
                        c.latestMessage.lowercase().contains(q)
            }
        }
        list = when (listFilter) {
            "unread" -> list.filter { it.unreadCount > 0 && !it.isArchived }
            "archived" -> list.filter { it.isArchived }
            "all" -> list.filter { !it.isArchived }
            // An item stage, in the words the row's badge uses.
            else -> list.filter { !it.isArchived && conversationStage(it) == listFilter }
        }
        // Pinned first; the server orders the rest by recency.
        list.sortedByDescending { it.isPinned }
    }

    // When entering a chat: mark messages as read + zero the badge locally.
    // When leaving a chat (conv == null): re-fetch so latest_message updates with polling every 5 seconds.
    LaunchedEffect(selectedConversation) {
        if (selectedConversation != null) {
            val conv = selectedConversation
            withContext(Dispatchers.IO) { markMessagesRead(token, conv.itemId) }
            conversations = conversations.map {
                if (it.otherUserId == conv.otherUserId && it.itemId == conv.itemId)
                    it.copy(unreadCount = 0) else it
            }
            return@LaunchedEffect
        }

        // Poll conversations every 5 seconds
        if (conversations.isEmpty()) isLoading = true
        while (true) {
            try {
                val result = withContext(Dispatchers.IO) { fetchConversations(token) }
                // Only redraw the list when something actually changed.
                if (result != conversations) conversations = result
                loadError = false
            } catch (_: Exception) {
                if (conversations.isEmpty()) loadError = true
            } finally {
                isLoading = false
            }
            delay(5000) // Poll every 5 seconds
        }
    }

    actionTarget?.let { conv ->
        ConversationActionsSheet(
            conversation = conv,
            onDismiss = { actionTarget = null },
            onRename = { actionTarget = null; renameTarget = conv },
            onTogglePin = { actionTarget = null; changeSetting(conv, pinned = !conv.isPinned) },
            onToggleArchive = { actionTarget = null; changeSetting(conv, archived = !conv.isArchived) },
            onDelete = { actionTarget = null; deleteTarget = conv },
        )
    }
    renameTarget?.let { conv ->
        RenameConversationDialog(
            conversation = conv,
            onDismiss = { renameTarget = null },
            onSave = { name -> renameTarget = null; changeSetting(conv, name = name) },
        )
    }
    deleteTarget?.let { conv ->
        DeleteConversationDialog(
            conversation = conv,
            onDismiss = { deleteTarget = null },
            onConfirm = { deleteTarget = null; removeConversation(conv) },
        )
    }

    // FIXED: Properly structured AnimatedContent
    AnimatedContent(
        targetState = selectedConversation,
        transitionSpec = {
            if (targetState != null) {
                // Opening a conversation — slide in from right
                slideInHorizontally(tween(300)) { it } togetherWith
                        slideOutHorizontally(tween(300)) { -it / 3 }
            } else {
                // Going back — slide in from left
                slideInHorizontally(tween(300)) { -it / 3 } togetherWith
                        slideOutHorizontally(tween(300)) { it }
            }
        },
        label = "ChatTransition"
    ) { conv ->
        if (conv != null) {
            ChatDetailContent(
                conversation  = conv,
                token         = token,
                currentUserId = currentUserId,
                onBack        = { onSelectConversation(null) },
                isAdmin       = isAdmin,
                onConversationChanged = { updated -> replaceConversation(updated) },
                onConversationDeleted = { removed -> removeConversation(removed); onSelectConversation(null) },
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                val unreadTotal = conversations.sumOf { it.unreadCount }

                AdminPageHeader(
                    title = "Messages",
                    subtitle = when {
                        unreadTotal == 1 -> "1 unread conversation"
                        unreadTotal > 1 -> "$unreadTotal unread conversations"
                        else -> if (isAdmin) "Buyers and sellers, in one place" else "Your chats with Ofelia's Store"
                    },
                    onMenuClick = onMenuClick,
                    favoritesCount = favoritesCount,
                    onFavoritesClick = onFavoritesClick,
                    showFavorites = !isAdmin,
                )

                // Search bar
                SearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = if (isAdmin) "Search students or items…" else "Search your chats…",
                    elevated = false,
                    modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.md),
                )

                // ── Filter chips ──────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.screen)
                        .padding(bottom = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    ChoiceChip(label = "All", selected = listFilter == "all", onClick = { listFilter = "all" })
                    ChoiceChip(label = "Unread", selected = listFilter == "unread", onClick = { listFilter = "unread" }, count = unreadTotal)
                    listOf(
                        "negotiating" to "Negotiating",
                        "available" to "Available",
                        "reserved" to "Reserved",
                        "sold" to "Sold",
                        "rejected" to "Rejected",
                    ).forEach { (stage, label) ->
                        ChoiceChip(label = label, selected = listFilter == stage, onClick = { listFilter = stage })
                    }
                    ChoiceChip(label = "Archived", selected = listFilter == "archived", onClick = { listFilter = "archived" })
                }

                when {
                    isLoading -> Column(modifier = Modifier.fillMaxSize()) {
                        repeat(6) { ListRowSkeleton() }
                    }
                    loadError -> ErrorState(
                        title = "Could not load conversations",
                        message = "Check your connection and try again.",
                    )
                    filteredConversations.isEmpty() -> Box(
                        Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            icon = Icons.Outlined.ChatBubbleOutline,
                            title = when {
                                searchQuery.isNotEmpty() -> "No results"
                                listFilter == "unread"   -> "All caught up"
                                listFilter == "archived" -> "Nothing archived"
                                listFilter != "all"      -> "No $listFilter items"
                                else                     -> "No conversations yet"
                            },
                            message = when {
                                searchQuery.isNotEmpty() -> "Nothing matches \"$searchQuery\"."
                                listFilter == "unread"   -> "You have read every message."
                                listFilter == "archived" -> "Long-press a conversation to archive it."
                                listFilter != "all"      -> "No conversation is about an item in that stage right now."
                                isAdmin                  -> "Chats open when a student offers an item or asks about a listing."
                                else                     -> "Ask about an item on the marketplace, or offer one to Ofelia's Store, and the chat appears here."
                            },
                        )
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = Spacing.lg),
                    ) {
                        // Pinned threads on their own shelf, the way the website lists them.
                        val pinnedRows = filteredConversations.filter { it.isPinned }
                        val otherRows = filteredConversations.filter { !it.isPinned }

                        if (pinnedRows.isNotEmpty()) {
                            item(key = "shelf_pinned") { ListShelfLabel("Pinned") }
                            items(pinnedRows, key = { "${it.otherUserId}_${it.itemId}" }) { conv ->
                                ConversationItem(
                                    conv,
                                    { onSelectConversation(conv) },
                                    isAdmin = isAdmin,
                                    currentUserId = currentUserId,
                                    onLongClick = { actionTarget = conv },
                                    onMore = { actionTarget = conv },
                                )
                            }
                            if (otherRows.isNotEmpty()) {
                                item(key = "shelf_rest") { ListShelfLabel(if (listFilter == "archived") "Archived" else "Recent") }
                            }
                        }
                        items(otherRows, key = { "${it.otherUserId}_${it.itemId}" }) { conv ->
                            ConversationItem(
                                conv,
                                { onSelectConversation(conv) },
                                isAdmin = isAdmin,
                                currentUserId = currentUserId,
                                onLongClick = { actionTarget = conv },
                                onMore = { actionTarget = conv },
                            )
                        }
                    }
                }
            }
        }
    } // end AnimatedContent
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit,
    isAdmin: Boolean = true,
    currentUserId: Int = 0,
    onLongClick: () -> Unit = {},
    /** The three-dot button: the same sheet the long-press opens. */
    onMore: () -> Unit = {},
) {
    val hasUnread = conversation.unreadCount > 0
    val accents = LocalMarketAccents.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .background(
                if (hasUnread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                else Color.Transparent
            )
            .padding(horizontal = Spacing.screen, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // The person, with the item they are talking about tucked into the
        // corner - the way a group chat shows two faces.
        Box(modifier = Modifier.size(58.dp)) {
            Avatar(
                url = conversation.profilePicture,
                initial = conversation.firstName.firstOrNull()?.toString() ?: "?",
                size = 50.dp,
                modifier = Modifier.align(Alignment.TopStart),
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                if (conversation.itemPhoto.isNotBlank()) {
                    AsyncImage(
                        model = conversation.itemPhoto,
                        contentDescription = "Item photo",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        Icons.Outlined.Image,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.width(Spacing.md))
        // Text content
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            // Row 1: item title + time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                if (conversation.isPinned) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = "Pinned",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Text(
                    conversation.displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    timeAgo(conversation.lastMessageAt),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (hasUnread) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onMore, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Conversation options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // Row 2: sender name + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text(
                    "${conversation.firstName} ${conversation.lastName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Status badge (different for admin and student)
                val statusText = conversation.itemStatus.ifBlank { "Unknown" }
                val statusLower = statusText.lowercase()

                // The same words the conversation header uses, acceptance
                // included, so the list and the thread can never disagree.
                val sellerStageLabel = when {
                    statusLower == "pending" || statusLower == "private" ->
                        if (conversation.itemOfferAccepted) "Offer accepted" else "Negotiating"
                    statusLower == "acquired" -> "Acquired"
                    else -> statusText.replaceFirstChar { it.uppercaseChar() }
                }

                val (badgeText, badgeTone) = if (isAdmin) {
                    // Admin view: "Status · Buyer/Seller"
                    val isBuyer = statusLower == "public" || statusLower == "reserved" || statusLower == "sold"
                    val categoryLabel = if (isBuyer) "Buyer" else "Seller"
                    val statusDisplay = if (isBuyer) {
                        statusText.replaceFirstChar { it.uppercaseChar() }
                    } else {
                        sellerStageLabel
                    }
                    val tone = when {
                        isBuyer -> StatusTone.Info
                        conversation.itemOfferAccepted && (statusLower == "pending" || statusLower == "private") ->
                            StatusTone.Success
                        else -> StatusTone.Warning
                    }
                    "$statusDisplay · $categoryLabel" to tone
                } else {
                    // Student view: simple status labels
                    when (statusLower) {
                        "pending", "private" ->
                            if (conversation.itemOfferAccepted) "Offer accepted" to StatusTone.Success
                            else "Negotiating" to StatusTone.Warning
                        "acquired" -> "Acquired" to StatusTone.Success
                        "public" -> "Available" to StatusTone.Success
                        "reserved" -> "Reserved" to StatusTone.Warning
                        "sold" -> "Sold" to StatusTone.Neutral
                        "rejected" -> "Not accepted" to StatusTone.Danger
                        else -> "Negotiating" to StatusTone.Neutral
                    }
                }

                StatusPill(label = badgeText, tone = badgeTone)
            }

            // Row 3: latest message + unread count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                val displayMessage = if (currentUserId == conversation.lastMessageSenderId)
                    "You: ${conversation.latestMessage}"
                else
                    conversation.latestMessage
                Text(
                    displayMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (hasUnread) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (hasUnread) {
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (conversation.unreadCount > 99) "99+" else "${conversation.unreadCount}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 86.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
private fun ChatDetailContent(
    conversation: Conversation,
    token: String,
    currentUserId: Int,
    onBack: () -> Unit,
    isAdmin: Boolean = true,
    /** The list keeps a copy of this thread; tell it when its name or pin changes here. */
    onConversationChanged: (Conversation) -> Unit = {},
    onConversationDeleted: (Conversation) -> Unit = {},
) {
    // The thread as this screen last changed it - the list's copy catches up.
    var thread                 by remember(conversation) { mutableStateOf(conversation) }
    // What happened to the thread itself - renames - as the server keeps
    // them for this account, so every device shows the same lines.
    var threadEvents           by remember(conversation) { mutableStateOf<List<ThreadEvent>>(emptyList()) }
    var replyingTo             by remember { mutableStateOf<ChatMessage?>(null) }
    var showThreadMenu         by remember { mutableStateOf(false) }
    var renameOpen             by remember { mutableStateOf(false) }
    var deleteOpen             by remember { mutableStateOf(false) }
    var messages               by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var isLoading              by remember { mutableStateOf(true) }
    var isInitialLoad          by remember { mutableStateOf(true) }  // Track initial load separately
    var fetchError             by remember { mutableStateOf(false) }
    var fetchErrorMsg          by remember { mutableStateOf("") }
    var retryTrigger           by remember { mutableStateOf(0) }   // increment to retry
    var messageText            by remember { mutableStateOf("") }
    var isSending              by remember { mutableStateOf(false) }
    var showEmojiPicker        by remember { mutableStateOf(false) }
    var chatItem               by remember { mutableStateOf<ChatItem?>(null) }
    var showItemPreview        by remember { mutableStateOf(false) }

    // The student's View Item: the same ItemDetailDialog the Home screen
    // opens - Buy Now bar and all - floating over the chat, with checkout
    // reachable right from it. Admin keeps the management page instead.
    var studentViewItem        by remember { mutableStateOf(false) }
    var chatBuyItem            by remember { mutableStateOf<Item?>(null) }
    var showEditItem           by remember { mutableStateOf(false) }
    var showConfirmDialog      by remember { mutableStateOf<String?>(null) } // "sold" or "reserved"
    var isProcessing           by remember { mutableStateOf(false) }
    var confirmMessage         by remember { mutableStateOf("") }
    var selectedPaymentMethod  by remember { mutableStateOf("cash") }
    // val pusherStatus   by pusherGlobalStatus
    // val pusherDebugLog by pusherGlobalLog
    val listState              = rememberLazyListState()
    val scope                  = rememberCoroutineScope()
    val focusManager           = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }

    // ── Pusher real-time ──────────────────────────────────────────────────────
    // Hot flow used as a thread-safe bridge between Pusher's callback thread
    // and the Compose main thread.
    // val incomingFlow = remember { MutableSharedFlow<ChatMessage>(extraBufferCapacity = 64) }

    // DisposableEffect(currentUserId, conversation.otherUserId) {
    //     val handler     = Handler(Looper.getMainLooper())
    //     val channelName = "private-conversation.$currentUserId.${conversation.otherUserId}"
    //     var pusherRef: Pusher? = null
    //
    //     try {
    //         val tokenPreview = if (token.length > 8) "${token.take(8)}…" else "(empty)"
    //         handler.post {
    //             pusherGlobalStatus.value = "connecting"
    //             pusherGlobalLog.value    = "token=$tokenPreview ch=$channelName"
    //         }
    //         Log.d("PusherSetup", "token=$tokenPreview channel=$channelName")
    //
    //         val options = PusherOptions().apply {
    //             setCluster(PUSHER_CLUSTER)
    //             setAuthorizer(BearerTokenAuthorizer(PUSHER_AUTH_URL, token))
    //         }
    //         val pusher = Pusher(PUSHER_APP_KEY, options)
    //         pusherRef = pusher
    //
    //         val channel = pusher.subscribePrivate(
    //             channelName,
    //             object : PrivateChannelEventListener {
    //                 override fun onEvent(event: PusherEvent) {}
    //                 override fun onSubscriptionSucceeded(cn: String) {
    //                     Log.d("Pusher", "Subscribed to $cn")
    //                     handler.post {
    //                         pusherGlobalStatus.value = "subscribed"
    //                         pusherGlobalLog.value    = "OK — $cn"
    //                     }
    //                 }
    //                 override fun onAuthenticationFailure(message: String, e: Exception) {
    //                     Log.e("Pusher", "Auth failed for $channelName: $message", e)
    //                     handler.post {
    //                         pusherGlobalStatus.value = "auth_failed"
    //                         pusherGlobalLog.value    = message
    //                     }
    //                 }
    //             }
    //         )
    //
    //         channel.bind("message.sent", object : PrivateChannelEventListener {
    //             override fun onEvent(event: PusherEvent) {
    //                 Log.d("Pusher", "message.sent: ${event.data}")
    //                 handler.post { pusherGlobalLog.value = "rx: ${event.data.take(60)}" }
    //                 try {
    //                     val d: JSONObject = when (val v = org.json.JSONTokener(event.data).nextValue()) {
    //                         is JSONObject -> v
    //                         is String     -> JSONObject(v)
    //                         else          -> return
    //                     }
    //                     incomingFlow.tryEmit(ChatMessage(
    //                         messageId              = d.optInt("message_id", d.optInt("id", -1)),
    //                         itemId                 = d.optInt("item_id", conversation.itemId),
    //                         itemTitle              = d.optString("item_title", conversation.itemTitle),
    //                         senderId               = d.optInt("sender_id"),
    //                         senderName             = d.optString("sender_name"),
    //                         senderProfilePicture   = d.optString("sender_profile_picture"),
    //                         receiverId             = d.optInt("receiver_id"),
    //                         receiverName           = d.optString("receiver_name"),
    //                         receiverProfilePicture = d.optString("receiver_profile_picture"),
    //                         message                = d.optString("message"),
    //                         sentAt                 = d.optString("sent_at", d.optString("created_at", ""))
    //                     ))
    //                 } catch (e: Exception) {
    //                     Log.e("Pusher", "Parse error: ${event.data}", e)
    //                     handler.post { pusherGlobalLog.value = "parse error: ${e.message}" }
    //                 }
    //             }
    //             override fun onSubscriptionSucceeded(cn: String) {}
    //             override fun onAuthenticationFailure(message: String, e: Exception) {
    //                 Log.e("Pusher", "Bind auth failed: $message", e)
    //                 handler.post {
    //                     pusherGlobalStatus.value = "auth_failed"
    //                     pusherGlobalLog.value    = message
    //                 }
    //             }
    //         })
    //
    //         pusher.connect(object : ConnectionEventListener {
    //             override fun onConnectionStateChange(change: ConnectionStateChange) {
    //                 Log.d("Pusher", "${change.previousState} → ${change.currentState}")
    //                 handler.post {
    //                     pusherGlobalStatus.value = when (change.currentState) {
    //                         ConnectionState.CONNECTING    -> "connecting"
    //                         ConnectionState.CONNECTED     -> "connected"
    //                         ConnectionState.DISCONNECTED  -> "disconnected"
    //                         ConnectionState.RECONNECTING  -> "reconnecting"
    //                         ConnectionState.DISCONNECTING -> "disconnecting"
    //                         else                          -> "unknown"
    //                     }
    //                 }
    //             }
    //             override fun onError(message: String, code: String?, e: Exception?) {
    //                 Log.e("Pusher", "Connection error: $message code=$code", e)
    //                 handler.post {
    //                     pusherGlobalStatus.value = "error"
    //                     pusherGlobalLog.value    = "conn error: $message"
    //                 }
    //             }
    //         }, ConnectionState.ALL)
    //
    //     } catch (e: Exception) {
    //         Log.e("Pusher", "Setup crashed", e)
    //         handler.post {
    //             pusherGlobalStatus.value = "error"
    //             pusherGlobalLog.value    = "setup error: ${e.message}"
    //         }
    //     }
    //
    //     onDispose {
    //         try { pusherRef?.unsubscribe(channelName) } catch (_: Exception) {}
    //         try { pusherRef?.disconnect() } catch (_: Exception) {}
    //         // post to avoid writing state synchronously during Compose apply phase
    //         handler.post {
    //             pusherGlobalStatus.value = "idle"
    //             pusherGlobalLog.value    = "disconnected"
    //         }
    //     }
    // }

    // Merge incoming Pusher messages into the UI message list
    // LaunchedEffect(incomingFlow) {
    //     incomingFlow.collect { msg ->
    //         when {
    //             // Message we sent: swap out our optimistic placeholder with the confirmed copy
    //             msg.senderId == currentUserId -> {
    //                 messages = messages.map { existing ->
    //                     if (existing.messageId == -1 &&
    //                         existing.message.trim() == msg.message.trim()) msg
    //                     else existing
    //                 }
    //             }
    //             // New message from the other participant — append if not already present
    //             messages.none { it.messageId == msg.messageId } -> {
    //                 messages = messages + msg
    //                 listState.scrollToItem(messages.size - 1)
    //             }
    //         }
    //     }
    // }
    // ─────────────────────────────────────────────────────────────────────────

    LaunchedEffect(conversation.otherUserId, conversation.itemId) {
        chatItem = withContext(Dispatchers.IO) { fetchChatItem(token, conversation.itemId) }
    }

    LaunchedEffect(conversation.otherUserId, conversation.itemId, retryTrigger) {
        if (conversation.itemId == 0) { isLoading = false; return@LaunchedEffect }

        // Poll messages every 5 seconds (hide loading during polling)
        while (true) {
            // Only show loading on initial load
            if (isInitialLoad) {
                isLoading = true
            }
            fetchError = false
            fetchErrorMsg = ""
            try {
                Log.d("ChatDetail", "Fetching messages for itemId=${conversation.itemId}, otherUserId=${conversation.otherUserId}")
                val fetched = withContext(Dispatchers.IO) {
                    fetchMessages(token, conversation.itemId, conversation.otherUserId)
                }
                Log.d("ChatDetail", "Fetched ${fetched.messages.size} messages")
                val distinct = fetched.messages.distinctBy { it.messageId }
                // Only redraw the thread when something actually changed.
                if (distinct != messages) messages = distinct
                if (fetched.events != threadEvents) threadEvents = fetched.events
            } catch (e: Exception) {
                fetchError = true
                fetchErrorMsg = e.message ?: "Unknown error"
            } finally {
                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
                // reverseLayout=true means newest message is always at index 0 (bottom)
                // — no scroll needed after load, the list is already at the correct position
            }
            delay(5000) // Poll every 5 seconds
        }
    }


    fun doSend() {
        val text = messageText.trim()
        if (text.isBlank() || isSending) return

        // Resolve receiver ID:
        // 1. Use the conversation's otherUserId if it was parsed correctly (non-zero).
        // 2. Fall back to inferring from already-loaded messages — look for a
        //    receiver_id on messages WE sent, or a sender_id on messages THEY sent.
        // This handles the case where the conversations API omits other_user_id.
        val receiverId = conversation.otherUserId.takeIf { it != 0 }
            ?: messages.firstOrNull { it.senderId == currentUserId }?.receiverId?.takeIf { it != 0 }
            ?: messages.firstOrNull { it.senderId != currentUserId }?.senderId?.takeIf { it != 0 }
            ?: return  // still unknown — don't send a broken request

        val quoted = replyingTo
        replyingTo  = null
        messageText = ""
        isSending   = true
        val nowStr = java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", java.util.Locale.US
        ).format(java.util.Date())
        val optimistic = ChatMessage(
            messageId              = -1,
            itemId                 = conversation.itemId,
            itemTitle              = conversation.itemTitle,
            senderId               = currentUserId,
            senderName             = "Me",
            senderProfilePicture   = "",
            receiverId             = receiverId,
            receiverName           = "${conversation.firstName} ${conversation.lastName}",
            receiverProfilePicture = conversation.profilePicture,
            message                = text,
            sentAt                 = nowStr,
            replyTo                = quoted?.let {
                ReplyPreview(
                    messageId  = it.messageId,
                    senderId   = it.senderId,
                    senderName = if (it.senderId == currentUserId) "You" else it.senderName,
                    text       = it.previewText(),
                    kind       = it.kind,
                )
            },
        )
        messages = messages + optimistic
        scope.launch {
            // reverseLayout=true keeps newest at index 0 (bottom) — no manual scroll needed
            withContext(Dispatchers.IO) {
                sendMessage(token, conversation.itemId, receiverId, text, quoted?.messageId?.takeIf { it > 0 })
            }
            isSending = false
        }
    }

    // Mark item as sold or reserved
    fun markItemAction(action: String) {
        isProcessing = true
        val endpoint = if (action == "sold")
            "https://fati-api.alertaraqc.com/api/admin/mark-as-sold"
        else
            "https://fati-api.alertaraqc.com/api/admin/mark-as-reserved"

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val requestBody = JSONObject()
                    requestBody.put("item_id", conversation.itemId)
                    requestBody.put("buyer_id", conversation.otherUserId)
                    if (action == "sold") {
                        requestBody.put("payment_method", selectedPaymentMethod)
                    }

                    val request = Request.Builder()
                        .url(endpoint)
                        .header("Authorization", "Bearer $token")
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                        .build()

                    adminHttpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            confirmMessage = if (action == "sold") "Item marked as sold successfully!" else "Item marked as reserved successfully!"
                        } else {
                            confirmMessage = "Failed to ${action} item. Please try again."
                        }
                    }
                }
            } catch (e: Exception) {
                confirmMessage = "Error: ${e.message ?: "Unknown error"}"
            } finally {
                isProcessing = false
                showConfirmDialog = null
            }
        }
    }

    // Back press: close emoji picker first, then go back to conversation list
    BackHandler { onBack() }
    BackHandler(enabled = showEmojiPicker) { showEmojiPicker = false }
    BackHandler(enabled = replyingTo != null && !showEmojiPicker) { replyingTo = null }

    // Housekeeping from the thread's own menu, mirrored to the list.
    fun changeThread(name: String? = null, pinned: Boolean? = null, archived: Boolean? = null) {
        val before = thread
        val updated = before.copy(
            customName = name ?: before.customName,
            isPinned = pinned ?: (if (archived == true) false else before.isPinned),
            isArchived = archived ?: before.isArchived,
        )
        thread = updated
        onConversationChanged(updated)
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                updateConversationSettings(token, before.itemId, before.otherUserId, name, pinned, archived)
            }
            if (!ok) {
                thread = before
                onConversationChanged(before)
            } else if (name != null && name != before.customName) {
                // The server writes the line in the thread; read it back
                // rather than guessing what it says.
                retryTrigger++
            }
        }
    }

    if (renameOpen) {
        RenameConversationDialog(
            conversation = thread,
            onDismiss = { renameOpen = false },
            onSave = { name -> renameOpen = false; changeThread(name = name) },
        )
    }
    if (deleteOpen) {
        DeleteConversationDialog(
            conversation = thread,
            onDismiss = { deleteOpen = false },
            onConfirm = { deleteOpen = false; onConversationDeleted(thread) },
        )
    }
    BackHandler(enabled = showItemPreview && !showEmojiPicker) { showItemPreview = false }

    if (studentViewItem) {
        chatItem?.let { item ->
            ItemDetailDialog(
                item = Item(
                    itemId = item.itemId,
                    sellerId = item.sellerId,
                    sellerEmail = item.sellerEmail,
                    title = item.title,
                    description = item.description,
                    categoryId = 0,
                    status = item.status,
                    photos = item.photos,
                    createdAt = "",
                    sellerAskingPrice = item.askingPrice,
                    acquisitionPrice = item.acquisitionPrice,
                    publicPrice = item.publicPrice,
                    rewardPoints = item.rewardPoints,
                ),
                token = token,
                isFavorited = false,
                onFavoriteToggle = { _, _ -> },
                onGoToChat = { studentViewItem = false },
                onDismiss = { studentViewItem = false },
                onBuyNow = { buyItem ->
                    studentViewItem = false
                    chatBuyItem = buyItem
                },
                currentUserId = currentUserId,
            )
        } ?: run { studentViewItem = false }
    }

    chatBuyItem?.let { buyItem ->
        CheckoutScreen(
            item = buyItem,
            onBack = { chatBuyItem = null },
        )
    }
    BackHandler(enabled = showEditItem && !showEmojiPicker && !showItemPreview) { showEditItem = false }

    // ── Item detail / Edit pages (full-screen, slides in over the chat) ────────
    AnimatedContent(
        targetState = when {
            showEditItem -> "edit"
            showItemPreview -> "view"
            else -> "chat"
        },
        transitionSpec = {
            if (targetState != "chat") {
                slideInHorizontally(tween(280)) { it } togetherWith
                        slideOutHorizontally(tween(280)) { -it / 3 }
            } else {
                slideInHorizontally(tween(280)) { -it / 3 } togetherWith
                        slideOutHorizontally(tween(280)) { it }
            }
        },
        label = "ItemPageTransition"
    ) { state ->
        when (state) {
            "edit" -> {
                chatItem?.let { item ->
                    // Rendered in the chat's unpadded area, so the form insets
                    // itself: the Save button must clear the gesture bar and
                    // rise above the keyboard.
                    Box(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
                        EditItemPage(
                            item = item,
                            token = token,
                            onBack = { showEditItem = false },
                            onItemUpdated = { updatedItem ->
                                chatItem = updatedItem
                                showEditItem = false
                            }
                        )
                    }
                } ?: run { showEditItem = false }
            }
            "view" -> {
                chatItem?.let { item ->
                    Box(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
                        ChatItemDetailPage(item = item, onBack = { showItemPreview = false })
                    }
                } ?: run { showItemPreview = false }
            }
            else -> {
                // Flat Column — mirrors Messenger's layout. No Scaffold re-measure on every
                // keyboard frame; imePadding() only re-measures this simple Column + children.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                ) {
            // ── Top bar ────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brandGradient())
            ) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Row(
                    modifier = Modifier.fillMaxWidth().height(60.dp).padding(end = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Avatar(
                        url = conversation.profilePicture,
                        initial = conversation.firstName.firstOrNull()?.toString() ?: "?",
                        size = 40.dp,
                        ringColor = Color.White.copy(alpha = 0.5f),
                        ringWidth = 1.5.dp,
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White,
                    )
                    Spacer(Modifier.width(Spacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        val personName = "${conversation.firstName} ${conversation.lastName}".trim()
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (thread.isPinned) {
                                Icon(Icons.Filled.PushPin, "Pinned", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(14.dp))
                            }
                            Text(
                                thread.customName.ifBlank { personName },
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            if (thread.customName.isBlank()) conversation.itemTitle else "$personName · ${conversation.itemTitle}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box {
                        IconButton(onClick = { showThreadMenu = true }) {
                            Icon(Icons.Filled.MoreVert, "Conversation options", tint = Color.White)
                        }
                        DropdownMenu(expanded = showThreadMenu, onDismissRequest = { showThreadMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(if (thread.isPinned) "Unpin" else "Pin to top") },
                                leadingIcon = { Icon(Icons.Outlined.PushPin, null) },
                                onClick = { showThreadMenu = false; changeThread(pinned = !thread.isPinned) },
                            )
                            DropdownMenuItem(
                                text = { Text("Rename conversation") },
                                leadingIcon = { Icon(Icons.Outlined.DriveFileRenameOutline, null) },
                                onClick = { showThreadMenu = false; renameOpen = true },
                            )
                            DropdownMenuItem(
                                text = { Text(if (thread.isArchived) "Unarchive" else "Archive") },
                                leadingIcon = { Icon(if (thread.isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive, null) },
                                onClick = { showThreadMenu = false; changeThread(archived = !thread.isArchived) },
                            )
                            DropdownMenuItem(
                                text = { Text("Delete conversation", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = { showThreadMenu = false; deleteOpen = true },
                            )
                        }
                    }
                }
                // ── Order banner ──────────────────────────────────────────────
                // Conversations are per item and per buyer, so when this thread
                // has a live order behind it the admin can settle it here
                // instead of switching to the transactions screen.
                // Both sides get the order strip now: the admin settles it,
                // the buyer pays from it and, once the order is approved,
                // carries the pickup code without going to My Orders.
                ChatOrderPanel(
                    itemId = conversation.itemId,
                    buyerId = if (isAdmin) conversation.otherUserId else currentUserId,
                    token = token,
                    onChanged = { retryTrigger++ }
                )

                // ── Offer banner ─────────────────────────────────────────────
                // The listing's own decisions - accept, decline, schedule,
                // acquire, and the seller's turnover QR - pinned above the
                // thread so a long chat can never bury them.
                ItemOfferPanel(
                    itemId = conversation.itemId,
                    expectedSellerId = if (isAdmin) conversation.otherUserId else currentUserId,
                    token = token,
                )

                // ── Item info bar (sticky, always visible) ────────────────────
                chatItem?.let { item ->
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.18f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Thumbnail
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.photos.isNotEmpty()) {
                                AsyncImage(
                                    model = item.photos.first(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Filled.Photo, null,
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                        // Title + price + status
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Which price this thread is actually about.
                                //
                                // A conversation is per item and per person.
                                // In the seller's own thread the number that
                                // matters is what the store pays them - the
                                // acquired amount - not the buyer price, and
                                // never the asking price once a figure has
                                // been agreed. A buyer's thread shows the
                                // catalog price, and never the store's cost.
                                val studentSideId = if (isAdmin) conversation.otherUserId else currentUserId
                                val isSellerThread = studentSideId == item.sellerId

                                // The label matters as much as the figure: a
                                // bare peso amount in a seller's thread was
                                // read as their own asking price.
                                val (priceToShow, priceLabel) = when {
                                    isSellerThread && item.acquisitionPrice != null ->
                                        item.acquisitionPrice to "You get"
                                    isSellerThread ->
                                        item.askingPrice to "You asked"
                                    else ->
                                        (item.publicPrice ?: item.askingPrice) to null
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Filled.MonetizationOn, null,
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(11.dp))
                                    priceLabel?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                    Text(
                                        Money.format(priceToShow),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                // Status badge (moved next to points)
                                val isPrivate = item.status.lowercase() == "pending"
                                val offerAccepted = isPrivate && item.acquisitionPrice != null
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when {
                                        offerAccepted -> LocalMarketAccents.current.success.copy(alpha = 0.55f)
                                        isPrivate -> LocalMarketAccents.current.warning.copy(alpha = 0.30f)
                                        else -> Color.White.copy(alpha = 0.20f)
                                    }
                                ) {
                                    Text(
                                        when {
                                            offerAccepted -> "Offer Accepted"
                                            isPrivate -> "Negotiating"
                                            else -> item.status.replaceFirstChar { it.uppercaseChar() }
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = Color.White,
                                        modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        // Buttons (View Item and Edit Item)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Edit Item button (visible only in admin view, user is seller, and status is private, acquired, or public)
                            val canEditItem = isAdmin && currentUserId == item.sellerId && (
                                item.status.lowercase() == "private" ||
                                item.status.lowercase() == "acquired" ||
                                item.status.lowercase() == "public"
                            )
                            if (canEditItem) {
                                OutlinedButton(
                                    onClick = { showEditItem = true },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Filled.Edit, null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Edit", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = Color.White)
                                }
                            }

                            // View Item button
                            OutlinedButton(
                                onClick = {
                                    if (isAdmin) showItemPreview = true else studentViewItem = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("View Item", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = Color.White)
                            }
                        }
                    }
                }
            } // end top bar Column

            // ── Messages ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                    val threadRows = remember(messages, threadEvents) { buildThreadRows(messages, threadEvents) }

                    // The newest order card is the one the thread is still
                    // about: the pickup code and the store's pin belong on it,
                    // not on whichever older card happened to carry buttons.
                    val liveOrderMessageId = remember(messages) {
                        messages.lastOrNull { it.order != null }?.messageId ?: 0
                    }
                    when {
                        isLoading -> LoadingState()
                        fetchError -> ErrorState(
                            title = "Could not load messages",
                            message = "Check your connection and try again.",
                            onRetry = { retryTrigger++ },
                        )
                        messages.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Icons.Outlined.ChatBubbleOutline,
                                title = "Say hello",
                                message = if (isAdmin) "Start the conversation about this item."
                                          else "Ask Ofelia's Store anything about this item.",
                            )
                        }
                        else -> LazyColumn(
                            state = listState,
                            // reverseLayout = true: item 0 anchors to bottom.
                            // Newest messages stay visible when keyboard opens —
                            // no programmatic scroll needed (mirrors Messenger).
                            reverseLayout = true,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pass reversed list so newest message is at index 0 (bottom)
                            items(threadRows.asReversed(), key = { it.key }) { row ->
                                when (row) {
                                    is ThreadRow.Line -> ChatBubble(
                                        msg = row.msg,
                                        isMe = row.msg.senderId == currentUserId,
                                        onReply = { replyingTo = it },
                                        isLiveOrderCard = row.msg.messageId == liveOrderMessageId,
                                    )
                                    is ThreadRow.System -> ThreadSystemLine(row.event)
                                }
                            }
                        }
                    }
            } // end messages Box

            // ── Input bar ───────────────────────────────────────────────────────
            Surface(
                shadowElevation = 0.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    // ── Pusher debug status bar ───────────────────────────────
                    // val (dotColor, statusLabel) = when (pusherStatus) {
                    //     "subscribed"    -> Color(0xFF4CAF50) to "Live"
                    //     "connected"     -> Color(0xFFFF9800) to "Authenticating…"
                    //     "reconnecting"  -> Color(0xFFFF9800) to "Reconnecting…"
                    //     "auth_failed"   -> Color(0xFFF44336) to "Auth failed"
                    //     "error"         -> Color(0xFFF44336) to "Error"
                    //     "disconnected",
                    //     "disconnecting" -> Color(0xFFF44336) to "Disconnected"
                    //     else            -> Color(0xFFFF9800) to "Connecting…"
                    // }
                    // Row(
                    //     modifier = Modifier
                    //         .fillMaxWidth()
                    //         .padding(horizontal = 12.dp, vertical = 3.dp),
                    //     verticalAlignment = Alignment.CenterVertically,
                    //     horizontalArrangement = Arrangement.Center
                    // ) {
                    //     Canvas(modifier = Modifier.size(7.dp)) { drawCircle(dotColor) }
                    //     Spacer(Modifier.width(5.dp))
                    //     Text(
                    //         statusLabel,
                    //         style = MaterialTheme.typography.labelSmall,
                    //         color = dotColor,
                    //         fontWeight = FontWeight.Medium
                    //     )
                    //     if (pusherDebugLog.isNotEmpty()) {
                    //         Text(
                    //             "  •  $pusherDebugLog",
                    //             style = MaterialTheme.typography.labelSmall,
                    //             color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    //             maxLines = 1,
                    //             overflow = TextOverflow.Ellipsis,
                    //             modifier = Modifier.weight(1f, fill = false)
                    //         )
                    //     }
                    // }
                    // ─────────────────────────────────────────────────────────
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Mark-as-Reserve / Mark-as-Sold used to appear here for
                    // the student seller once their item went public - but a
                    // published item belongs to the store, and its lifecycle
                    // (reserve on checkout, sold on completion) is the
                    // server's to run. The student has no say past turnover.

                    replyingTo?.let { target ->
                        ReplyComposerBar(target = target, currentUserId = currentUserId, onDismiss = { replyingTo = null })
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji button — toggles picker panel; restores keyboard when closing
                        IconButton(onClick = {
                            if (showEmojiPicker) {
                                showEmojiPicker = false
                                scope.launch { textFieldFocusRequester.requestFocus() }
                            } else {
                                showEmojiPicker = true
                                focusManager.clearFocus()
                            }
                        }) {
                            Icon(
                                if (showEmojiPicker) Icons.Outlined.Keyboard else Icons.Outlined.EmojiEmotions,
                                contentDescription = if (showEmojiPicker) "Keyboard" else "Emoji",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Text field
                        androidx.compose.foundation.text.BasicTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 42.dp)
                                .focusRequester(textFieldFocusRequester)
                                .onFocusChanged { if (it.isFocused) showEmojiPicker = false }
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    RoundedCornerShape(21.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 0.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { doSend() }),
                            maxLines = 4,
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.defaultMinSize(minHeight = 42.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (messageText.isEmpty()) {
                                        Text(
                                            "Type a message…",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        Spacer(Modifier.width(Spacing.sm))

                        // Send button
                        RoundIconButton(
                            icon = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            onClick = { doSend() },
                            size = 42.dp,
                            enabled = messageText.isNotBlank(),
                            loading = isSending,
                        )
                    }
                    // Emoji picker panel — shown when emoji button is toggled
                    if (showEmojiPicker) {
                        EmojiPickerPanel(onEmojiClick = { messageText += it })
                    }
                }
            }
                } // end outer Column (chat view)
                } // end else (chat view)
        } // end when (AnimatedContent state)
    } // end AnimatedContent

    // Confirmation dialog for mark as sold/reserved
    if (showConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { if (!isProcessing) showConfirmDialog = null },
            title = {
                Text(if (showConfirmDialog == "sold") "Mark as Sold?" else "Mark as Reserved?")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (showConfirmDialog == "sold")
                            "Are you sure you want to mark this item as sold?"
                        else
                            "Are you sure you want to mark this item as reserved?"
                    )

                    if (showConfirmDialog == "sold") {
                        var expanded by remember { mutableStateOf(false) }
                        // Cash and GCash are the only ways to pay. "Points" and
                        // "trade" were offered here long after the server stopped
                        // accepting them - it folds anything but GCash into cash -
                        // so picking either quietly recorded a full-price cash
                        // sale. Points are a discount the buyer applies at their
                        // own checkout, not a method Admin can choose for them.
                        val paymentOptions = listOf("cash", "gcash")
                        val paymentLabels = mapOf(
                            "cash" to "Cash at store",
                            "gcash" to "GCash",
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Payment Method:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(6.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { expanded = !expanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(paymentLabels[selectedPaymentMethod] ?: selectedPaymentMethod)
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    paymentOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(paymentLabels[option] ?: option) },
                                            onClick = {
                                                selectedPaymentMethod = option
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Confirm",
                    compact = true,
                    loading = isProcessing,
                    onClick = { markItemAction(showConfirmDialog!!) },
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = null },
                    enabled = !isProcessing
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Success/Error message dialog
    if (confirmMessage.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { confirmMessage = "" },
            title = { Text("Status Update") },
            text = { Text(confirmMessage) },
            confirmButton = {
                PrimaryButton(text = "OK", compact = true, onClick = { confirmMessage = "" })
            }
        )
    }
}

@Composable
private fun EmojiPickerPanel(onEmojiClick: (String) -> Unit) {
    // selectedIndex drives which category slug is requested
    var selectedIndex    by remember { mutableStateOf(0) }
    var categoryEmojis   by remember { mutableStateOf<List<EmojiItem>>(emptyList()) }
    var isLoading        by remember { mutableStateOf(false) }

    // Fetch only the selected category; result is cached in emojiCache at module level
    LaunchedEffect(selectedIndex) {
        isLoading = true
        val (_, slug) = emojiCategories[selectedIndex]
        categoryEmojis = try { fetchEmojisByCategory(slug) } catch (_: Exception) { emptyList() }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 0.dp
        ) {
            emojiCategories.forEachIndexed { index, (label, _) ->
                Tab(
                    selected = index == selectedIndex,
                    onClick = { selectedIndex = index },
                    text = {
                        Text(
                            label.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(44.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(categoryEmojis) { emoji ->
                    val char = emoji.htmlCode.firstOrNull()?.let { htmlCodeToChar(it) } ?: ""
                    if (char.isNotEmpty()) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { onEmojiClick(char) }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Item Detail Page (shown when "View Item" is tapped in chat) ────────────────

@Composable
private fun ChatItemDetailPage(item: ChatItem, onBack: () -> Unit) {
    var currentImageIndex by remember { mutableStateOf(0) }
    BackHandler(onBack = onBack)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ────────────────────────────────────────────────────────
            MarketPageTopBar(title = "Item details", onBack = onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Photo viewer
                if (item.photos.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        AsyncImage(
                            model = item.photos[currentImageIndex],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (item.photos.size > 1) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp),
                                color = Color.Black.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    "${currentImageIndex + 1} / ${item.photos.size}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    // Thumbnail strip
                    if (item.photos.size > 1) {
                        LazyRow(
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(item.photos) { index, url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                                width = if (index == currentImageIndex) 2.dp else 1.dp,
                                                color = if (index == currentImageIndex) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { currentImageIndex = index }
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
                // Info section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.MonetizationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        Text(
                            Money.format(item.publicPrice ?: item.askingPrice),
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    if (item.publicPrice != null && item.rewardPoints > 0) {
                        Text(
                            LoyaltyRules.rewardLabel(item.rewardPoints),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val statusColor = when (item.status.lowercase()) {
                        "approved" -> MaterialTheme.colorScheme.primary
                        "rejected" -> MaterialTheme.colorScheme.error
                        else       -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Surface(shape = RoundedCornerShape(50), color = statusColor.copy(alpha = 0.12f)) {
                        Text(
                            item.status.replaceFirstChar { it.uppercaseChar() },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    }
                    HorizontalDivider()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Person, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                        Text(item.sellerEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (item.description.isNotBlank()) {
                        HorizontalDivider()
                        Text("Description", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Text(item.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

/**
 * The item's words and shelf: name, description, category. Shared by both
 * edit pages so a listing can be corrected from the chat or the inventory
 * alike - the same fields the website's editor offers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDetailsEditor(
    token: String,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    categoryId: Int,
    onCategoryChange: (Int) -> Unit,
) {
    var categories by remember { mutableStateOf<List<MarketCategory>>(emptyList()) }
    var menuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        when (val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchCategories(token) }) {
            is MarketplaceApi.Result.Ok -> categories = result.value
            is MarketplaceApi.Result.Failure -> Unit
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        MarketTextField(
            value = title,
            onValueChange = { if (it.length <= 255) onTitleChange(it) },
            label = "Name",
        )
        MarketTextField(
            value = description,
            onValueChange = { if (it.length <= 1000) onDescriptionChange(it) },
            label = "Description",
            singleLine = false,
            minLines = 2,
            maxLines = 5,
        )
        Column {
            Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        categories.firstOrNull { it.categoryId == categoryId }?.name
                            ?: if (categories.isEmpty()) "Loading categories…" else "Keep current category",
                        modifier = Modifier.weight(1f),
                    )
                    Icon(Icons.Filled.ArrowDropDown, null)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                    DropdownMenuItem(text = { Text("Keep current category") }, onClick = { onCategoryChange(0); menuOpen = false })
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name, fontWeight = if (category.categoryId == categoryId) FontWeight.Bold else FontWeight.Normal) },
                            onClick = { onCategoryChange(category.categoryId); menuOpen = false },
                        )
                    }
                }
            }
        }
    }
}

/**
 * "Items acquired" and "Items sold", read from the inventory itself - only
 * the items in that status, with the figures the website's report shows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemsReportContent(title: String, status: String, onMenuClick: () -> Unit) {
    val context = LocalContext.current
    val token = remember { context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE).getString("auth_token", "") ?: "" }
    var items by remember { mutableStateOf<List<Item>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableStateOf(0) }
    val accents = LocalMarketAccents.current
    val sold = status == "sold"

    LaunchedEffect(reload) {
        error = null
        when (val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchAdminItems(token, status) }) {
            is MarketplaceApi.Result.Ok -> items = result.value.filter { it.status.equals(status, true) }
            is MarketplaceApi.Result.Failure -> error = result.message
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AdminPageHeader(
            title = title,
            subtitle = if (sold) "Everything that has left the store, with its price and markup"
                       else "Every item the store has taken in and what it agreed to pay",
            onMenuClick = onMenuClick,
        )

        val rows = items
        when {
            error != null -> ErrorState(message = error!!, onRetry = { reload++ })
            rows == null -> LoadingState()
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Spacing.screen, vertical = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item(key = "summary") {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        StatTile(
                            label = if (sold) "Total items sold" else "Total items acquired",
                            value = "${rows.size}",
                            icon = if (sold) Icons.Filled.DoneAll else Icons.Filled.Inventory,
                            tint = if (sold) accents.success else accents.info,
                            modifier = Modifier.weight(1f),
                        )
                        if (sold) {
                            val markup = rows.sumOf { Money.parse(it.markup)?.toDouble() ?: 0.0 }
                            StatTile(
                                label = "Profit from markup",
                                value = Money.format(String.format(java.util.Locale.US, "%.2f", markup)),
                                icon = Icons.Filled.Stars,
                                tint = accents.reward,
                                modifier = Modifier.weight(1f),
                            )
                        } else {
                            val unpaid = rows.count { !it.sellerIsPaid }
                            StatTile(
                                label = "Sellers unpaid",
                                value = "$unpaid",
                                icon = Icons.Filled.Payments,
                                tint = if (unpaid > 0) accents.warning else accents.success,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                if (rows.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            icon = if (sold) Icons.Filled.DoneAll else Icons.Filled.Inventory,
                            title = if (sold) "Nothing sold yet" else "Nothing acquired yet",
                            message = if (sold) "Items appear here once an order for them is completed."
                                      else "Items appear here once their turnover is verified.",
                        )
                    }
                }
                items(rows, key = { it.itemId }) { row ->
                    MarketCard(contentPadding = PaddingValues(Spacing.md)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(52.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (row.photos.isNotEmpty()) {
                                    AsyncImage(model = row.photos.first(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                } else {
                                    Icon(Icons.Outlined.Image, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(row.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(row.sellerEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (!sold) {
                                StatusPill(
                                    label = if (row.sellerIsPaid) "Paid" else "Unpaid",
                                    tone = if (row.sellerIsPaid) StatusTone.Success else StatusTone.Warning,
                                )
                            }
                        }
                        SoftDivider()
                        if (sold) {
                            SummaryRow("Sold for", Money.format(row.publicPrice))
                            SummaryRow("Acquisition", Money.format(row.acquisitionPrice))
                            SummaryRow("Markup", Money.format(row.markup), valueColor = accents.success)
                        } else {
                            SummaryRow("Acquisition price", Money.format(row.acquisitionPrice))
                            SummaryRow("Acquired", row.acquiredAt?.let { formatDate(it) } ?: "—")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditItemPage(
    item: ChatItem,
    token: String,
    onBack: () -> Unit,
    onItemUpdated: (ChatItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    // The listing's photos, kept current by the photo editor below.
    var photos by remember { mutableStateOf(item.photos) }
    // The words and the shelf: editable here, like on the website.
    var editTitle by remember { mutableStateOf(item.title) }
    var editDescription by remember { mutableStateOf(item.description) }
    var editCategoryId by remember { mutableStateOf(0) }

    // Status dropdown
    var expanded by remember { mutableStateOf(false) }
    var editStatus by remember { mutableStateOf(item.status) }
    // Shared with the counter screen, so both offer the same choices.
    val statusOptions = ITEM_STATUS_OPTIONS

    // The public selling price, in pesos. Reward points are derived from it by
    // the server; the figure shown here is only a preview.
    var editPublicPrice by remember {
        mutableStateOf(Money.formatPlain(item.publicPrice).replace(",", "").takeIf { it != "—" } ?: "")
    }
    val rewardPreview = LoyaltyRules.rewardPointsFor(Money.normalizeInput(editPublicPrice))
    val canEditPrice = editStatus.lowercase() in listOf("acquired", "public")
    val acquisitionRef = item.acquisitionPrice
    val turnoverVerifiedRef = item.isTurnoverVerified

    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var saveSuccess by remember { mutableStateOf(false) }
    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog   by remember { mutableStateOf(false) }
    var dialogErrorMsg    by remember { mutableStateOf("") }
    // Holds updated item until user dismisses the success dialog
    var savedChatItem     by remember { mutableStateOf<ChatItem?>(null) }

    BackHandler(onBack = onBack)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ────────────────────────────────────────────────────────
            MarketPageTopBar(title = "Edit item", onBack = onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AdminItemPhotoViewer(photos)

                // Editable info section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ItemDetailsEditor(
                        token = token,
                        title = editTitle,
                        onTitleChange = { editTitle = it },
                        description = editDescription,
                        onDescriptionChange = { editDescription = it },
                        categoryId = editCategoryId,
                        onCategoryChange = { editCategoryId = it },
                    )
                    HorizontalDivider()

                    // Ofelia's own photos. Editable until the item is sold or rejected.
                    if (item.status.lowercase() !in listOf("sold", "rejected")) {
                        AdminItemPhotoEditor(
                            itemId = item.itemId,
                            token = token,
                            onPhotosChanged = { photos = it },
                        )
                        HorizontalDivider()
                    }

                    // Seller asking price - read-only reference
                    Text("Seller Asking Price", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.MonetizationOn, null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                Money.format(item.askingPrice),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Status field (dropdown)
                    Text("Status", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editStatus.replaceFirstChar { it.uppercaseChar() },
                            onValueChange = {},
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            statusOptions.forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            status.replaceFirstChar { it.uppercaseChar() },
                                            fontWeight = if (status == editStatus) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        editStatus = status
                                        expanded = false
                                        saveError = null
                                    }
                                )
                            }
                        }
                    }

                    // Public selling price, with the reward the buyer will earn.
                    Text("Public Selling Price (${Money.PESO})", style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (canEditPrice) {
                        OutlinedTextField(
                            value = editPublicPrice,
                            onValueChange = {
                                if (Money.isValidPriceInput(it)) { editPublicPrice = it; saveError = null }
                            },
                            leadingIcon = { Text(Money.PESO, style = MaterialTheme.typography.titleMedium) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        // Reward preview and expected profit, both derived from
                        // the price above. The server recalculates on publish.
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Buyer earns", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "$rewardPreview point${if (rewardPreview == 1) "" else "s"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                acquisitionRef?.let { acquired ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Acquisition price", style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(Money.format(acquired), style = MaterialTheme.typography.bodySmall)
                                    }
                                    val priced = Money.parse(Money.normalizeInput(editPublicPrice))
                                    val acq = Money.parse(acquired)
                                    if (priced != null && acq != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Expected profit", style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                Money.format(priced.subtract(acq).toPlainString()),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = LocalMarketAccents.current.info
                                            )
                                        }
                                    }
                                }
                                if (!turnoverVerifiedRef) {
                                    Text(
                                        "This item cannot be published until it has been received and verified.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LocalMarketAccents.current.warning
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.TrendingUp, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    Money.format(item.publicPrice),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Person, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                        Text(item.sellerEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (item.description.isNotBlank()) {
                        HorizontalDivider()
                        Text("Description", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Text(item.description, style = MaterialTheme.typography.bodyMedium)
                    }

                    HorizontalDivider()

                    // ── Success Dialog ────────────────────────────────────────
                    if (showSuccessDialog) {
                        AlertDialog(
                            onDismissRequest = { showSuccessDialog = false },
                            icon = {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            },
                            title = { Text("Success", fontWeight = FontWeight.Bold) },
                            text = { Text("Item has been updated successfully.") },
                            confirmButton = {
                                PrimaryButton(
                                    text = "Back",
                                    compact = true,
                                    onClick = {
                                        showSuccessDialog = false
                                        savedChatItem?.let { onItemUpdated(it) }
                                        onBack()
                                    },
                                )
                            },
                            dismissButton = {
                                SecondaryButton(text = "Close", compact = true, onClick = { showSuccessDialog = false })
                            }
                        )
                    }

                    // ── Error Dialog ──────────────────────────────────────────
                    if (showErrorDialog) {
                        AlertDialog(
                            onDismissRequest = { showErrorDialog = false },
                            icon = {
                                Icon(
                                    Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(36.dp)
                                )
                            },
                            title = { Text("Update Failed", fontWeight = FontWeight.Bold) },
                            text = { Text(dialogErrorMsg) },
                            confirmButton = {
                                PrimaryButton(
                                    text = "Close",
                                    compact = true,
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                    onClick = { showErrorDialog = false },
                                )
                            }
                        )
                    }

                    // Save button
                    PrimaryButton(
                        text = "Save Changes",
                        icon = Icons.Filled.Save,
                        onClick = {
                            saveError = null
                            if (editStatus.isBlank()) {
                                dialogErrorMsg = "Status cannot be empty."
                                showErrorDialog = true
                                return@PrimaryButton
                            }
                            // Publishing needs a real peso price, and the server
                            // additionally refuses if turnover is unverified.
                            val normalizedPrice = Money.normalizeInput(editPublicPrice)
                            if (canEditPrice && editPublicPrice.isNotBlank() && normalizedPrice == null) {
                                dialogErrorMsg = "Enter a valid selling price, e.g. 250 or 249.50."
                                showErrorDialog = true
                                return@PrimaryButton
                            }
                            if (editStatus.lowercase() == "public" && normalizedPrice == null) {
                                dialogErrorMsg = "A public selling price is required before publishing."
                                showErrorDialog = true
                                return@PrimaryButton
                            }
                            scope.launch {
                                isSaving = true
                                val (ok, errMsg) = withContext(Dispatchers.IO) {
                                    updateAdminItem(
                                        token,
                                        item.itemId,
                                        status = editStatus,
                                        publicPrice = normalizedPrice,
                                        title = editTitle.trim().takeIf { it.isNotBlank() && it != item.title },
                                        description = editDescription.trim().takeIf { it != item.description },
                                        categoryId = editCategoryId.takeIf { it > 0 },
                                    )
                                }
                                isSaving = false
                                if (ok) {
                                    saveSuccess = true
                                    savedChatItem = item.copy(
                                        title = editTitle.trim().ifBlank { item.title },
                                        description = editDescription.trim(),
                                        status = editStatus,
                                        publicPrice = normalizedPrice ?: item.publicPrice,
                                        rewardPoints = LoyaltyRules.rewardPointsFor(
                                            normalizedPrice ?: item.publicPrice
                                        ),
                                        photos = photos,
                                    )
                                    // Show dialog FIRST — onItemUpdated is called from Back button
                                    showSuccessDialog = true
                                } else {
                                    dialogErrorMsg = errMsg.ifBlank { "Failed to update. Please try again." }
                                    showErrorDialog = true
                                }
                            }
                        },
                        enabled = !isSaving && !saveSuccess,
                        loading = isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditItemPageForList(
    item: Item,
    token: String,
    onBack: () -> Unit,
    onItemUpdated: (ChatItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    // The listing's photos, kept current by the photo editor below.
    var photos by remember { mutableStateOf(item.photos) }
    // The words and the shelf: editable here, like on the website.
    var editTitle by remember { mutableStateOf(item.title) }
    var editDescription by remember { mutableStateOf(item.description) }
    var editCategoryId by remember { mutableStateOf(0) }

    // Status dropdown
    var expanded by remember { mutableStateOf(false) }
    var editStatus by remember { mutableStateOf(item.status) }
    // Shared with the counter screen, so both offer the same choices.
    val statusOptions = ITEM_STATUS_OPTIONS

    // The public selling price, in pesos. Reward points are derived from it by
    // the server; the figure shown here is only a preview.
    var editPublicPrice by remember {
        mutableStateOf(Money.formatPlain(item.publicPrice).replace(",", "").takeIf { it != "—" } ?: "")
    }
    val rewardPreview = LoyaltyRules.rewardPointsFor(Money.normalizeInput(editPublicPrice))
    val canEditPrice = editStatus.lowercase() in listOf("acquired", "public")
    val acquisitionRef = item.acquisitionPrice
    val turnoverVerifiedRef = item.isTurnoverVerified

    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var saveSuccess by remember { mutableStateOf(false) }
    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog  by remember { mutableStateOf(false) }
    var dialogErrorMsg   by remember { mutableStateOf("") }
    // Holds the updated item until the user dismisses the success dialog
    var savedChatItem    by remember { mutableStateOf<ChatItem?>(null) }

    BackHandler(onBack = onBack)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ────────────────────────────────────────────────────────
            MarketPageTopBar(title = "Edit item", onBack = onBack)
            // ── Scrollable content ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AdminItemPhotoViewer(photos)

                // Editable info section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ItemDetailsEditor(
                        token = token,
                        title = editTitle,
                        onTitleChange = { editTitle = it },
                        description = editDescription,
                        onDescriptionChange = { editDescription = it },
                        categoryId = editCategoryId,
                        onCategoryChange = { editCategoryId = it },
                    )
                    HorizontalDivider()

                    // Ofelia's own photos. Editable until the item is sold or rejected.
                    if (item.status.lowercase() !in listOf("sold", "rejected")) {
                        AdminItemPhotoEditor(
                            itemId = item.itemId,
                            token = token,
                            onPhotosChanged = { photos = it },
                        )
                        HorizontalDivider()
                    }

                    // Seller asking price - read-only reference, hidden once the
                    // item is on the public catalog and the selling price rules.
                    if (editStatus.lowercase() != "public") {
                        Text("Seller Asking Price", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.MonetizationOn, null,
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    item.displayAskingPrice,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Status field (dropdown)
                    Text("Status", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editStatus.replaceFirstChar { it.uppercaseChar() },
                            onValueChange = {},
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            statusOptions.forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            status.replaceFirstChar { it.uppercaseChar() },
                                            fontWeight = if (status == editStatus) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        editStatus = status
                                        expanded = false
                                        saveError = null
                                    }
                                )
                            }
                        }
                    }

                    // Public selling price. Reward points are derived from it by
                    // the server; what is shown here is a preview only.
                    Text("Public Selling Price (${Money.PESO})", style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (canEditPrice) {
                        OutlinedTextField(
                            value = editPublicPrice,
                            onValueChange = {
                                if (Money.isValidPriceInput(it)) { editPublicPrice = it; saveError = null }
                            },
                            leadingIcon = { Text(Money.PESO, style = MaterialTheme.typography.titleMedium) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Buyer earns", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "$rewardPreview point${if (rewardPreview == 1) "" else "s"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                acquisitionRef?.let { acquired ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Acquisition price", style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(Money.format(acquired), style = MaterialTheme.typography.bodySmall)
                                    }
                                    val priced = Money.parse(Money.normalizeInput(editPublicPrice))
                                    val acq = Money.parse(acquired)
                                    if (priced != null && acq != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Expected profit", style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                Money.format(priced.subtract(acq).toPlainString()),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = LocalMarketAccents.current.info
                                            )
                                        }
                                    }
                                }
                                if (!turnoverVerifiedRef) {
                                    Text(
                                        "This item cannot be published until it has been received and verified.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LocalMarketAccents.current.warning
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.TrendingUp, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = item.publicPrice?.let { Money.format(it) } ?: "Not set",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider()
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Person, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                        Text(item.sellerEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    if (item.description.isNotBlank()) {
                        HorizontalDivider()
                        Text("Description", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Text(item.description, style = MaterialTheme.typography.bodyMedium)
                    }

                    HorizontalDivider()

                    // ── Success Dialog ─────────────────────────────────────────
                    if (showSuccessDialog) {
                        AlertDialog(
                            onDismissRequest = { showSuccessDialog = false },
                            icon = {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            },
                            title = { Text("Success", fontWeight = FontWeight.Bold) },
                            text = { Text("Item has been updated successfully.") },
                            confirmButton = {
                                PrimaryButton(
                                    text = "Back",
                                    compact = true,
                                    onClick = {
                                        showSuccessDialog = false
                                        savedChatItem?.let { onItemUpdated(it) }
                                        onBack()
                                    },
                                )
                            },
                            dismissButton = {
                                SecondaryButton(text = "Close", compact = true, onClick = { showSuccessDialog = false })
                            }
                        )
                    }

                    // ── Error Dialog ───────────────────────────────────────────
                    if (showErrorDialog) {
                        AlertDialog(
                            onDismissRequest = { showErrorDialog = false },
                            icon = {
                                Icon(
                                    Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(36.dp)
                                )
                            },
                            title = { Text("Update Failed", fontWeight = FontWeight.Bold) },
                            text = { Text(dialogErrorMsg) },
                            confirmButton = {
                                PrimaryButton(
                                    text = "Close",
                                    compact = true,
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                    onClick = { showErrorDialog = false },
                                )
                            }
                        )
                    }

                    // Save button
                    PrimaryButton(
                        text = "Save Changes",
                        icon = Icons.Filled.Save,
                        onClick = {
                            saveError = null
                            if (editStatus.isBlank()) {
                                dialogErrorMsg = "Status cannot be empty."
                                showErrorDialog = true
                                return@PrimaryButton
                            }
                            // Publishing needs a real peso price, and the server
                            // additionally refuses if turnover is unverified.
                            val normalizedPrice = Money.normalizeInput(editPublicPrice)
                            if (canEditPrice && editPublicPrice.isNotBlank() && normalizedPrice == null) {
                                dialogErrorMsg = "Enter a valid selling price, e.g. 250 or 249.50."
                                showErrorDialog = true
                                return@PrimaryButton
                            }
                            if (editStatus.lowercase() == "public" && normalizedPrice == null) {
                                dialogErrorMsg = "A public selling price is required before publishing."
                                showErrorDialog = true
                                return@PrimaryButton
                            }
                            scope.launch {
                                isSaving = true
                                val (ok, errMsg) = withContext(Dispatchers.IO) {
                                    updateAdminItem(
                                        token,
                                        item.itemId,
                                        status = editStatus,
                                        publicPrice = normalizedPrice,
                                        title = editTitle.trim().takeIf { it.isNotBlank() && it != item.title },
                                        description = editDescription.trim().takeIf { it != item.description },
                                        categoryId = editCategoryId.takeIf { it > 0 },
                                    )
                                }
                                isSaving = false
                                if (ok) {
                                    saveSuccess = true
                                    savedChatItem = ChatItem(
                                        itemId = item.itemId,
                                        title = editTitle.trim().ifBlank { item.title },
                                        description = editDescription.trim(),
                                        askingPrice = item.sellerAskingPrice,
                                        acquisitionPrice = item.acquisitionPrice,
                                        publicPrice = normalizedPrice ?: item.publicPrice,
                                        rewardPoints = LoyaltyRules.rewardPointsFor(
                                            normalizedPrice ?: item.publicPrice
                                        ),
                                        sellerPayoutStatus = item.sellerPayoutStatus,
                                        isTurnoverVerified = item.isTurnoverVerified,
                                        sellerId = item.sellerId,
                                        sellerEmail = item.sellerEmail,
                                        status = editStatus,
                                        photos = photos
                                    )
                                    // Show dialog FIRST — onItemUpdated is called from the Back button
                                    showSuccessDialog = true
                                } else {
                                    dialogErrorMsg = errMsg.ifBlank { "Failed to update. Please try again." }
                                    showErrorDialog = true
                                }
                            }
                        },
                        enabled = !isSaving && !saveSuccess,
                        loading = isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

/** What a quote or a reply strip shows for a line: its text, or what kind of card it is. */
private fun ChatMessage.previewText(): String = when {
    isOrderCard -> "Order card"
    isItemCard -> "Item offer"
    isAcquiredCard -> "Item received"
    else -> message
}

/**
 * One line of the thread.
 *
 * Swipe a text bubble towards the middle of the screen - or long-press it -
 * to answer it, the way Messenger does: the bubble follows the finger, a
 * reply arrow grows behind it, and past the threshold the composer picks the
 * line up. A reply carries the quoted line above its own text.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBubble(
    msg: ChatMessage,
    isMe: Boolean,
    onReply: ((ChatMessage) -> Unit)? = null,
    /** True for the newest order card, which is the one still being acted on. */
    isLiveOrderCard: Boolean = false,
) {
    // An order is a card, not a sentence: the item, its photo, what is owed,
    // how it is being paid and whether that has happened yet.
    if (msg.isOrderCard) {
        ChatOrderCard(msg = msg, isMe = isMe, isLiveOrderCard = isLiveOrderCard)
        return
    }

    // A fresh listing is an offer card: the item, its asking price, and the
    // review decisions on Admin's side.
    if (msg.isItemCard) {
        ItemOfferCard(msg = msg, isMe = isMe)
        return
    }

    // The other end of that offer: the item is in the store, the seller has
    // been settled, and the counter's two photographs are the proof.
    if (msg.isAcquiredCard) {
        ItemAcquiredCard(msg = msg, isMe = isMe)
        return
    }

    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val thresholdPx = with(density) { 64.dp.toPx() }
    val maxPx = with(density) { 96.dp.toPx() }
    var dragX by remember(msg.messageId) { mutableStateOf(0f) }
    var armed by remember(msg.messageId) { mutableStateOf(false) }
    val shownX by animateFloatAsState(targetValue = dragX, label = "swipeReply")
    val progress = (abs(shownX) / thresholdPx).coerceIn(0f, 1f)
    // Mine sit on the right and swipe left; theirs sit on the left and swipe right.
    val towardsMiddle = if (isMe) -1f else 1f

    Box(modifier = Modifier.fillMaxWidth()) {
        if (onReply != null && progress > 0.05f) {
            Box(
                modifier = Modifier
                    .align(if (isMe) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(horizontal = 8.dp)
                    .size(30.dp)
                    .graphicsLayer {
                        alpha = progress
                        scaleX = 0.6f + 0.4f * progress
                        scaleY = scaleX
                    }
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Reply,
                    contentDescription = "Reply",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(shownX.roundToInt(), 0) }
                .then(
                    if (onReply == null) Modifier else Modifier.pointerInput(msg.messageId) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (armed) onReply(msg)
                                dragX = 0f
                                armed = false
                            },
                            onDragCancel = {
                                dragX = 0f
                                armed = false
                            },
                        ) { change, delta ->
                            val next = dragX + delta
                            val clamped = if (towardsMiddle > 0) next.coerceIn(0f, maxPx) else next.coerceIn(-maxPx, 0f)
                            if (clamped != dragX) change.consume()
                            dragX = clamped
                            val past = abs(dragX) >= thresholdPx
                            if (past && !armed) {
                                armed = true
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            } else if (!past) {
                                armed = false
                            }
                        }
                    }
                ),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom,
        ) {
            if (!isMe) {
                Avatar(
                    url = msg.senderProfilePicture,
                    initial = msg.senderName.firstOrNull()?.toString() ?: "?",
                    size = 30.dp,
                )
                Spacer(Modifier.width(Spacing.sm))
            }

            Column(
                modifier = Modifier.widthIn(max = 280.dp),
                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isMe) 18.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 18.dp
                    ),
                    color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    shadowElevation = if (isMe) Elevation.flat else Elevation.card,
                    modifier = if (onReply == null) Modifier else Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onReply(msg)
                        },
                    ),
                ) {
                    Column {
                        msg.replyTo?.let { QuotedLine(quote = it, onPrimary = isMe) }
                        Text(
                            msg.message,
                            color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                }

                Text(
                    timeAgo(msg.sentAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(
                        top = 3.dp,
                        start = if (isMe) 0.dp else 4.dp,
                        end = if (isMe) 4.dp else 0.dp
                    )
                )
            }

            if (isMe) Spacer(Modifier.width(Spacing.xs))
        }
    }
}

/** The quoted line inside a reply bubble: a coloured bar, who said it, and what. */
@Composable
private fun QuotedLine(quote: ReplyPreview, onPrimary: Boolean) {
    val bar = if (onPrimary) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f) else MaterialTheme.colorScheme.primary
    val fill = if (onPrimary) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    val ink = if (onPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(fill)
            .height(IntrinsicSize.Min),
    ) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(bar))
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                quote.senderName.ifBlank { "Message" },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = ink.copy(alpha = 0.92f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                quote.text.ifBlank { quote.kind.replace('_', ' ').replaceFirstChar { it.uppercaseChar() } },
                style = MaterialTheme.typography.bodySmall,
                color = ink.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The strip above the composer while a reply is being written. */
@Composable
private fun ReplyComposerBar(target: ChatMessage, currentUserId: Int, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(3.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Replying to " + (if (target.senderId == currentUserId) "yourself" else target.senderName.ifBlank { "message" }),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                target.previewText(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = "Cancel reply", modifier = Modifier.size(18.dp))
        }
    }
}

/** Long-press on a conversation: pin, rename, archive, delete - for this person only. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationActionsSheet(
    conversation: Conversation,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = Spacing.xl)) {
            Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)) {
                Text(conversation.displayTitle, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "with ${conversation.firstName} ${conversation.lastName}".trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SettingsRow(
                icon = Icons.Outlined.PushPin,
                title = if (conversation.isPinned) "Unpin" else "Pin to top",
                subtitle = if (conversation.isPinned) "Back to its place by date" else "Keeps it at the top of the list",
                onClick = onTogglePin,
            )
            SettingsRow(
                icon = Icons.Outlined.DriveFileRenameOutline,
                title = "Rename conversation",
                subtitle = conversation.customName.ifBlank { "Named after the item: ${conversation.itemTitle}" },
                onClick = onRename,
            )
            SettingsRow(
                icon = if (conversation.isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                title = if (conversation.isArchived) "Unarchive" else "Archive",
                subtitle = if (conversation.isArchived) "Back to the inbox" else "Out of the way until someone writes",
                onClick = onToggleArchive,
            )
            SettingsRow(
                icon = Icons.Outlined.DeleteOutline,
                title = "Delete conversation",
                subtitle = "Clears it for you only",
                tint = MaterialTheme.colorScheme.error,
                titleColor = MaterialTheme.colorScheme.error,
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun RenameConversationDialog(conversation: Conversation, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(conversation.customName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename conversation") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    "Only you see this name. Leave it blank to use the item's title again.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                MarketTextField(
                    value = name,
                    onValueChange = { if (it.length <= 80) name = it },
                    label = "Name",
                    placeholder = conversation.itemTitle,
                )
            }
        },
        confirmButton = {
            PrimaryButton(text = "Save", compact = true, onClick = { onSave(name.trim()) })
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun DeleteConversationDialog(conversation: Conversation, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(30.dp)) },
        title = { Text("Delete this conversation?") },
        text = {
            Text(
                "\"${conversation.displayTitle}\" is cleared from your list. ${conversation.firstName.ifBlank { "The other person" }} keeps their copy, and the chat comes back here if either of you writes again.",
            )
        },
        confirmButton = {
            PrimaryButton(
                text = "Delete",
                compact = true,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                onClick = onConfirm,
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** The item's stage behind a conversation, in the words the filter chips use. */
private fun conversationStage(conversation: Conversation): String = when (conversation.itemStatus.lowercase()) {
    "pending", "private" -> "negotiating"
    "public" -> "available"
    "reserved" -> "reserved"
    "sold" -> "sold"
    "rejected" -> "rejected"
    else -> ""
}

/** "Pinned" / "Recent" above a shelf of the conversation list. */
@Composable
private fun ListShelfLabel(text: String) {
    Overline(
        text,
        modifier = Modifier.padding(start = Spacing.screen, end = Spacing.screen, top = Spacing.md, bottom = Spacing.xs),
    )
}

/** Something that happened to the thread itself: a rename, and when. */
internal data class ThreadEvent(val name: String, val at: Long)

/** A thread as the API hands it over: its messages, and its own lines. */
internal data class ChatThread(
    val messages: List<ChatMessage>,
    val events: List<ThreadEvent> = emptyList(),
)

/** One row of the thread: a message, or a line about the thread itself. */
internal sealed class ThreadRow {
    abstract val key: String

    data class Line(val msg: ChatMessage) : ThreadRow() {
        override val key: String get() = "m${msg.messageId}"
    }

    data class System(val event: ThreadEvent, val index: Int) : ThreadRow() {
        override val key: String get() = "e$index"
    }
}

/** Messages in order, with each event slotted after the last line older than it. */
internal fun buildThreadRows(messages: List<ChatMessage>, events: List<ThreadEvent>): List<ThreadRow> {
    if (events.isEmpty()) return messages.map { ThreadRow.Line(it) }

    val times = messages.map { Dates.parse(it.sentAt)?.toEpochMilli() ?: Long.MAX_VALUE }
    val placed = events.sortedBy { it.at }.mapIndexed { index, event ->
        val position = times.count { it <= event.at }
        position to ThreadRow.System(event, index)
    }

    return buildList {
        for (position in 0..messages.size) {
            placed.filter { it.first == position }.forEach { add(it.second) }
            if (position < messages.size) add(ThreadRow.Line(messages[position]))
        }
    }
}

/** The centred line for a rename, the way Messenger notes a changed name. */
@Composable
private fun ThreadSystemLine(event: ThreadEvent) {
    val text = if (event.name.isBlank()) "You removed the conversation name"
    else "You renamed the conversation to “${event.name}”"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.DriveFileRenameOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(Spacing.xs))
        Text(
            text + " · " + timeAgo(java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(event.at))),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Users ──────────────────────────────────────────────────────────────────────

@Composable
private fun AdminUsersContent(onMenuClick: () -> Unit) {
    val context = LocalContext.current
    val token = remember {
        context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", null)
    }

    var students        by remember { mutableStateOf<List<Student>>(emptyList()) }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }
    var selectedFilter  by remember { mutableStateOf("All") }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var refreshKey      by remember { mutableStateOf(0) }
    var searchQuery     by remember { mutableStateOf("") }

    // Always fetch ALL students — filter client-side via displayStatus so that
    // students with is_verified=true but status="pending" on the backend still
    // appear correctly under the Approved tab.
    LaunchedEffect(refreshKey) {
        if (token != null) {
            isLoading = true
            errorMessage = null
            try {
                students = withContext(Dispatchers.IO) { fetchStudents(token, null) }
            } catch (e: Exception) {
                errorMessage = "Failed to load students: ${e.message}"
            } finally {
                isLoading = false
            }
        } else {
            errorMessage = "Not authenticated. Please log in again."
        }
    }

    // Client-side filter using displayStatus (bridges is_verified / status mismatch)
    // Wrapped in remember so it only recomputes when students list or filter changes
    val filteredStudents = remember(students, selectedFilter, searchQuery) {
        val byStatus = when (selectedFilter) {
            "Pending"  -> students.filter { it.displayStatus == "pending" }
            "Approved" -> students.filter { it.displayStatus == "approved" }
            "Declined" -> students.filter { it.displayStatus == "declined" }
            "Blocked"  -> students.filter { it.displayStatus == "blocked" }
            else       -> students
        }
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) byStatus
        else byStatus.filter { it.fullName.lowercase().contains(q) || it.email.lowercase().contains(q) }
    }

    // Pre-compute counts once per students change — avoids 4x .count() on every recomposition
    val pendingCount  = remember(students) { students.count { it.displayStatus == "pending" } }
    val approvedCount = remember(students) { students.count { it.displayStatus == "approved" } }
    val declinedCount = remember(students) { students.count { it.displayStatus == "declined" } }
    val blockedCount  = remember(students) { students.count { it.displayStatus == "blocked" } }

    // Student detail modal
    selectedStudent?.let { student ->
        StudentDetailDialog(
            student         = student,
            token           = token,
            onDismiss       = { selectedStudent = null },
            onStatusUpdated = {
                selectedStudent = null
                refreshKey++
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AdminPageHeader(
            title = "Students",
            subtitle = when {
                pendingCount == 1 -> "1 account waiting for approval"
                pendingCount > 1 -> "$pendingCount accounts waiting for approval"
                else -> "${students.size} registered"
            },
            onMenuClick = onMenuClick,
        )

        SearchField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Search by name or email…",
            elevated = false,
            modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.md),
        )

        // ── Filter chips ─────────────────────────────────────────────────────
        val filters = listOf("All", "Pending", "Approved", "Declined", "Blocked")
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.screen),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.padding(bottom = Spacing.sm),
        ) {
            items(filters, key = { it }) { filter ->
                val count = when (filter) {
                    "Pending"  -> pendingCount
                    "Approved" -> approvedCount
                    "Declined" -> declinedCount
                    "Blocked"  -> blockedCount
                    else       -> students.size
                }
                ChoiceChip(
                    label = filter,
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    count = if (filter == "All") null else count,
                )
            }
        }

        // ── Content ──────────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> Column(modifier = Modifier.fillMaxSize()) {
                    repeat(7) { ListRowSkeleton() }
                }

                errorMessage != null -> ErrorState(
                    title = "Could not load students",
                    message = errorMessage!!,
                    onRetry = { refreshKey++ },
                )

                filteredStudents.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.PeopleOutline,
                        title = when {
                            searchQuery.isNotBlank() -> "No matches"
                            selectedFilter == "All" -> "No students yet"
                            else -> "No ${selectedFilter.lowercase()} students"
                        },
                        message = when {
                            searchQuery.isNotBlank() -> "Nothing matches \"$searchQuery\"."
                            selectedFilter == "Pending" -> "Every account has been reviewed."
                            else -> "Students appear here once they register."
                        },
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = Spacing.screen, vertical = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    items(filteredStudents, key = { it.studentVerificationId }) { student ->
                        StudentCard(student = student, onClick = { selectedStudent = student })
                    }
                    item { Spacer(Modifier.height(Spacing.sm)) }
                }
            }
        }
    }
}

// ── Student Card ───────────────────────────────────────────────────────────────

/** The badge tone for a verification status. */
private fun studentStatusTone(status: String): StatusTone = when (status.lowercase()) {
    "approved" -> StatusTone.Success
    "declined" -> StatusTone.Danger
    "blocked"  -> StatusTone.Neutral
    else       -> StatusTone.Warning
}

@Composable
private fun StudentCard(student: Student, onClick: () -> Unit) {
    MarketCard(onClick = onClick, contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(url = student.profilePicture ?: "", initial = student.initial, size = 48.dp)

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    student.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    student.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val detail = buildString {
                    student.verificationType?.let { append(it.replace("_", " ").replaceFirstChar { c -> c.uppercaseChar() }) }
                    if (student.isVerified) { if (isNotEmpty()) append(" · "); append("Verified") }
                }
                if (detail.isNotBlank()) {
                    Text(
                        detail,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (student.isVerified) LocalMarketAccents.current.success
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.sm))

            // Status badge — uses displayStatus so is_verified:true always shows Approved
            StatusPill(
                label = student.displayStatus.replaceFirstChar { it.uppercaseChar() },
                tone = studentStatusTone(student.displayStatus),
            )
        }
    }
}

// ── Student Detail Page (full-screen) ─────────────────────────────────────────

@Composable
private fun StudentDetailDialog(
    student: Student,
    token: String?,
    onDismiss: () -> Unit,
    onStatusUpdated: () -> Unit
) {
    val scope  = rememberCoroutineScope()

    var declineReason    by remember { mutableStateOf(student.reason ?: "") }
    var showDeclineInput by remember { mutableStateOf(false) }
    var blockReason      by remember { mutableStateOf("") }
    var showBlockInput   by remember { mutableStateOf(false) }
    var actionLoading    by remember { mutableStateOf(false) }
    var resultDialog     by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var showDocViewer    by remember { mutableStateOf(false) }

    // ── Full-screen document viewer ───────────────────────────────────────────
    if (showDocViewer && student.verificationDocument != null) {
        var scale       by remember { mutableStateOf(1f) }
        var offsetX     by remember { mutableStateOf(0f) }
        var offsetY     by remember { mutableStateOf(0f) }
        val transformState = rememberTransformableState { zoomChange, panChange, _ ->
            scale   = (scale * zoomChange).coerceIn(1f, 5f)
            offsetX += panChange.x
            offsetY += panChange.y
        }
        Dialog(
            onDismissRequest = { showDocViewer = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false, dismissOnBackPress = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .transformable(state = transformState)
            ) {
                SubcomposeAsyncImage(
                    model = student.verificationDocument,
                    contentDescription = "Verification Document",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX       = scale,
                            scaleY       = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    contentScale = ContentScale.Fit,
                    loading = { ShimmerEffect(Modifier.fillMaxSize()) },
                    error = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Could not load image", color = Color.White)
                        }
                    }
                )
                IconButton(
                    onClick = { showDocViewer = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close",
                        tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Text(
                    "Pinch to zoom",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
                )
            }
        }
    }

    // ── Success / Error alert dialog ─────────────────────────────────────────
    resultDialog?.let { (success, message) ->
        AlertDialog(
            onDismissRequest = {
                resultDialog = null
                if (success) onStatusUpdated()
            },
            icon = {
                Icon(
                    if (success) Icons.Filled.CheckCircle else Icons.Filled.Error,
                    contentDescription = null,
                    tint = if (success) LocalMarketAccents.current.success else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text(if (success) "Success" else "Failed", fontWeight = FontWeight.Bold) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    resultDialog = null
                    if (success) onStatusUpdated()
                }) { Text("OK", fontWeight = FontWeight.SemiBold) }
            }
        )
    }

    // ── Full-screen page (Dialog that fills the whole screen) ─────────────────
    Dialog(
        onDismissRequest = { if (!actionLoading) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false, decorFitsSystemWindows = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        val accents = LocalMarketAccents.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Header, with the identity block on the gradient ──────────
                MarketHeader(
                    title = "Student account",
                    onBack = { if (!actionLoading) onDismiss() },
                    actions = {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = Spacing.md),
                        ) {
                            Text(
                                student.displayStatus.replaceFirstChar { it.uppercaseChar() },
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
                            )
                        }
                    },
                    bottomContent = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = Spacing.xl, end = Spacing.xl, top = Spacing.xs, bottom = Spacing.xl),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Avatar(
                                url = student.profilePicture ?: "",
                                initial = student.initial,
                                size = 64.dp,
                                ringColor = Color.White.copy(alpha = 0.5f),
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White,
                            )
                            Spacer(modifier = Modifier.width(Spacing.lg))
                            Column {
                                Text(
                                    student.fullName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = accents.onBrand,
                                )
                                Text(
                                    student.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = accents.onBrandMuted,
                                )
                            }
                        }
                    },
                )

                // ── Scrollable Content ────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeAreaBottom()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.screen, vertical = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xl),
                ) {
                    // ── Pending nudge ─────────────────────────────────────────
                    if (student.displayStatus == "pending") {
                        InfoBanner(
                            title = "Waiting for your decision",
                            text = "Check the ID below is a real Fatima student card or registration form, then approve or decline.",
                            tone = StatusTone.Warning,
                            icon = Icons.Filled.HourglassTop,
                        )
                    }
                    if (!student.reason.isNullOrBlank()) {
                        InfoBanner(
                            title = "Reason on file",
                            text = student.reason,
                            tone = StatusTone.Danger,
                            icon = Icons.Filled.Info,
                        )
                    }

                    // ── Verification document ─────────────────────────────────
                    student.verificationDocument?.let { docUrl ->
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            SectionHeader(
                                title = "Verification document",
                                subtitle = student.verificationType
                                    ?.replace("_", " ")
                                    ?.replaceFirstChar { c -> c.uppercaseChar() }
                                    ?: "Student ID or registration card",
                                actionLabel = "Zoom",
                                onAction = { showDocViewer = true },
                            )
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { showDocViewer = true },
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shadowElevation = Elevation.card,
                            ) {
                                Box {
                                    SubcomposeAsyncImage(
                                        model = docUrl,
                                        contentDescription = "Verification Document",
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp, max = 300.dp),
                                        contentScale = ContentScale.FillWidth,
                                        loading = {
                                            ShimmerBox(
                                                Modifier.fillMaxWidth().height(200.dp),
                                                shape = MaterialTheme.shapes.medium,
                                            )
                                        },
                                        error = {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().height(140.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Outlined.BrokenImage, null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                        modifier = Modifier.size(36.dp))
                                                    Spacer(Modifier.height(Spacing.xs))
                                                    Text("Could not load document",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    )
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(Spacing.sm),
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.5f),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                                        ) {
                                            Icon(Icons.Filled.ZoomIn, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Text("Tap to zoom", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Info section ──────────────────────────────────────────
                    SettingsGroup(title = "Account details") {
                        InfoRowItem(Icons.Outlined.Badge, "Verification ID", "#${student.studentVerificationId}")
                        RowDivider()
                        InfoRowItem(
                            Icons.Outlined.VerifiedUser, "Verified",
                            if (student.isVerified) "Yes" else "Not yet",
                            valueColor = if (student.isVerified) accents.success else MaterialTheme.colorScheme.onSurface,
                        )
                        RowDivider()
                        InfoRowItem(Icons.Outlined.Stars, "Wallet points", "${student.walletPoints} pts", tint = accents.reward)
                        RowDivider()
                        InfoRowItem(
                            Icons.Outlined.ToggleOn, "Active",
                            if (student.isActive) "Yes" else "No",
                            valueColor = if (student.isActive) accents.success else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        RowDivider()
                        InfoRowItem(Icons.Outlined.CalendarToday, "Registered", formatDate(student.registeredDate))
                    }

                    // ── Decisions ─────────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        SectionHeader(title = "Decision")

                        // ── Decline reason input ──────────────────────────────
                        if (showDeclineInput) {
                            MarketTextField(
                                value = declineReason,
                                onValueChange = { declineReason = it },
                                label = "Reason for declining",
                                placeholder = "Optional - the student will see this",
                                singleLine = false,
                                maxLines = 3,
                            )
                        }

                        // ── Block reason input ────────────────────────────────
                        if (showBlockInput) {
                            MarketTextField(
                                value = blockReason,
                                onValueChange = { blockReason = it },
                                label = "Reason for blocking",
                                placeholder = "Why this student is being blocked",
                                singleLine = false,
                                maxLines = 3,
                            )
                        }

                        if (actionLoading) {
                            Box(modifier = Modifier.fillMaxWidth().padding(Spacing.lg), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            }
                        } else {
                            // Approve — shown when not already approved
                            if (student.displayStatus != "approved") {
                                PrimaryButton(
                                    text = "Approve student",
                                    icon = Icons.Filled.CheckCircle,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        if (token == null) return@PrimaryButton
                                        showDeclineInput = false
                                        showBlockInput = false
                                        scope.launch {
                                            actionLoading = true
                                            val ok = withContext(Dispatchers.IO) {
                                                updateStudentStatus(token, student.userId, "approve")
                                            }
                                            actionLoading = false
                                            resultDialog = if (ok)
                                                Pair(true, "Student has been approved successfully.")
                                            else
                                                Pair(false, "Approval failed. Please try again.")
                                        }
                                    },
                                )
                            }

                            // Decline — shown when not already declined
                            if (student.displayStatus != "declined") {
                                if (!showDeclineInput) {
                                    SecondaryButton(
                                        text = "Decline",
                                        icon = Icons.Outlined.Cancel,
                                        contentColor = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { showDeclineInput = true; showBlockInput = false },
                                    )
                                } else {
                                    PrimaryButton(
                                        text = "Confirm decline",
                                        icon = Icons.Outlined.Cancel,
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            if (token == null) return@PrimaryButton
                                            scope.launch {
                                                actionLoading = true
                                                val ok = withContext(Dispatchers.IO) {
                                                    updateStudentStatus(
                                                        token,
                                                        student.userId,
                                                        "decline",
                                                        declineReason.trim().ifBlank { null }
                                                    )
                                                }
                                                actionLoading = false
                                                resultDialog = if (ok)
                                                    Pair(true, "Student has been declined successfully.")
                                                else
                                                    Pair(false, "Decline failed. Please try again.")
                                            }
                                        },
                                    )
                                }
                            }

                            // Block — shown when not already blocked
                            if (student.displayStatus != "blocked") {
                                if (!showBlockInput) {
                                    SecondaryButton(
                                        text = "Block student",
                                        icon = Icons.Outlined.Block,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { showBlockInput = true; showDeclineInput = false },
                                    )
                                } else {
                                    PrimaryButton(
                                        text = "Confirm block",
                                        icon = Icons.Outlined.Block,
                                        containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            if (token == null) return@PrimaryButton
                                            scope.launch {
                                                actionLoading = true
                                                val ok = withContext(Dispatchers.IO) {
                                                    updateStudentStatus(token, student.userId, "block", blockReason.trim().ifBlank { null })
                                                }
                                                actionLoading = false
                                                resultDialog = if (ok)
                                                    Pair(true, "Student has been blocked successfully.")
                                                else
                                                    Pair(false, "Block failed. Please try again.")
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp).padding(top = 1.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("$label:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium, modifier = Modifier.width(110.dp))
        Text(value, style = MaterialTheme.typography.bodySmall,
            color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor,
            modifier = Modifier.weight(1f))
    }
}

// ── Settings ───────────────────────────────────────────────────────────────────

/**
 * Kept for the drawer pages that still reach it. Day to day, the same rows
 * live on the profile screen, which is where both roles now find them.
 */
@Composable
fun AdminSettingsContent(
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onMenuClick: () -> Unit,
    role: String = "Administrator",
    favoritesCount: Int = 0,
    onFavoritesClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AdminPageHeader(title = "Settings", onMenuClick = onMenuClick, favoritesCount = favoritesCount, onFavoritesClick = onFavoritesClick)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl),
        ) {
            AppearanceGroup(isDarkMode = isDarkMode, onThemeToggle = onThemeToggle)
            StoreLocationGroup()
            AboutGroup(role = role)
        }
    }
}

/** The dark-mode switch, in a titled group. */
@Composable
private fun AppearanceGroup(isDarkMode: Boolean, onThemeToggle: () -> Unit) {
    SettingsGroup(title = "Appearance") {
        SettingsRow(
            icon = if (isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
            title = "Dark mode",
            subtitle = if (isDarkMode) "On" else "Off",
            onClick = onThemeToggle,
            trailing = { Switch(checked = isDarkMode, onCheckedChange = { onThemeToggle() }) },
        )
    }
}

/** App name, version and the institution it serves. */
@Composable
private fun AboutGroup(role: String) {
    val context = LocalContext.current
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0" }
        catch (e: Exception) { "1.0" }
    }

    SettingsGroup(title = "About") {
        InfoRowItem(StoreLogoIcon, "App", "Fati-Market v$versionName")
        RowDivider()
        InfoRowItem(Icons.Outlined.School, "Institution", "Our Lady of Fatima University")
        RowDivider()
        InfoRowItem(Icons.Outlined.Badge, "Signed in as", role.replaceFirstChar { it.uppercaseChar() })
    }
}

// ── Profile ────────────────────────────────────────────────────────────────────

@Composable
fun AdminProfileContent(
    onMenuClick: () -> Unit,
    firstName: String,
    lastName: String,
    email: String,
    role: String,
    walletPoints: Int,
    profilePic: String,
    onProfilePicUpdated: (String) -> Unit,
    favoritesCount: Int = 0,
    onFavoritesClick: () -> Unit = {},
    /**
     * Opens the buyer's purchases. Absent for admin, who has the full
     * transactions screen instead.
     */
    onMyOrders: (() -> Unit)? = null,
    /** Student-only: the listings they are selling, turnover state and all. */
    onMySales: (() -> Unit)? = null,
    /** Admin-only: the full transactions screen. */
    onManageOrders: (() -> Unit)? = null,
    /** Admin-only: student management, which used to sit on the bottom bar. */
    onManageStudents: (() -> Unit)? = null,
    /** Settings, which used to be a tab of their own, live here now. */
    isDarkMode: Boolean = false,
    onThemeToggle: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
) {
    val context     = LocalContext.current
    val prefs       = remember { context.getSharedPreferences("fatimarket_prefs", 0) }
    val scope       = rememberCoroutineScope()
    val accents     = LocalMarketAccents.current
    val isAdmin     = role.equals("admin", true)
    val fullName    = "$firstName $lastName".trim().ifBlank { if (isAdmin) "Administrator" else "Student" }
    val initial     = firstName.firstOrNull()?.uppercaseChar()?.toString() ?: if (isAdmin) "A" else "S"
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showGcashSettings by remember { mutableStateOf(false) }
    var showStoreHoursSettings by remember { mutableStateOf(false) }
    var showStoreHours by remember { mutableStateOf(false) }

    if (isAdmin && showGcashSettings) {
        AdminGcashSettingsDialog(onDismiss = { showGcashSettings = false })
    }
    if (isAdmin && showStoreHoursSettings) {
        AdminStoreHoursSettingsDialog(onDismiss = { showStoreHoursSettings = false })
    }
    if (!isAdmin && showStoreHours) {
        StoreHoursInfoDialog(onDismiss = { showStoreHours = false })
    }

    // The balance passed in is whatever login stored - a number frozen at
    // sign-in that never moved again, so a buyer who earned points saw zero
    // here while the header, which polls, showed the real figure. This reads
    // the wallet the same way the header does, and writes it back so the
    // stored value stops going stale.
    var livePoints by remember { mutableStateOf(walletPoints) }

    LaunchedEffect(Unit) {
        val token = prefs.getString("auth_token", "") ?: ""

        if (token.isBlank()) return@LaunchedEffect

        while (true) {
            val result = withContext(Dispatchers.IO) { MarketplaceApi.fetchWalletPoints(token) }

            if (result is MarketplaceApi.Result.Ok) {
                livePoints = result.value
                prefs.edit().putInt("user_wallet_points", result.value).apply()
            }

            delay(15_000)
        }
    }

    // Auto-clear error after 3 s
    LaunchedEffect(uploadError) {
        if (uploadError != null) {
            delay(3000)
            uploadError = null
        }
    }

    // Image picker — uploads selected image to the profile picture API
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                isUploading = true
                uploadError = null
                try {
                    val mimeType = context.contentResolver.getType(selectedUri) ?: "image/jpeg"
                    val ext = when {
                        mimeType.contains("png")  -> "png"
                        mimeType.contains("webp") -> "webp"
                        mimeType.contains("gif")  -> "gif"
                        else                      -> "jpg"
                    }
                    val file = withContext(Dispatchers.IO) {
                        val input = context.contentResolver.openInputStream(selectedUri)
                            ?: return@withContext null
                        val f = File(context.filesDir, "user_profile_pic.$ext")
                        input.use { src -> f.outputStream().use { dst -> src.copyTo(dst) } }
                        f
                    }
                    if (file == null) {
                        uploadError = "Could not read the selected image."
                        isUploading = false
                        return@launch
                    }
                    val token  = prefs.getString("auth_token", "") ?: ""
                    val newUrl = withContext(Dispatchers.IO) { uploadProfilePicture(token, file, mimeType) }
                    if (newUrl != null) {
                        if (newUrl.isNotEmpty()) {
                            prefs.edit().putString("user_profile_picture", newUrl).apply()
                            onProfilePicUpdated(newUrl)
                        }
                        // newUrl == "" means upload succeeded but server didn't return a new URL — treat as success
                    } else {
                        uploadError = "Upload failed. Please try again."
                    }
                } catch (e: Exception) {
                    uploadError = e.message ?: "Upload failed."
                } finally {
                    isUploading = false
                }
            }
        }
    }

    if (showLogoutDialog && onLogout != null) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp)) },
            title = { Text("Log out?") },
            text  = { Text("You will need to sign in again next time.") },
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

    // One scroll for the whole page: the header and its identity block scroll
    // away with the rest instead of staying pinned over a shrinking list.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // The header carries the identity block, so the avatar sits on the
        // brand gradient rather than floating in the page.
        MarketHeader(
            title = "Profile",
            onMenuClick = onMenuClick,
            bottomContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm, bottom = Spacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        if (isUploading) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = accents.onBrand,
                                    strokeWidth = 3.dp
                                )
                            }
                        } else {
                            Avatar(
                                url = profilePic,
                                initial = initial,
                                size = 96.dp,
                                ringColor = Color.White.copy(alpha = 0.7f),
                                ringWidth = 3.dp,
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White,
                            )
                        }
                        // Camera badge
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable(enabled = !isUploading) { imagePicker.launch("image/*") },
                            shape = CircleShape,
                            color = accents.reward,
                            shadowElevation = Elevation.raised,
                        ) {
                            Icon(
                                Icons.Filled.CameraAlt, "Change photo",
                                tint = Color(0xFF3D2900),
                                modifier = Modifier.padding(7.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = accents.onBrand,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        if (isAdmin) "Store administrator" else email.ifBlank { "Student" },
                        style = MaterialTheme.typography.bodySmall,
                        color = accents.onBrandMuted,
                        textAlign = TextAlign.Center,
                    )
                    if (uploadError != null) {
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = uploadError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFB4AB),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = Spacing.xl)
                        )
                    }
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screen)
                .padding(top = Spacing.lg, bottom = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            // ── At a glance ───────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                StatTile(
                    label = "Points balance",
                    value = "$livePoints",
                    icon = Icons.Filled.Stars,
                    tint = accents.reward,
                    modifier = Modifier.weight(1f),
                    // Points mean nothing to a buyer until they are shown in pesos.
                    supporting = if (isAdmin) null else Money.format(
                        LoyaltyRules.discountFor(livePoints).toPlainString()
                    ) + " off at checkout",
                )
                if (isAdmin) {
                    StatTile(
                        label = "Students",
                        value = "Manage",
                        icon = Icons.Filled.Group,
                        modifier = Modifier.weight(1f),
                        onClick = onManageStudents,
                        supporting = "Approve, decline, block",
                    )
                } else {
                    StatTile(
                        label = "Favourites",
                        value = "$favoritesCount",
                        icon = Icons.Filled.Favorite,
                        tint = FavoriteRed,
                        modifier = Modifier.weight(1f),
                        onClick = onFavoritesClick,
                        supporting = "Saved for later",
                    )
                }
            }

            // ── Activity ──────────────────────────────────────────────────
            // Only buyers get this group; admin has the full transactions
            // screen and does not shop.
            onMyOrders?.let { openOrders ->
                SettingsGroup(title = "My activity") {
                    SettingsRow(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "My purchases",
                        subtitle = "Track payments and download receipts",
                        onClick = openOrders
                    )
                    onMySales?.let { openSales ->
                        RowDivider()
                        SettingsRow(
                            icon = Icons.Outlined.Sell,
                            title = "My listings",
                            subtitle = "Items you offered, and where each one stands",
                            onClick = openSales
                        )
                    }
                }
            }

            if (!isAdmin) {
                SettingsGroup(title = "Store information") {
                    SettingsRow(
                        icon = Icons.Outlined.Schedule,
                        title = "Store hours",
                        subtitle = "See opening times, open days and booking slots",
                        onClick = { showStoreHours = true },
                    )
                }
            }

            // ── Store management ─────────────────────────────────────────
            // The admin's day-to-day: every order in one screen, and the
            // student roster that used to occupy the bottom bar.
            if (onManageOrders != null || onManageStudents != null) {
                SettingsGroup(title = "Store") {
                    if (isAdmin) {
                        SettingsRow(
                            icon = Icons.Outlined.Schedule,
                            title = "Store hours & booking slots",
                            subtitle = "Opening times, open days and slot length",
                            onClick = { showStoreHoursSettings = true },
                        )
                        RowDivider()
                        SettingsRow(
                            icon = Icons.Outlined.Payment,
                            title = "GCash payment settings",
                            subtitle = "Account name, mobile number and payment QR",
                            onClick = { showGcashSettings = true },
                        )
                        RowDivider()
                    }
                    onManageOrders?.let { openOrders ->
                        SettingsRow(
                            icon = Icons.Outlined.ReceiptLong,
                            title = "Transactions",
                            subtitle = "Every order - pending, reserved, unpaid, completed",
                            onClick = openOrders
                        )
                    }

                    if (onManageOrders != null && onManageStudents != null) RowDivider()

                    onManageStudents?.let { openStudents ->
                        SettingsRow(
                            icon = Icons.Outlined.Group,
                            title = "Students",
                            subtitle = "Approve, decline or block student accounts",
                            onClick = openStudents
                        )
                    }
                }
            }

            // ── Store location ───────────────────────────────────────────

            // Students only need the location details and map actions here;
            // the embedded preview stays available to administrators.

            StoreLocationGroup(

                subtitle = if (isAdmin) "What students see for meet-ups and pickups"

                           else "Meet-ups and walk-in pickups happen here",

                showMap = isAdmin,

            )


            // ── Account ───────────────────────────────────────────────────
            SettingsGroup(title = "Account") {
                InfoRowItem(Icons.Outlined.Person, "Full name", fullName)
                RowDivider()
                InfoRowItem(Icons.Outlined.Email, "School email", email.ifBlank { "—" })

                // The password this account signs in with. The app could set
                // one for the recovery address and nothing else, so an admin
                // had no way to change theirs short of the forgotten-password
                // email.
                RowDivider()

                var changingPassword by remember { mutableStateOf(false) }
                var passwordChanged by remember { mutableStateOf(false) }

                if (changingPassword) {
                    ChangePasswordDialog(
                        onDismiss = { changingPassword = false },
                        onSaved = { passwordChanged = true },
                    )
                }

                SettingsRow(
                    icon = Icons.Outlined.Lock,
                    title = "Change password",
                    subtitle = if (passwordChanged) {
                        "Changed just now - your other devices were signed out"
                    } else {
                        "Your other devices are signed out when it changes"
                    },
                    onClick = { changingPassword = true },
                )

                // The address that outlives the school account. Only students
                // need it - an admin account is not lent out by a school.
                if (!isAdmin) {
                    RowDivider()

                    var linked by remember {
                        mutableStateOf(prefs.getString("personal_email", "").orEmpty())
                    }
                    val pending = prefs.getString("personal_email_pending", "").orEmpty()
                    var editing by remember { mutableStateOf(false) }
                    var settingPassword by remember { mutableStateOf(false) }
                    var passwordSet by remember { mutableStateOf(prefs.getBoolean("personal_email_password_set", false)) }
                    var emailVerified by remember { mutableStateOf(linked.isNotBlank()) }
                    LaunchedEffect(Unit) {
                        val result = withContext(Dispatchers.IO) { personalEmailStatus(prefs.getString("auth_token", "").orEmpty()) }
                        val status = runCatching { org.json.JSONObject(result.body.orEmpty()) }.getOrNull()
                        val payload = status?.optJSONObject("data") ?: status
                        linked = payload?.optString("personal_email").orEmpty().ifBlank { linked }
                        emailVerified = payload?.optBoolean("personal_email_verified", false) == true
                        passwordSet = payload?.optBoolean("password_set", false) == true
                        prefs.edit().putBoolean("personal_email_password_set", passwordSet).apply()
                    }

                    if (editing) {
                        PersonalEmailDialog(
                            onDismiss = { editing = false },
                            onLinked = { linked = it; editing = false },
                            existing = linked.takeIf { it.isNotBlank() },
                        )
                    }
                    if (settingPassword) {
                        PersonalEmailPasswordDialog(
                            onDismiss = { settingPassword = false },
                            onChangeEmail = { settingPassword = false; editing = true },
                            passwordAlreadySet = passwordSet,
                            onSaved = { passwordSet = true },
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SettingsRow(
                        icon = Icons.Outlined.AlternateEmail,
                        title = "Personal email",
                        subtitle = linked.ifBlank {
                            pending.takeIf { it.isNotBlank() }?.let { "Verify $it and set a password" } ?:
                            "Not set - add one so you keep this account after graduation"
                        },
                        tint = if (linked.isBlank()) accents.warning else MaterialTheme.colorScheme.primary,
                        onClick = { if (linked.isBlank() || !emailVerified) editing = true else settingPassword = true },
                        trailing = if (emailVerified) ({
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Verified, "Verified", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text("Verified", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }) else null,
                    )
                    if (passwordSet) {
                        Row(
                            Modifier.padding(start = Spacing.lg + 38.dp + Spacing.md, bottom = Spacing.md),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text("Password set", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    }
                }
            }

            // ── Preferences ───────────────────────────────────────────────
            onThemeToggle?.let { toggle ->
                AppearanceGroup(isDarkMode = isDarkMode, onThemeToggle = toggle)
            }

            AboutGroup(role = role)

            onLogout?.let {
                SecondaryButton(
                    text = "Log out",
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    onClick = { showLogoutDialog = true },
                    contentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(Spacing.sm))
        }
    }
}

// ── Shared Page Header ─────────────────────────────────────────────────────────

/**
 * The header for the admin's pages, and for any student page that still
 * calls it. Now a thin wrapper over [MarketHeader]: the wallet chip and its
 * five-second polling are gone - points live on the home hero and the
 * profile - and favourites only show where a student can use them.
 */
@Composable
fun AdminPageHeader(
    title: String,
    onMenuClick: () -> Unit,
    favoritesCount: Int = 0,
    onFavoritesClick: () -> Unit = {},
    subtitle: String? = null,
    showFavorites: Boolean = false,
) {
    // The bell was dead until now: a push banner is gone the moment
    // it is dismissed, so anything that arrived while the app was
    // closed had nowhere to be read.
    var showNotifications by remember { mutableStateOf(false) }

    if (showNotifications) {
        NotificationsDialog(onDismiss = { showNotifications = false })
    }

    MarketHeader(
        title = title,
        subtitle = subtitle,
        onMenuClick = onMenuClick,
        actions = {
            if (showFavorites) {
                HeaderAction(
                    icon = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favourites",
                    onClick = onFavoritesClick,
                    badge = favoritesCount,
                )
            }
            HeaderAction(
                icon = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                onClick = { showNotifications = true },
            )
        },
    )
}

// ── Chat item network ──────────────────────────────────────────────────────────

private fun fetchChatItem(token: String, itemId: Int): ChatItem? {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/items/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string() ?: return null
            val json = JSONObject(raw)
            val parsed = parseItem(json.optJSONObject("data") ?: json)
            ChatItem(
                itemId             = parsed.itemId,
                title              = parsed.title,
                description        = parsed.description,
                askingPrice        = parsed.sellerAskingPrice,
                acquisitionPrice   = parsed.acquisitionPrice,
                publicPrice        = parsed.publicPrice,
                rewardPoints       = parsed.rewardPoints,
                sellerPayoutStatus = parsed.sellerPayoutStatus,
                isTurnoverVerified = parsed.isTurnoverVerified,
                sellerId           = parsed.sellerId,
                sellerEmail        = parsed.sellerEmail,
                status             = parsed.status,
                photos             = parsed.photos
            )
        }
    } catch (_: Exception) { null }
}

fun performLogout(token: String): Boolean {
    // Remove only this device's association. Keep the Firebase token stable
    // so a delayed deletion cannot invalidate the next login's registration.
    runCatching {
        val messaging = com.google.firebase.messaging.FirebaseMessaging.getInstance()
        val fcmToken = com.google.android.gms.tasks.Tasks.await(
            messaging.token, 10, java.util.concurrent.TimeUnit.SECONDS,
        )

        val unregister = Request.Builder()
            .url("https://fati-api.alertaraqc.com/api/device-tokens")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .method(
                "DELETE",
                JSONObject().put("token", fcmToken).toString()
                    .toRequestBody("application/json".toMediaType()),
            )
            .build()

        adminHttpClient.newCall(unregister).execute().close()
    }

    return try {
        val request = Request.Builder()
            .url("https://fati-api.alertaraqc.com/api/logout")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .post("{}".toRequestBody("application/json".toMediaType()))
            .build()

        adminHttpClient.newCall(request).execute().use { response ->
            response.isSuccessful
        }
    } catch (_: Exception) { false }
}

// Get all information of items base on status (admin endpoint — returns all users' items)
private fun fetchItems(token: String, status: String): List<Item> {
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/admin/items?status=$status")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .get()
        .build()
    adminHttpClient.newCall(request).execute().use { response ->
        val raw = response.body?.string() ?: return emptyList()
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}: $raw")
        return try {
            val json = JSONObject(raw)
            // handle both {"data":[...]} and a bare array
            val arr = when {
                json.has("data") -> json.getJSONArray("data")
                else             -> org.json.JSONArray(raw)
            }
            (0 until arr.length()).map { i -> parseItem(arr.getJSONObject(i)) }
        } catch (e: Exception) { throw Exception("Parse error: ${e.message}") }
    }
}

private fun updateItemStatus(token: String, itemId: Int, status: String): Pair<Boolean, String> {
    val body = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("_method", "PUT")
        .addFormDataPart("status", status)
        .build()
    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/items/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(body)
        .build()
    return try {
        adminHttpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Pair(true, "")
            } else {
                val msg = try {
                    val json = JSONObject(raw)
                    json.optString("message", "HTTP ${response.code}")
                } catch (_: Exception) {
                    "HTTP ${response.code}"
                }
                Pair(false, msg)
            }
        }
    } catch (e: Exception) {
        Pair(false, e.message ?: "Network error")
    }
}

/**
 * Update an item's status and/or its public selling price.
 *
 * Publishing is gated server-side on verified turnover and a recorded
 * acquisition price, so a refusal here surfaces the server's own explanation
 * rather than being second-guessed locally.
 */
internal fun updateAdminItem(
    token: String,
    itemId: Int,
    status: String? = null,
    publicPrice: String? = null,
    title: String? = null,
    description: String? = null,
    categoryId: Int? = null,
): Pair<Boolean, String> {
    val body = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("_method", "PUT")

    if (status != null) {
        body.addFormDataPart("status", status)
    }
    title?.let { body.addFormDataPart("title", it) }
    description?.let { body.addFormDataPart("description", it) }
    categoryId?.takeIf { it > 0 }?.let { body.addFormDataPart("category_id", it.toString()) }
    if (publicPrice != null) {
        body.addFormDataPart("public_price", publicPrice)
    }

    val request = Request.Builder()
        .url("https://fati-api.alertaraqc.com/api/admin/items/$itemId")
        .header("Authorization", "Bearer $token")
        .header("Accept", "application/json")
        .post(body.build())
        .build()

    return try {
        adminHttpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Pair(true, "")
            } else {
                val msg = try {
                    val json = JSONObject(raw)
                    json.optString("message", "HTTP ${response.code}")
                } catch (_: Exception) {
                    "HTTP ${response.code}"
                }
                Pair(false, msg)
            }
        }
    } catch (e: Exception) {
        Pair(false, e.message ?: "Network error")
    }
}


@Composable
private fun PointsTransactionContent(
    title: String,
    endpoint: String,
    onMenuClick: () -> Unit,
    onGoToChat: () -> Unit = {},
    onNavigateToPage: (DrawerPage) -> Unit = {},
    onShowBottomBarChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var transactions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isInitialLoad by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        requestNotificationPermissionAndRegister(context)
        while (true) {
            try {
                val sharedPref = context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE)
                val token = sharedPref.getString("auth_token", "") ?: ""

                if (token.isBlank()) {
                    errorMessage = "Authentication failed: No token found"
                    if (isInitialLoad) {
                        isLoading = false
                        isInitialLoad = false
                    }
                    delay(5000)
                    continue
                }


                val responseData = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("https://fati-api.alertaraqc.com$endpoint")
                        .header("Authorization", "Bearer $token")
                        .header("Accept", "application/json")
                        .get()
                        .build()

                    adminHttpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            response.body?.string() ?: "{\"data\": []}"
                        } else {
                            val errorBody = response.body?.string() ?: ""
                            null
                        }
                    }
                }

                if (responseData != null) {
                    try {
                        val json = JSONObject(responseData)
                        val dataArray = json.optJSONArray("data") ?: JSONArray()
                        val data = mutableListOf<Map<String, Any>>()
                        for (i in 0 until dataArray.length()) {
                            val obj = dataArray.getJSONObject(i)
                            val userObj = obj.optJSONObject("user") ?: JSONObject()
                            val relatedItemObj = obj.optJSONObject("relatedItem") ?: JSONObject()
                            data.add(mapOf(
                                "point_id" to obj.optInt("point_id", 0),
                                "user_email" to userObj.optString("email", ""),
                                "points_change" to obj.optInt("points_change", 0),
                                "reason" to obj.optString("reason", ""),
                                "related_item_title" to relatedItemObj.optString("title", ""),
                                "created_at" to obj.optString("created_at", "")
                            ))
                        }
                        transactions = data
                        errorMessage = ""
                    } catch (parseError: Exception) {
                        errorMessage = "Failed to parse response: ${parseError.message}"
                    }
                } else {
                    errorMessage = "Failed to fetch data"
                }

                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.javaClass.simpleName} - ${e.message ?: "Unknown error loading transactions"}"
                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
            }
            delay(5000)
        }
    }

    val showBar = remember { true }
    LaunchedEffect(showBar) {
        onShowBottomBarChange(showBar)
    }

    CompositionLocalProvider(
        LocalProvidesBottomBar provides showBar
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AdminPageHeader(title = title, onMenuClick = onMenuClick)

            when {
                isLoading -> LoadingState()
                errorMessage.isNotEmpty() -> ErrorState(
                    message = errorMessage,
                    onRetry = { isInitialLoad = true; isLoading = true },
                )
                transactions.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Inventory2,
                    title = "No transactions found",
                    message = "There is nothing to show here yet.",
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(transactions.size) { index ->
                        val transaction = transactions[index]
                        val pointsChange = (transaction["points_change"] as? Int ?: 0)
                        val email = transaction["user_email"].toString()
                        val reason = transaction["reason"].toString()
                        val itemTitle = transaction["related_item_title"].toString()
                        val createdAt = transaction["created_at"].toString()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(email, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${if (pointsChange > 0) "+" else ""}${pointsChange} pts • $reason",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (pointsChange > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                if (itemTitle.isNotEmpty()) {
                                    Text(itemTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TransactionsContent(
    title: String,
    endpoint: String,
    onMenuClick: () -> Unit,
    onGoToChat: () -> Unit = {},
    onNavigateToPage: (DrawerPage) -> Unit = {},
    onShowBottomBarChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var transactions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isInitialLoad by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val sharedPref = context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE)
                val token = sharedPref.getString("auth_token", "") ?: ""

                if (token.isBlank()) {
                    errorMessage = "Authentication failed: No token found"
                    if (isInitialLoad) {
                        isLoading = false
                        isInitialLoad = false
                    }
                    delay(5000)
                    continue
                }


                val responseData = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("https://fati-api.alertaraqc.com$endpoint")
                        .header("Authorization", "Bearer $token")
                        .header("Accept", "application/json")
                        .get()
                        .build()

                    adminHttpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            response.body?.string() ?: "{\"data\": []}"
                        } else {
                            val errorBody = response.body?.string() ?: ""
                            null
                        }
                    }
                }

                if (responseData != null) {
                    try {
                        val json = JSONObject(responseData)
                        val dataArray = json.optJSONArray("data") ?: JSONArray()
                        val data = mutableListOf<Map<String, Any>>()
                        for (i in 0 until dataArray.length()) {
                            val obj = dataArray.getJSONObject(i)
                            val itemObj = obj.optJSONObject("item") ?: JSONObject()
                            val buyerObj = obj.optJSONObject("buyer") ?: JSONObject()
                            val sellerObj = obj.optJSONObject("seller") ?: JSONObject()
                            data.add(mapOf(
                                "transaction_id" to obj.optInt("transaction_id", 0),
                                "item_title" to itemObj.optString("title", ""),
                                "buyer_email" to buyerObj.optString("email", ""),
                                // The store sells what it owns, so the seller is
                                // the store - "Ofelia Store", as the API names
                                // it. The student the item came from is
                                // provenance and is carried separately; showing
                                // them here made a sale read as the buyer
                                // selling to themselves.
                                // optString hands back the literal "null" for a
                                // JSON null, so both are filtered the way the
                                // model parser does it.
                                "seller_name" to sellerObj.optString("name", "")
                                    .takeIf { it != "null" }.orEmpty(),
                                "consigned_by" to obj.optString("consigned_by", "")
                                    .takeIf { it != "null" }.orEmpty(),
                                "payment_method" to obj.optString("payment_method", ""),
                                "status" to obj.optString("status", ""),
                                "points_used" to obj.optInt("points_used", 0)
                            ))
                        }
                        transactions = data
                        errorMessage = ""
                    } catch (parseError: Exception) {
                        errorMessage = "Failed to parse response: ${parseError.message}"
                    }
                } else {
                    errorMessage = "Failed to fetch data"
                }

                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.javaClass.simpleName} - ${e.message ?: "Unknown error loading transactions"}"
                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
            }
            delay(5000)
        }
    }

    val showBar = remember { true }
    LaunchedEffect(showBar) {
        onShowBottomBarChange(showBar)
    }

    CompositionLocalProvider(
        LocalProvidesBottomBar provides showBar
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AdminPageHeader(title = title, onMenuClick = onMenuClick)

            when {
                isLoading -> LoadingState()
                errorMessage.isNotEmpty() -> ErrorState(
                    message = errorMessage,
                    onRetry = { isInitialLoad = true; isLoading = true },
                )
                transactions.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Inventory2,
                    title = "No transactions found",
                    message = "There is nothing to show here yet.",
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(transactions.size) { index ->
                        val transaction = transactions[index]
                        val itemTitle = transaction["item_title"].toString()
                        val buyerEmail = transaction["buyer_email"].toString()
                        val sellerName = transaction["seller_name"].toString()
                            .ifBlank { "Ofelia Store" }
                        val consignedBy = transaction["consigned_by"].toString()
                        val paymentMethod = transaction["payment_method"].toString()
                        val status = transaction["status"].toString()
                        val pointsUsed = (transaction["points_used"] as? Int ?: 0)

                        // Three methods exist now: cash, GCash, and an order the
                        // points covered outright. Amber is points, as everywhere.
                        val methodLabel = when (paymentMethod.lowercase()) {
                            "gcash" -> "GCash"
                            "points_full" -> "Paid fully with points"
                            "cash" -> "Cash at store"
                            else -> paymentMethod
                        }

                        val methodColor = when (paymentMethod.lowercase()) {
                            "gcash" -> MaterialTheme.colorScheme.primary
                            "points_full" -> LocalMarketAccents.current.reward
                            else -> MaterialTheme.colorScheme.outline
                        }

                        val statusColor = when (status.lowercase()) {
                            "completed" -> MaterialTheme.colorScheme.primary
                            "pending" -> LocalMarketAccents.current.warning
                            "failed" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outline
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(itemTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(8.dp))
                                Text("Buyer: $buyerEmail", style = MaterialTheme.typography.bodySmall)
                                Text("Seller: $sellerName", style = MaterialTheme.typography.bodySmall)

                                if (consignedBy.isNotBlank()) {
                                    Text(
                                        "Consigned by: $consignedBy",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = methodColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            methodLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = methodColor,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                    Surface(
                                        color = statusColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            status,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = statusColor,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                }
                                if (pointsUsed != 0) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "$pointsUsed pts used",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ProfitSummaryContent(
    onMenuClick: () -> Unit,
    onGoToChat: () -> Unit = {},
    onNavigateToPage: (DrawerPage) -> Unit = {},
    onShowBottomBarChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var profitData by remember { mutableStateOf(mapOf<String, Any>()) }
    var isLoading by remember { mutableStateOf(true) }
    var isInitialLoad by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val sharedPref = context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE)
                val token = sharedPref.getString("auth_token", "") ?: ""

                if (token.isBlank()) {
                    errorMessage = "Authentication failed: No token found"
                    if (isInitialLoad) {
                        isLoading = false
                        isInitialLoad = false
                    }
                    delay(5000)
                    continue
                }


                val responseData = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("https://fati-api.alertaraqc.com/api/admin/transactions/profit-summary")
                        .header("Authorization", "Bearer $token")
                        .header("Accept", "application/json")
                        .get()
                        .build()

                    adminHttpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            response.body?.string() ?: "{\"data\": {}}"
                        } else {
                            val errorBody = response.body?.string() ?: ""
                            null
                        }
                    }
                }

                if (responseData != null) {
                    try {
                        val json = JSONObject(responseData)
                        val dataObj = json.optJSONObject("data") ?: JSONObject()
                        profitData = mapOf(
                            "total_profit_points" to dataObj.optInt("total_profit_points", 0),
                            "monthly_profit_points" to dataObj.optInt("monthly_profit_points", 0),
                            "completed_transactions" to dataObj.optInt("completed_transactions", 0),
                            "average_profit_per_transaction" to dataObj.optDouble("average_profit_per_transaction", 0.0)
                        )
                        errorMessage = ""
                    } catch (parseError: Exception) {
                        errorMessage = "Failed to parse response: ${parseError.message}"
                    }
                } else {
                    errorMessage = "Failed to fetch data"
                }

                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message ?: "Unknown error loading profit data"}"
                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
            }
            delay(5000)
        }
    }

    val showBar = remember { true }
    LaunchedEffect(showBar) {
        onShowBottomBarChange(showBar)
    }

    CompositionLocalProvider(
        LocalProvidesBottomBar provides showBar
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AdminPageHeader(title = "Profit Summary", onMenuClick = onMenuClick)

            when {
                isLoading -> LoadingState()
                errorMessage.isNotEmpty() -> ErrorState(
                    message = errorMessage,
                    onRetry = { isInitialLoad = true; isLoading = true },
                )
                profitData.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Inventory2,
                    title = "No profit data available",
                    message = "There is nothing to show here yet.",
                )
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val items = listOf(
                        Pair("Total Profit", "${profitData["total_profit_points"] ?: 0} pts"),
                        Pair("Monthly Profit", "${profitData["monthly_profit_points"] ?: 0} pts"),
                        Pair("Completed Txns", (profitData["completed_transactions"] ?: 0).toString()),
                        Pair("Avg Per Txn", String.format("%.2f", profitData["average_profit_per_transaction"] ?: 0.0))
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(items[0].first, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(items[0].second, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(items[1].first, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(items[1].second, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(items[2].first, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(items[2].second, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(items[3].first, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(items[3].second, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SalesReportContent(
    onMenuClick: () -> Unit,
    onGoToChat: () -> Unit = {},
    onNavigateToPage: (DrawerPage) -> Unit = {},
    onShowBottomBarChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var salesData by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isInitialLoad by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val sharedPref = context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE)
                val token = sharedPref.getString("auth_token", "") ?: ""

                if (token.isBlank()) {
                    errorMessage = "Authentication failed: No token found"
                    if (isInitialLoad) {
                        isLoading = false
                        isInitialLoad = false
                    }
                    delay(5000)
                    continue
                }


                val responseData = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("https://fati-api.alertaraqc.com/api/admin/reports/sales")
                        .header("Authorization", "Bearer $token")
                        .header("Accept", "application/json")
                        .get()
                        .build()

                    adminHttpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            response.body?.string() ?: "{\"data\": {}}"
                        } else {
                            val errorBody = response.body?.string() ?: ""
                            null
                        }
                    }
                }

                if (responseData != null) {
                    try {
                        val json = JSONObject(responseData)
                        val dataObj = json.optJSONObject("data") ?: JSONObject()
                        val data = mutableListOf<Map<String, Any>>()

                        data.add(mapOf(
                            "total_items_sold" to dataObj.optInt("total_items_sold", 0),
                            "total_items_acquired" to dataObj.optInt("total_items_acquired", 0)
                        ))

                        val recentSalesArray = dataObj.optJSONArray("recent_sales") ?: JSONArray()
                        for (i in 0 until recentSalesArray.length()) {
                            val sale = recentSalesArray.getJSONObject(i)
                            val itemObj = sale.optJSONObject("item") ?: JSONObject()
                            val buyerObj = sale.optJSONObject("buyer") ?: JSONObject()
                            val sellerObj = sale.optJSONObject("seller") ?: JSONObject()
                            data.add(mapOf(
                                "item_title" to itemObj.optString("title", ""),
                                "buyer_email" to buyerObj.optString("email", ""),
                                "seller_email" to sellerObj.optString("email", ""),
                                "points_used" to sale.optInt("points_used", 0)
                            ))
                        }
                        salesData = data
                        errorMessage = ""
                    } catch (parseError: Exception) {
                        errorMessage = "Failed to parse response: ${parseError.message}"
                    }
                } else {
                    errorMessage = "Failed to fetch data"
                }

                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message ?: "Unknown error loading sales report"}"
                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
            }
            delay(5000)
        }
    }

    val showBar = remember { true }
    LaunchedEffect(showBar) {
        onShowBottomBarChange(showBar)
    }

    CompositionLocalProvider(
        LocalProvidesBottomBar provides showBar
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AdminPageHeader(title = "Sales Report", onMenuClick = onMenuClick)

            when {
                isLoading -> LoadingState()
                errorMessage.isNotEmpty() -> ErrorState(
                    message = errorMessage,
                    onRetry = { isInitialLoad = true; isLoading = true },
                )
                salesData.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Inventory2,
                    title = "No sales data found",
                    message = "There is nothing to show here yet.",
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        if (salesData.isNotEmpty()) {
                            val summary = salesData[0]
                            val sold = summary["total_items_sold"] as? Int ?: 0
                            val acquired = summary["total_items_acquired"] as? Int ?: 0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text("Sales Summary", style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = LocalMarketAccents.current.success.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("$sold", fontWeight = FontWeight.Bold, color = LocalMarketAccents.current.success)
                                                Text("Sold", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                        Surface(
                                            color = LocalMarketAccents.current.info.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("$acquired", fontWeight = FontWeight.Bold, color = LocalMarketAccents.current.info)
                                                Text("Acquired", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (salesData.size > 1) {
                        items(salesData.size - 1) { index ->
                            val sale = salesData[index + 1]
                            val itemTitle = (sale["item_title"] as? String) ?: "Unknown Item"
                            val buyerEmail = (sale["buyer_email"] as? String) ?: "Unknown Buyer"
                            val sellerEmail = (sale["seller_email"] as? String) ?: "Unknown Seller"
                            val pointsUsed = sale["points_used"] as? Int ?: 0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text(itemTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Buyer: $buyerEmail", style = MaterialTheme.typography.bodySmall)
                                    Text("Seller: $sellerEmail", style = MaterialTheme.typography.bodySmall)
                                    if (pointsUsed > 0) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "$pointsUsed pts used",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ProfitReportContent(
    onMenuClick: () -> Unit,
    onGoToChat: () -> Unit = {},
    onNavigateToPage: (DrawerPage) -> Unit = {},
    onShowBottomBarChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var totalMarkupProfit by remember { mutableStateOf("0.00") }
    var profitByMonth by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var topProfitableItems by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isInitialLoad by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val sharedPref = context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE)
                val token = sharedPref.getString("auth_token", "") ?: ""

                if (token.isBlank()) {
                    errorMessage = "Authentication failed: No token found"
                    if (isInitialLoad) {
                        isLoading = false
                        isInitialLoad = false
                    }
                    delay(5000)
                    continue
                }


                val responseData = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("https://fati-api.alertaraqc.com/api/admin/reports/profit")
                        .header("Authorization", "Bearer $token")
                        .header("Accept", "application/json")
                        .get()
                        .build()

                    adminHttpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            response.body?.string() ?: "{\"data\": {}}"
                        } else {
                            val errorBody = response.body?.string() ?: ""
                            null
                        }
                    }
                }

                if (responseData != null) {
                    try {
                        val json = JSONObject(responseData)
                        val dataObj = json.optJSONObject("data") ?: JSONObject()

                        totalMarkupProfit = dataObj.optString("total_profit", "0.00")

                        val profitByMonthArray = dataObj.optJSONArray("profit_by_month") ?: JSONArray()
                        val monthsData = mutableListOf<Map<String, Any>>()
                        for (i in 0 until profitByMonthArray.length()) {
                            val month = profitByMonthArray.getJSONObject(i)
                            monthsData.add(mapOf(
                                "month" to month.optString("month", ""),
                                "profit" to month.optInt("profit", 0)
                            ))
                        }
                        profitByMonth = monthsData

                        val topItemsArray = dataObj.optJSONArray("top_profitable_items") ?: JSONArray()
                        val itemsData = mutableListOf<Map<String, Any>>()
                        for (i in 0 until topItemsArray.length()) {
                            val item = topItemsArray.getJSONObject(i)
                            itemsData.add(mapOf(
                                "title" to item.optString("title", ""),
                                "markup" to item.optString("markup", "0.00"),
                                "seller_email" to (item.optJSONObject("seller")?.optString("email", "") ?: "")
                            ))
                        }
                        topProfitableItems = itemsData

                        errorMessage = ""
                    } catch (parseError: Exception) {
                        errorMessage = "Failed to parse response: ${parseError.message}"
                    }
                } else {
                    errorMessage = "Failed to fetch data"
                }

                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message ?: "Unknown error loading profit report"}"
                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
            }
            delay(5000)
        }
    }

    val showBar = remember { true }
    LaunchedEffect(showBar) {
        onShowBottomBarChange(showBar)
    }

    CompositionLocalProvider(
        LocalProvidesBottomBar provides showBar
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AdminPageHeader(title = "Profit Report", onMenuClick = onMenuClick)

            when {
                isLoading -> LoadingState()
                errorMessage.isNotEmpty() -> ErrorState(
                    message = errorMessage,
                    onRetry = { isInitialLoad = true; isLoading = true },
                )
                Money.parse(totalMarkupProfit)?.signum() == 0 && profitByMonth.isEmpty() && topProfitableItems.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Inventory2,
                    title = "No profit data found",
                    message = "There is nothing to show here yet.",
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Total Markup Profit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(Money.format(totalMarkupProfit), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    if (profitByMonth.isNotEmpty()) {
                        item {
                            Text("Profit by Month", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))
                        }
                        items(profitByMonth.size) { index ->
                            val month = profitByMonth[index]
                            val monthName = (month["month"] as? String) ?: "Unknown"
                            val profit = month["profit"] as? Int ?: 0
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(monthName, fontWeight = FontWeight.Bold)
                                    Text("$profit pts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (topProfitableItems.isNotEmpty()) {
                        item {
                            Text("Top Profitable Items", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))
                        }
                        items(topProfitableItems.size) { index ->
                            val item = topProfitableItems[index]
                            val title = (item["title"] as? String) ?: "Unknown Item"
                            val markup = (item["markup"] as? String) ?: "0.00"
                            val sellerEmail = (item["seller_email"] as? String) ?: "Unknown Seller"

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text(title, fontWeight = FontWeight.Bold)
                                    Text("Seller: $sellerEmail", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(4.dp))
                                    Text(Money.format(markup), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CategoryReportContent(
    onMenuClick: () -> Unit,
    onGoToChat: () -> Unit = {},
    onNavigateToPage: (DrawerPage) -> Unit = {},
    onShowBottomBarChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var mostSoldCategory by remember { mutableStateOf("") }
    var mostSoldCount by remember { mutableStateOf(0) }
    var categorySales by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isInitialLoad by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val sharedPref = context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE)
                val token = sharedPref.getString("auth_token", "") ?: ""

                if (token.isBlank()) {
                    errorMessage = "Authentication failed: No token found"
                    if (isInitialLoad) {
                        isLoading = false
                        isInitialLoad = false
                    }
                    delay(5000)
                    continue
                }


                val responseData = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("https://fati-api.alertaraqc.com/api/admin/reports/categories")
                        .header("Authorization", "Bearer $token")
                        .header("Accept", "application/json")
                        .get()
                        .build()

                    adminHttpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            response.body?.string() ?: "{\"data\": {}}"
                        } else {
                            val errorBody = response.body?.string() ?: ""
                            null
                        }
                    }
                }

                if (responseData != null) {
                    try {
                        val json = JSONObject(responseData)
                        val dataObj = json.optJSONObject("data") ?: JSONObject()

                        val categorySalesArray = dataObj.optJSONArray("category_sales") ?: JSONArray()
                        val salesData = mutableListOf<Map<String, Any>>()
                        for (i in 0 until categorySalesArray.length()) {
                            val category = categorySalesArray.getJSONObject(i)
                            salesData.add(mapOf(
                                "category_name" to category.optString("category_name", ""),
                                "items_sold" to category.optInt("items_sold", 0),
                                "total_markup_profit" to category.optInt("total_markup_profit", 0)
                            ))
                        }
                        categorySales = salesData

                        val mostSoldObj = dataObj.optJSONObject("most_sold_category") ?: JSONObject()
                        if (mostSoldObj.length() > 0) {
                            mostSoldCategory = mostSoldObj.optString("category_name", "")
                            mostSoldCount = mostSoldObj.optInt("items_sold", 0)
                        }

                        errorMessage = ""
                    } catch (parseError: Exception) {
                        errorMessage = "Failed to parse response: ${parseError.message}"
                    }
                } else {
                    errorMessage = "Failed to fetch data"
                }

                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message ?: "Unknown error loading category report"}"
                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
            }
            delay(5000)
        }
    }

    val showBar = remember { true }
    LaunchedEffect(showBar) {
        onShowBottomBarChange(showBar)
    }

    CompositionLocalProvider(
        LocalProvidesBottomBar provides showBar
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AdminPageHeader(title = "Category Sales Report", onMenuClick = onMenuClick)

            when {
                isLoading -> LoadingState()
                errorMessage.isNotEmpty() -> ErrorState(
                    message = errorMessage,
                    onRetry = { isInitialLoad = true; isLoading = true },
                )
                mostSoldCategory.isEmpty() && categorySales.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Inventory2,
                    title = "No category data found",
                    message = "There is nothing to show here yet.",
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (mostSoldCategory.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card),
                                colors = CardDefaults.cardColors(containerColor = LocalMarketAccents.current.success.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Most Sold Category", style = MaterialTheme.typography.bodySmall, color = LocalMarketAccents.current.success)
                                    Text(mostSoldCategory, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = LocalMarketAccents.current.success)
                                    Text("$mostSoldCount sold", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }

                    if (categorySales.isNotEmpty()) {
                        item {
                            Text("All Categories", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))
                        }
                        items(categorySales.size) { index ->
                            val category = categorySales[index]
                            val categoryName = (category["category_name"] as? String) ?: "Unknown Category"
                            val itemsSold = category["items_sold"] as? Int ?: 0
                            val markupProfit = category["total_markup_profit"] as? Int ?: 0

                            val performanceColor = when {
                                itemsSold >= 10 -> MaterialTheme.colorScheme.primary
                                itemsSold >= 5 -> LocalMarketAccents.current.warning
                                else -> MaterialTheme.colorScheme.error
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(categoryName, fontWeight = FontWeight.Bold)
                                        Surface(
                                            color = performanceColor.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "$itemsSold sold",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = performanceColor,
                                                modifier = Modifier.padding(4.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text("Profit: $markupProfit pts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun UserReportContent(
    onMenuClick: () -> Unit,
    onGoToChat: () -> Unit = {},
    onNavigateToPage: (DrawerPage) -> Unit = {},
    onShowBottomBarChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var activeUsers by remember { mutableStateOf(0) }
    var totalStudents by remember { mutableStateOf(0) }
    var topBuyers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var topSellers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var userActivityByMonth by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isInitialLoad by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val sharedPref = context.getSharedPreferences("fatimarket_prefs", Context.MODE_PRIVATE)
                val token = sharedPref.getString("auth_token", "") ?: ""

                if (token.isBlank()) {
                    errorMessage = "Authentication failed: No token found"
                    if (isInitialLoad) {
                        isLoading = false
                        isInitialLoad = false
                    }
                    delay(5000)
                    continue
                }


                val responseData = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("https://fati-api.alertaraqc.com/api/admin/reports/users")
                        .header("Authorization", "Bearer $token")
                        .header("Accept", "application/json")
                        .get()
                        .build()

                    adminHttpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            response.body?.string() ?: "{\"data\": {}}"
                        } else {
                            val errorBody = response.body?.string() ?: ""
                            null
                        }
                    }
                }

                if (responseData != null) {
                    try {
                        val json = JSONObject(responseData)
                        val data = json.optJSONObject("data") ?: JSONObject()

                        activeUsers = data.optInt("active_users", 0)
                        totalStudents = data.optInt("total_students", 0)

                        val buyersArray = data.optJSONArray("top_buyers") ?: JSONArray()
                        val buyersData = mutableListOf<Map<String, Any>>()
                        for (i in 0 until buyersArray.length()) {
                            val buyer = buyersArray.getJSONObject(i)
                            buyersData.add(mapOf(
                                "email" to buyer.optString("email", ""),
                                "wallet_points" to buyer.optInt("wallet_points", 0),
                                "transactions_count" to buyer.optInt("transactions_as_buyer_count", 0)
                            ))
                        }
                        topBuyers = buyersData

                        val sellersArray = data.optJSONArray("top_sellers") ?: JSONArray()
                        val sellersData = mutableListOf<Map<String, Any>>()
                        for (i in 0 until sellersArray.length()) {
                            val seller = sellersArray.getJSONObject(i)
                            sellersData.add(mapOf(
                                "email" to seller.optString("email", ""),
                                "wallet_points" to seller.optInt("wallet_points", 0),
                                "transactions_count" to seller.optInt("transactions_as_seller_count", 0)
                            ))
                        }
                        topSellers = sellersData

                        val activityArray = data.optJSONArray("user_activity_by_month") ?: JSONArray()
                        val activityData = mutableListOf<Map<String, Any>>()
                        for (i in 0 until activityArray.length()) {
                            val activity = activityArray.getJSONObject(i)
                            activityData.add(mapOf(
                                "month" to activity.optString("month", ""),
                                "count" to activity.optInt("count", 0)
                            ))
                        }
                        userActivityByMonth = activityData

                        errorMessage = ""
                    } catch (parseError: Exception) {
                        errorMessage = "Failed to parse response: ${parseError.message}"
                    }
                } else {
                    errorMessage = "Failed to fetch data"
                }

                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message ?: "Unknown error loading user report"}"
                if (isInitialLoad) {
                    isLoading = false
                    isInitialLoad = false
                }
            }
            delay(5000)
        }
    }

    val showBar = remember { true }
    LaunchedEffect(showBar) {
        onShowBottomBarChange(showBar)
    }

    CompositionLocalProvider(
        LocalProvidesBottomBar provides showBar
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AdminPageHeader(title = "User Report", onMenuClick = onMenuClick)

            when {
                isLoading -> LoadingState()
                errorMessage.isNotEmpty() -> ErrorState(
                    message = errorMessage,
                    onRetry = { isInitialLoad = true; isLoading = true },
                )
                activeUsers == 0 && totalStudents == 0 && topBuyers.isEmpty() && topSellers.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Inventory2,
                    title = "No user data available",
                    message = "There is nothing to show here yet.",
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        val statItems = listOf(
                            Pair("Active Users", activeUsers.toString()),
                            Pair("Total Students", totalStudents.toString()),
                            Pair("Top Buyers", topBuyers.size.toString()),
                            Pair("Top Sellers", topSellers.size.toString())
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(statItems[0].first, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(statItems[0].second, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                                    }
                                }
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(statItems[1].first, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(statItems[1].second, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(statItems[2].first, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(statItems[2].second, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                                    }
                                }
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(statItems[3].first, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(statItems[3].second, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                                    }
                                }
                            }
                        }
                    }

                    if (topBuyers.isNotEmpty()) {
                        item {
                            Text("Top Buyers", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))
                        }
                        items(topBuyers.size) { index ->
                            val buyer = topBuyers[index]
                            val email = (buyer["email"] as? String) ?: "Unknown User"
                            val points = buyer["wallet_points"] as? Int ?: 0
                            val txnCount = buyer["transactions_count"] as? Int ?: 0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text(email, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "$points pts",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "$txnCount transactions",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (topSellers.isNotEmpty()) {
                        item {
                            Text("Top Sellers", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))
                        }
                        items(topSellers.size) { index ->
                            val seller = topSellers[index]
                            val email = (seller["email"] as? String) ?: "Unknown User"
                            val points = seller["wallet_points"] as? Int ?: 0
                            val txnCount = seller["transactions_count"] as? Int ?: 0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text(email, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "$points pts",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "$txnCount transactions",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (userActivityByMonth.isNotEmpty()) {
                        item {
                            Text("User Activity by Month", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))
                        }
                        items(userActivityByMonth.size) { index ->
                            val activity = userActivityByMonth[index]
                            val month = (activity["month"] as? String) ?: "Unknown"
                            val count = activity["count"] as? Int ?: 0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text(month, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "$count active users",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}
