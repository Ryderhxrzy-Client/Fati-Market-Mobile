package com.fati_market

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.fati_market.ui.theme.FatiMarketFrontendTheme
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : ComponentActivity() {

    override fun onResume() {
        super.onResume()
        InAppNotifications.onEnterForeground()
    }

    override fun onPause() {
        super.onPause()
        InAppNotifications.onEnterBackground()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()  // Keep this

        // Block per-frame IME animation updates from reaching Compose so the keyboard
        // appears/disappears instantly instead of triggering a recompose on every frame.
        ViewCompat.setWindowInsetsAnimationCallback(
            window.decorView,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ) = insets
            }
        )

        // Configure Coil with memory + disk cache to avoid re-downloading images
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.25) // 25% of available memory
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("image_cache"))
                        .maxSizeBytes(50L * 1024 * 1024) // 50 MB
                        .build()
                }
                .crossfade(true)
                .build()
        )

        val prefs = getSharedPreferences("fatimarket_prefs", MODE_PRIVATE)

        setContent {
            var isDarkMode by remember {
                mutableStateOf(prefs.getBoolean("dark_mode", false))
            }

            FatiMarketFrontendTheme(darkTheme = isDarkMode) {
                // Insets are measured HERE, in the activity window - the one
                // place they are always right - and carried down so even the
                // full-screen dialogs (whose own windows report zero insets
                // on Android 15) pad correctly. See SafeArea.kt.
                ProvideSafeArea {
                    // The banner wraps the whole app so a push that arrives
                    // while the user is inside it shows here rather than as a
                    // system notification over the screen they are using.
                    InAppNotificationHost {
                        AppNavigation(
                            isDarkMode = isDarkMode,
                            onThemeToggle = {
                                isDarkMode = !isDarkMode
                                prefs.edit().putBoolean("dark_mode", isDarkMode).apply()
                            }
                        )
                    }
                }
            }
        }
    }
}
