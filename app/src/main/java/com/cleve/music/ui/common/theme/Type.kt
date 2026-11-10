package com.cleve.music.ui.common.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cleve.music.R

/**
 * Two-family pairing:
 *  - Sora        → brand / display / big numbers (geometric, techy, tight tracking)
 *  - Outfit      → UI body, labels, captions (friendly, high legibility at small sizes)
 */
val SoraFamily = FontFamily(
    Font(R.font.sora, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.sora, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.sora, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.sora, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

val OutfitFamily = FontFamily(
    Font(R.font.outfit, weight = FontWeight.Light, variationSettings = FontVariation.Settings(FontVariation.weight(300))),
    Font(R.font.outfit, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.outfit, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.outfit, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.outfit, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

private fun sora(size: Int, weight: FontWeight, spacing: Double = -0.4, line: Int = (size * 1.25).toInt()) =
    TextStyle(
        fontFamily = SoraFamily,
        fontSize = size.sp,
        fontWeight = weight,
        lineHeight = line.sp,
        letterSpacing = spacing.sp,
    )

private fun outfit(size: Int, weight: FontWeight, spacing: Double = 0.0, line: Int = (size * 1.4).toInt()) =
    TextStyle(
        fontFamily = OutfitFamily,
        fontSize = size.sp,
        fontWeight = weight,
        lineHeight = line.sp,
        letterSpacing = spacing.sp,
    )

val CleveTypography = Typography(
    // Brand / display — Sora
    displayLarge = sora(40, FontWeight.Bold, -1.2, 46),
    displayMedium = sora(32, FontWeight.Bold, -0.9, 38),
    displaySmall = sora(26, FontWeight.SemiBold, -0.7, 32),
    headlineLarge = sora(24, FontWeight.SemiBold, -0.6, 30),
    headlineMedium = sora(20, FontWeight.SemiBold, -0.5, 26),
    headlineSmall = sora(18, FontWeight.SemiBold, -0.4, 24),

    // UI hierarchy — Outfit
    titleLarge = outfit(18, FontWeight.SemiBold, -0.1, 24),
    titleMedium = outfit(16, FontWeight.Medium, 0.0, 22),
    titleSmall = outfit(14, FontWeight.Medium, 0.0, 20),
    bodyLarge = outfit(15, FontWeight.Normal, 0.1, 22),
    bodyMedium = outfit(14, FontWeight.Normal, 0.1, 20),
    bodySmall = outfit(13, FontWeight.Normal, 0.1, 18),
    labelLarge = outfit(14, FontWeight.SemiBold, 0.2, 18),
    labelMedium = outfit(12, FontWeight.Medium, 0.3, 16),
    labelSmall = outfit(11, FontWeight.Medium, 0.4, 14),
)
