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
 * The legacy names (DarkGreen, Gold, OffWhite, ...) are kept because screens
 * across the app import them directly; they now point at the tuned ramp values.
 */

// ── Brand green ─────────────────────────────────────────────────────────
// The market's identity colour. 700 is the resting brand tone; 500 lifts it
// for dark mode, where a deep green would disappear into the background.
val Green50 = Color(0xFFE8F3EC)
val Green100 = Color(0xFFC6E2D1)
val Green200 = Color(0xFF9ECEB3)
val Green300 = Color(0xFF72B994)
val Green400 = Color(0xFF4CA67D)
val Green500 = Color(0xFF2E8F63)
val Green600 = Color(0xFF247551)
val Green700 = Color(0xFF1A5C38)
val Green800 = Color(0xFF124528)
val Green900 = Color(0xFF0B2E1A)

// ── Amber ───────────────────────────────────────────────────────────────
// Reserved for loyalty points and rewards, so "amber" reads as "points"
// everywhere in the app rather than as a generic highlight.
val Amber50 = Color(0xFFFFF6E0)
val Amber100 = Color(0xFFFFE9B3)
val Amber300 = Color(0xFFFFCF52)
val Amber500 = Color(0xFFE0A213)
val Amber600 = Color(0xFFC4880A)
val Amber700 = Color(0xFF9A6A05)

// ── Neutrals ────────────────────────────────────────────────────────────
// Very slightly warm, so the greys sit comfortably beside the green instead
// of looking blue against it.
val Neutral0 = Color(0xFFFFFFFF)
val Neutral50 = Color(0xFFFAFAF8)
val Neutral100 = Color(0xFFF2F2EE)
val Neutral200 = Color(0xFFE6E6E0)
val Neutral300 = Color(0xFFD3D3CB)
val Neutral400 = Color(0xFFA9A9A0)
val Neutral500 = Color(0xFF7C7C74)
val Neutral600 = Color(0xFF5C5C56)
val Neutral700 = Color(0xFF42423E)
val Neutral800 = Color(0xFF2A2A27)
val Neutral900 = Color(0xFF1A1A18)
val Neutral950 = Color(0xFF111110)

// ── Semantic ────────────────────────────────────────────────────────────
val Success = Color(0xFF2E7D32)
val SuccessContainer = Color(0xFFDCF0DD)
val SuccessDark = Color(0xFF6ADf8B)

val Warning = Color(0xFFB25E02)
val WarningContainer = Color(0xFFFFE9D1)
val WarningDark = Color(0xFFFFB74D)

val Danger = Color(0xFFB3261E)
val DangerContainer = Color(0xFFFADAD7)
val DangerDark = Color(0xFFFF8A80)

val Info = Color(0xFF1565C0)
val InfoContainer = Color(0xFFDCE9FA)
val InfoDark = Color(0xFF82B1FF)

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
