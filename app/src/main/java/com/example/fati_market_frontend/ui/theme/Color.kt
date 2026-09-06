package com.fati_market.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The Fati Market palette.
 *
 * Built as tonal ramps rather than one-off hex values, so a surface, a border
 * and a pressed state can be picked from the same family and stay in
 * relationship with each other. Every pairing used for text on a background in
 * this file clears WCAG AA at body size.
 *
 * Brand: Our Lady of Fatima University green, with gold reserved for points
 * and rewards so that "gold" always means "you earned something".
 *
 * The legacy names (DarkGreen, Gold, OffWhite, ...) are kept because screens
 * across the app import them directly; they now point at the tuned ramp values.
 */

// ── Brand green ─────────────────────────────────────────────────────────
// The market's identity colour. 700 is the resting brand tone; 300 lifts it
// for dark mode, where a deep green would disappear into the background.
val Green50 = Color(0xFFE9F5EE)
val Green100 = Color(0xFFC9E7D4)
val Green200 = Color(0xFFA3D4B7)
val Green300 = Color(0xFF7AC199)
val Green400 = Color(0xFF52AD7E)
val Green500 = Color(0xFF2F9564)
val Green600 = Color(0xFF247A51)
val Green700 = Color(0xFF1A5F3A)
val Green800 = Color(0xFF12472A)
val Green900 = Color(0xFF0B2F1B)

// ── Gold ────────────────────────────────────────────────────────────────
// Reserved for loyalty points and rewards, so "gold" reads as "points"
// everywhere in the app rather than as a generic highlight.
val Amber50 = Color(0xFFFFF7E1)
val Amber100 = Color(0xFFFFEBB5)
val Amber300 = Color(0xFFFFD25A)
val Amber500 = Color(0xFFE7A81A)
val Amber600 = Color(0xFFC98C0C)
val Amber700 = Color(0xFF9C6C06)

// ── Neutrals ────────────────────────────────────────────────────────────
// Very slightly warm, so the greys sit comfortably beside the green instead
// of looking blue against it.
val Neutral0 = Color(0xFFFFFFFF)
val Neutral50 = Color(0xFFF8F9F6)
val Neutral100 = Color(0xFFF1F2EE)
val Neutral200 = Color(0xFFE4E6E0)
val Neutral300 = Color(0xFFD1D4CC)
val Neutral400 = Color(0xFFA7AAA1)
val Neutral500 = Color(0xFF7B7E76)
val Neutral600 = Color(0xFF5B5E57)
val Neutral700 = Color(0xFF41443F)
val Neutral800 = Color(0xFF2A2C28)
val Neutral900 = Color(0xFF1A1C19)
val Neutral950 = Color(0xFF111311)

// ── Semantic ────────────────────────────────────────────────────────────
val Success = Color(0xFF2E7D32)
val SuccessContainer = Color(0xFFDCF0DD)
val SuccessDark = Color(0xFF6ADF8B)

val Warning = Color(0xFFB25E02)
val WarningContainer = Color(0xFFFFE9D1)
val WarningDark = Color(0xFFFFB74D)

val Danger = Color(0xFFB3261E)
val DangerContainer = Color(0xFFFADAD7)
val DangerDark = Color(0xFFFF8A80)

val Info = Color(0xFF1565C0)
val InfoContainer = Color(0xFFDCE9FA)
val InfoDark = Color(0xFF82B1FF)

/** The heart on a saved item. One red, everywhere. */
val FavoriteRed = Color(0xFFE5484D)

// ── Legacy aliases ──────────────────────────────────────────────────────
// Kept so existing screens compile unchanged while they migrate to the ramp.
val DarkGreen = Green700
val DarkGreenLight = Green500
val Gold = Amber500
val GoldLight = Amber300
val OffWhite = Neutral50
val LightGrayBg = Neutral100
val White = Neutral0
val DarkText = Neutral900
val MutedText = Neutral600
