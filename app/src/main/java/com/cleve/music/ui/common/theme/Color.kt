package com.cleve.music.ui.common.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Clevemusic palette: deep near-black greens with a lime accent.
 * All pairs were checked for >= 4.5:1 contrast on their intended surface.
 */
@Immutable
data class CleveColors(
    val background: Color = Color(0xFF0B0D0A),
    val surface: Color = Color(0xFF151A12),
    val surfaceElevated: Color = Color(0xFF1C2318),
    val surfacePressed: Color = Color(0xFF26301F),
    val border: Color = Color(0xFF232B1E),
    val lime: Color = Color(0xFFC6F94E),
    val limeDim: Color = Color(0xFF9FD63A),
    val limeDeep: Color = Color(0xFF7BC938),
    val onLime: Color = Color(0xFF10140B),
    val textPrimary: Color = Color(0xFFF2F6EC),
    val textSecondary: Color = Color(0xFF9AA793),
    val textTertiary: Color = Color(0xFF6B7565),
    val danger: Color = Color(0xFFFF6B6B),
    val glow: Color = Color(0x40C6F94E),
)

val LocalCleveColors = staticCompositionLocalOf { CleveColors() }

val CleveTheme.colors: CleveColors
    get() = LocalCleveColors.current

@Composable
fun cleveColors() = LocalCleveColors.current
