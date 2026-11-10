package com.cleve.music.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cleve.music.data.model.Track
import com.cleve.music.ui.common.component.GhostIconButton
import com.cleve.music.ui.common.component.LimeActionButton
import com.cleve.music.ui.common.component.TrackArtwork
import com.cleve.music.ui.common.icon.Ph
import com.cleve.music.ui.common.icon.PhIcon
import com.cleve.music.ui.common.theme.cleveColors

/**
 * Full-screen now-playing sheet. Artwork crossfades on track change, controls adopt
 * the lime accent, and the whole surface is wrapped in a soft radial glow.
 */
@Composable
fun NowPlayingScreen(
    track: Track?,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    shuffle: Boolean,
    repeatMode: Int,
    onBack: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onSeekRatio: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = cleveColors()
    if (track == null) {
        Box(modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.Center) {
            Text("Tidak ada lagu", color = colors.textSecondary)
        }
        return
    }

    val progress = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()) else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .background(
                Brush.radialGradient(
                    colors = listOf(colors.lime.copy(alpha = 0.10f), Color.Transparent),
                    radius = 1100f,
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GhostIconButton(icon = Ph.CaretDown, onClick = onBack, tint = colors.textSecondary)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SEDANG DIPUTAR",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.lime,
                        fontWeight = FontWeight.Bold,
                    )
                }
                GhostIconButton(icon = Ph.DotsThree, onClick = {}, tint = colors.textSecondary)
            }

            Spacer(Modifier.height(28.dp))

            // Artwork with lime halo
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(colors.lime.copy(alpha = 0.18f), colors.surfaceElevated),
                        ),
                    )
                    .padding(3.dp),
            ) {
                TrackArtwork(
                    url = track.thumbnailUrl,
                    modifier = Modifier.fillMaxSize(),
                    corner = 26.dp,
                )
                // Small vinyl badge for a "physical media" cue.
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(14.dp)
                        .size(38.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.background.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center,
                ) {
                    PhIcon(icon = Ph.VinylRecord, size = 20.dp, tint = colors.lime)
                }
            }

            Spacer(Modifier.height(30.dp))

            Text(
                text = track.title,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(26.dp))

            ProgressStrip(
                progress = progress,
                onSeek = onSeekRatio,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatTime(positionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
                Text(
                    text = formatTime(durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
            }

            Spacer(Modifier.height(18.dp))

            // Transport
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundControl(
                    icon = Ph.Shuffle,
                    onClick = onToggleShuffle,
                    active = shuffle,
                )
                GhostIconButton(
                    icon = Ph.SkipBack,
                    onClick = onPrevious,
                    size = 44.dp,
                    tint = colors.textPrimary,
                )
                LimeActionButton(
                    icon = if (isPlaying) Ph.Pause else Ph.Play,
                    onClick = onToggle,
                    size = 76.dp,
                )
                GhostIconButton(
                    icon = Ph.SkipForward,
                    onClick = onNext,
                    size = 44.dp,
                    tint = colors.textPrimary,
                )
                RoundControl(
                    icon = if (repeatMode == 2) Ph.RepeatOnce else Ph.Repeat,
                    onClick = onCycleRepeat,
                    active = repeatMode != 0,
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GhostIconButton(icon = Ph.Heart, onClick = {}, size = 40.dp, tint = colors.textSecondary)
                GhostIconButton(icon = Ph.ListDashes, onClick = {}, size = 40.dp, tint = colors.textSecondary)
                GhostIconButton(icon = Ph.ShareNetwork, onClick = {}, size = 40.dp, tint = colors.textSecondary)
                GhostIconButton(icon = Ph.DownloadSimple, onClick = {}, size = 40.dp, tint = colors.textSecondary)
            }
        }
    }
}
