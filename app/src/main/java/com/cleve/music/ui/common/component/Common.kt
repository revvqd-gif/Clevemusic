package com.cleve.music.ui.common.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cleve.music.ui.common.feedback.pressFeedback
import com.cleve.music.ui.common.icon.Ph
import com.cleve.music.ui.common.icon.PhIcon
import com.cleve.music.ui.common.theme.cleveColors

/** Lime filled action button with press feedback and an outer glow while held. */
@Composable
fun LimeActionButton(
    icon: Ph,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    enabled: Boolean = true,
) {
    val colors = cleveColors()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val glow by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 700f),
        label = "glow",
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(1f + glow * 0.06f)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(colors.lime, colors.limeDim)))
            .border(
                width = 1.dp,
                color = colors.lime.copy(alpha = 0.5f + glow * 0.5f),
                shape = CircleShape,
            )
            .pressFeedback(interaction, enabled = enabled, pressedScale = 0.9f)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        PhIcon(icon = icon, size = size * 0.46f, tint = colors.onLime)
    }
}

@Composable
fun GhostIconButton(
    icon: Ph,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color = cleveColors().textPrimary,
    filled: Boolean = false,
) {
    val colors = cleveColors()
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (filled) {
                    Modifier
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.border, CircleShape)
                } else {
                    Modifier
                },
            )
            .pressFeedback(interaction, pressedScale = 0.88f)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PhIcon(icon = icon, size = size * 0.48f, tint = tint)
    }
}

@Composable
fun TrackArtwork(
    url: String?,
    modifier: Modifier = Modifier,
    corner: Dp = 14.dp,
    placeholderColor: Color = cleveColors().surfaceElevated,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(placeholderColor),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isNullOrBlank()) {
            PhIcon(icon = Ph.MusicNotes, size = 22.dp, tint = cleveColors().textTertiary)
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = cleveColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.textPrimary,
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = colors.lime,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
