package com.cleve.music.ui.player

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cleve.music.data.model.Track
import com.cleve.music.ui.common.component.GhostIconButton
import com.cleve.music.ui.common.component.LimeActionButton
import com.cleve.music.ui.common.component.TrackArtwork
import com.cleve.music.ui.common.feedback.pressFeedback
import com.cleve.music.ui.common.icon.Ph
import com.cleve.music.ui.common.icon.PhIcon
import com.cleve.music.ui.common.theme.cleveColors
import kotlin.math.sin

/** Compact bar shown above the bottom nav; tapping expands the full player. */
@Composable
fun MiniPlayer(
    track: Track?,
    isPlaying: Boolean,
    isLoading: Boolean,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (track == null) return
    val colors = cleveColors()
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .pressFeedback(interaction, pressedScale = 0.985f, haptic = false)
            .clickable(interactionSource = interaction, indication = null, onClick = onExpand)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TrackArtwork(url = track.thumbnailUrl, modifier = Modifier.size(44.dp), corner = 12.dp)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        WaveIndicator(
            isPlaying = isPlaying && !isLoading,
            modifier = Modifier.size(width = 26.dp, height = 20.dp),
        )

        GhostIconButton(
            icon = if (isPlaying) Ph.Pause else Ph.Play,
            onClick = onToggle,
            size = 38.dp,
            tint = colors.lime,
            filled = true,
        )

        GhostIconButton(
            icon = Ph.SkipForward,
            onClick = onNext,
            size = 34.dp,
            tint = colors.textSecondary,
        )
    }
}

/** Three animated bars; freezes into a flat line when paused. */
@Composable
fun WaveIndicator(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val colors = cleveColors()
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wave-phase",
    )

    Canvas(modifier = modifier) {
        val barWidth = size.width / 7f
        val gap = barWidth * 1.2f
        val baseline = size.height * 0.85f

        repeat(3) { i ->
            val amplitude = if (isPlaying) {
                val wave = sin(phase + i * 1.1f)
                size.height * 0.32f * (0.35f + (wave + 1f) / 2f)
            } else {
                size.height * 0.08f
            }
            val left = i * (barWidth + gap) + barWidth / 2f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(colors.lime, colors.limeDeep.copy(alpha = 0.6f)),
                    startY = baseline - amplitude,
                    endY = baseline,
                ),
                topLeft = Offset(left, baseline - amplitude),
                size = Size(barWidth, amplitude),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
    }
}

@Composable
fun ProgressStrip(
    progress: Float,
    modifier: Modifier = Modifier,
    onSeek: ((Float) -> Unit)? = null,
) {
    val colors = cleveColors()
    val safe = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .size(height = 4.dp, width = 0.dp)
            .clip(RoundedCornerShape(50))
            .background(colors.surfacePressed)
            .then(
                if (onSeek != null) {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onSeek(safe) },
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(safe)
                .size(height = 4.dp, width = 0.dp)
                .clip(RoundedCornerShape(50))
                .background(Brush.horizontalGradient(listOf(colors.limeDeep, colors.lime))),
        )
    }
}

/** Rounded pill control used in the expanded player. */
@Composable
fun RoundControl(
    icon: Ph,
    onClick: () -> Unit,
    active: Boolean,
    size: androidx.compose.ui.unit.Dp = 46.dp,
) {
    val colors = cleveColors()
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(50))
            .background(if (active) colors.lime.copy(alpha = 0.16f) else Color.Transparent)
            .pressFeedback(interaction, pressedScale = 0.85f)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PhIcon(
            icon = icon,
            size = size * 0.46f,
            tint = if (active) colors.lime else colors.textSecondary,
        )
    }
}

@Composable
fun formatTime(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val m = total / 60
    val s = total % 60
    return "%d:%02d".format(m, s)
}
