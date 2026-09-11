package com.fati_market

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

/**
 * Safe areas that survive dialogs - the Kotlin edition of
 * react-native-safe-area-context.
 *
 * The problem: most of this app's screens are full-screen [Dialog]s, and a
 * dialog is its own window. On Android 15 (targetSdk 35) that window draws
 * behind the gesture bar, but the *bottom inset it reports is zero* - so
 * `navigationBarsPadding()` inside a dialog pads by nothing and the bottom
 * button sits under the gesture bar, no matter how many inset modifiers are
 * stacked on it. That is the bug that kept coming back.
 *
 * The fix mirrors react-native-safe-area-context: measure the insets ONCE at
 * the activity root - the one window whose insets are always right - and
 * carry the values down through a CompositionLocal. Dialog compositions
 * inherit locals from their call site, so [safeAreaBottom] works identically
 * inside a dialog, a bottom sheet, or the plain activity.
 *
 * Only the BOTTOM is carried this way. A dialog is not edge-to-edge at the
 * top - the system already places it below the status bar - so the top inset
 * is deliberately absent from [SafeAreaInsets] and [safeAreaTop] reads the
 * window's own status bar instead. See the note on [safeAreaTop].
 */
data class SafeAreaInsets(
    val bottom: Dp = 0.dp,
    val ime: Dp = 0.dp,
)

val LocalSafeArea = compositionLocalOf { SafeAreaInsets() }

/** Installed once, around the app's root, inside the activity window. */
@Composable
fun ProvideSafeArea(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val portrait = LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE
    val buttonBar = remember(context, density) { buttonNavigationBarHeight(context, density) }

    // These are state-backed reads: when the keyboard opens or the bars
    // change, this recomposes and every consumer follows.
    val insets = with(density) {
        // The 3-button bar reports itself as a tappable element as well as a
        // navigation bar; take whichever the device fills in.
        val reported = maxOf(
            WindowInsets.navigationBars.getBottom(this),
            WindowInsets.tappableElement.getBottom(this),
        ).toDp()

        SafeAreaInsets(
            // In portrait the button bar sits at the bottom, so never clear
            // less than its real height - some devices report a zero inset
            // while it is on screen, and the 16 dp floor then left the Buy
            // now bar under Back / Home / Recents.
            bottom = if (portrait) max(reported, buttonBar) else reported,
            ime = WindowInsets.ime.getBottom(this).toDp(),
        )
    }

    CompositionLocalProvider(LocalSafeArea provides insets, content = content)
}

/**
 * The 3-button navigation bar's height, read from the platform's own
 * resources, or 0 dp when the device navigates by gestures.
 *
 * Insets remain the source of truth; this is the floor under them for the
 * devices that report none while the buttons are showing.
 */
private fun buttonNavigationBarHeight(context: Context, density: Density): Dp {
    val res = context.resources

    // 0 = three buttons, 1 = two buttons, 2 = gestures.
    val modeId = res.getIdentifier("config_navBarInteractionMode", "integer", "android")
    val mode = if (modeId != 0) res.getInteger(modeId) else 0
    if (mode == 2) return 0.dp

    val heightId = res.getIdentifier("navigation_bar_height", "dimen", "android")
    if (heightId == 0) return 0.dp

    return with(density) { res.getDimensionPixelSize(heightId).toDp() }
}

/**
 * Bottom padding clearing the gesture bar - or the keyboard, whichever is
 * taller - measured at the activity, so it works inside dialogs too.
 */
fun Modifier.safeAreaBottom(): Modifier = composed {
    val safe = LocalSafeArea.current
    padding(bottom = max(safe.bottom, safe.ime))
}

/**
 * Bottom padding for a pinned action bar.
 *
 * Same as [safeAreaBottom] but with a floor: some devices (gesture mode with
 * the handle hidden, certain Xiaomi builds) report a ZERO bottom inset, and a
 * bar padded by zero sits flush against the screen edge - exactly the
 * clipped Buy Now button. The tab navigator never looks like that because its
 * rows carry their own height, so a pinned bar gets the same guarantee: the
 * real inset when there is one, and never less than the floor.
 */
fun Modifier.safeAreaBarBottom(floor: Dp = 12.dp): Modifier = composed {
    val safe = LocalSafeArea.current
    padding(bottom = max(max(safe.bottom, safe.ime), floor))
}

/**
 * The bottom clearance for a pinned bar, as a real sibling Spacer.
 *
 * This is the tab navigator's own construction:
 *
 *     Column {
 *         Row { ...the buttons... }
 *         Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
 *     }
 *
 * The tab bar never clips because the clearance is a laid-out row of its own,
 * not padding that a modifier order or a zero inset can quietly cancel. Put
 * this as the last child of any pinned bar and it behaves the same - reading
 * the activity-measured inset so it also works inside dialogs, and never
 * collapsing below [min] on devices that report no inset at all.
 */
@Composable
fun SafeAreaBottomSpacer(min: Dp = 16.dp, extra: Dp = 32.dp) {
    val safe = LocalSafeArea.current

    // Every caller now ends a scrolling page rather than a pinned bar, so the
    // last button gets [extra] breathing room above the system bar instead of
    // sitting right on top of it.
    Spacer(Modifier.height(max(max(safe.bottom, safe.ime), min) + extra))
}

/**
 * Top clearance for the status bar - which, unlike the bottom, must NOT be
 * carried down from the activity.
 *
 * A dialog window is only edge-to-edge at the *bottom*: it draws behind the
 * gesture bar (the reason [safeAreaBottom] exists) but the system already
 * positions it below the status bar. Padding it by the activity's measured
 * top inset therefore adds a second status bar of blank space - the oversized
 * gap that showed up on the student pages. Reading the window's own status
 * bar inset gives zero inside a dialog and the real height on an activity
 * screen, which is exactly what the admin dashboard's headers do.
 */
fun Modifier.safeAreaTop(): Modifier = composed {
    windowInsetsPadding(WindowInsets.statusBars)
}

/** A spacer the height of the status bar, for headers that draw behind it. */
fun Modifier.safeAreaTopHeight(): Modifier = composed {
    windowInsetsTopHeight(WindowInsets.statusBars)
}
