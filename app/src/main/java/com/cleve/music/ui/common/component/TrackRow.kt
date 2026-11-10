package com.cleve.music.ui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cleve.music.data.model.Track
import com.cleve.music.ui.common.feedback.pressFeedback
import com.cleve.music.ui.common.icon.Ph
import com.cleve.music.ui.common.icon.PhIcon
import com.cleve.music.ui.common.theme.cleveColors

/**
 * Row used across search results, recent, and playlists. Pressing the whole row
 * triggers playback; the trailing button opens the per-track sheet.
 */
@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    onMore: (() -> Unit)? = null,
    isActive: Boolean = false,
    isResolving: Boolean = false,
    index: Int? = null,
    modifier: Modifier = Modifier,
) {
    val colors = cleveColors()
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (isActive) {
                    Modifier
                        .background(colors.lime.copy(alpha = 0.08f))
                        .border(1.dp, colors.lime.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                } else {
                    Modifier
                },
            )
            .pressFeedback(interaction, pressedScale = 0.97f, haptic = false)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (index != null) {
            Text(
                text = "%02d".format(index + 1),
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) colors.lime else colors.textTertiary,
                modifier = Modifier.padding(start = 6.dp),
            )
        }

        TrackArtwork(
            url = track.thumbnailUrl,
            modifier = Modifier.size(52.dp),
            corner = 14.dp,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isActive) colors.lime else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (isResolving) {
            PhIcon(
                icon = Ph.Spinner,
                size = 20.dp,
                tint = colors.lime,
                modifier = Modifier.padding(end = 4.dp),
            )
        } else {
            Text(
                text = track.durationLabel,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textTertiary,
            )
        }

        if (onMore != null) {
            GhostIconButton(
                icon = Ph.DotsThreeVertical,
                onClick = onMore,
                size = 36.dp,
                tint = colors.textSecondary,
            )
        }
    }
}

/** Shimmer-ish loading placeholder; avoids janky spinners on the result list. */
@Composable
fun SkeletonRow(modifier: Modifier = Modifier) {
    val colors = cleveColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceElevated),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(0.75f)
                    .size(width = 0.dp, height = 14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surfaceElevated),
            )
            Box(
                Modifier
                    .fillMaxWidth(0.45f)
                    .size(width = 0.dp, height = 10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surfaceElevated.copy(alpha = 0.7f)),
            )
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = cleveColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PhIcon(icon = Ph.Search, size = 20.dp, tint = colors.textSecondary)
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary),
            cursorBrush = Brush.verticalGradient(listOf(colors.lime, colors.limeDim)),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = androidx.compose.ui.text.input.ImeAction.Search,
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSearch = { onSubmit() },
            ),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = "Cari lagu, artis, atau album",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textTertiary,
                    )
                }
                inner()
            },
        )
        if (value.isNotEmpty()) {
            GhostIconButton(
                icon = Ph.X,
                onClick = { onValueChange("") },
                size = 28.dp,
                tint = colors.textTertiary,
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: Ph,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    val colors = cleveColors()
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(colors.lime.copy(alpha = 0.10f))
                .border(1.dp, colors.lime.copy(alpha = 0.25f), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center,
        ) {
            PhIcon(icon = icon, size = 30.dp, tint = colors.lime)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
fun Capsule(
    text: String,
    active: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = cleveColors()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (active) colors.lime else colors.surfaceElevated)
            .then(
                if (active) Modifier else Modifier.border(1.dp, colors.border, RoundedCornerShape(50)),
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (active) colors.onLime else colors.textSecondary,
        )
    }
}

@Composable
fun GlowBackground(content: @Composable () -> Unit) {
    val colors = cleveColors()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        colors.lime.copy(alpha = 0.13f),
                        Color.Transparent,
                    ),
                    radius = 900f,
                ),
            ),
    ) {
        content()
    }
}
