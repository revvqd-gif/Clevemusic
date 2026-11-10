package com.cleve.music.ui.common.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val CleveScheme = darkColorScheme(
    primary = CleveColors().lime,
    onPrimary = CleveColors().onLime,
    primaryContainer = CleveColors().limeDeep,
    onPrimaryContainer = CleveColors().onLime,
    secondary = CleveColors().limeDim,
    onSecondary = CleveColors().onLime,
    background = CleveColors().background,
    onBackground = CleveColors().textPrimary,
    surface = CleveColors().surface,
    onSurface = CleveColors().textPrimary,
    surfaceVariant = CleveColors().surfaceElevated,
    onSurfaceVariant = CleveColors().textSecondary,
    surfaceContainer = CleveColors().surfaceElevated,
    surfaceContainerHigh = CleveColors().surfaceElevated,
    surfaceContainerHighest = CleveColors().surfacePressed,
    surfaceContainerLow = CleveColors().surface,
    surfaceContainerLowest = CleveColors().background,
    outline = CleveColors().border,
    outlineVariant = CleveColors().border,
    error = CleveColors().danger,
)

@Composable
fun CleveTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalCleveColors provides CleveColors()) {
        MaterialTheme(
            colorScheme = CleveScheme,
            typography = CleveTypography,
            content = content,
        )
    }
}
