package com.cleve.music.ui.common.feedback

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * The "mencet" feel: an elastic press-in scale with a snappy spring and a light
 * haptic tick. Deliberately asymmetric — press-in is fast and non-bouncy, release
 * carries a small overshoot so the surface feels alive rather than dead.
 *
 * Call [rememberPressInteraction] and pass the source to the clickable so presses
 * are tracked.
 */
class PressState(
    val interactionSource: MutableInteractionSource = MutableInteractionSource(),
    val pressed: Boolean = false,
)

@Composable
fun rememberPressInteraction(): MutableInteractionSource =
    remember { MutableInteractionSource() }

@Composable
fun Modifier.pressFeedback(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    pressedScale: Float = 0.94f,
    haptic: Boolean = true,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(pressed) {
        if (pressed && enabled && haptic) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = if (pressed) Spring.DampingRatioNoBouncy else 0.34f,
            stiffness = if (pressed) 1100f else Spring.StiffnessMediumLow,
        ),
        label = "press-scale",
    )

    // Dim slightly while pressed so it reads as being pushed into the surface.
    val dim by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 900f),
        label = "press-dim",
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        alpha = dim
    }
}
