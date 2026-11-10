package com.cleve.music.ui.common.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Clevemusic palette: true black base with a lime accent.
 *
 * `background` and `surfaceDeep` are pure #000000 so OLED panels draw no pixels at
 * all. Separation between layers comes from raised greys, not from a tinted base.
 */
@Immutable
data class CleveColors(
    val background: Color = Color(0xFF000000),
    val surfaceDeep: Color = Color(0xFF070707),
    val surface: Color = Color(0xFF101010),
    val surfaceElevated: Color = Color(0xFF181818),
    val surfacePressed: Color = Color(0xFF242424),
    val border: Color = Color(0xFF242424),
    val lime: Color = Color(0xFFC6F94E),
    val limeDim: Color = Color(0xFF9FD63A),
    val limeDeep: Color = Color(0xFF7BC938),
    val onLime: Color = Color(0xFF0A0C06),
    val textPrimary: Color = Color(0xFFF5F7F2),
    val textSecondary: Color = Color(0xFF9BA396),
    val textTertiary: Color = Color(0xFF6E7669),
    val danger: Color = Color(0xFFFF6B6B),
    val glow: Color = Color(0x40C6F94E),
)

val LocalCleveColors = staticCompositionLocalOf { CleveColors() }

@Composable
fun cleveColors() = LocalCleveColors.current
