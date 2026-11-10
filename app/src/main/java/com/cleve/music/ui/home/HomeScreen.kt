package com.cleve.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cleve.music.data.model.Track
import com.cleve.music.ui.common.component.EmptyState
import com.cleve.music.ui.common.component.LimeActionButton
import com.cleve.music.ui.common.component.SectionHeader
import com.cleve.music.ui.common.component.TrackRow
import com.cleve.music.ui.common.icon.Ph
import com.cleve.music.ui.common.icon.PhIcon
import com.cleve.music.ui.common.theme.cleveColors

@Composable
fun HomeScreen(
    recent: List<Track>,
    nowPlaying: Track?,
    isResolving: (String) -> Boolean,
    onPlay: (Track) -> Unit,
    onPlayAll: () -> Unit,
    onSearchTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = cleveColors()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            BrandHeader(onSearchTap = onSearchTap)
        }

        item {
            Spacer(Modifier.height(6.dp))
            HeroCard(
                nowPlaying = nowPlaying,
                onPlayAll = onPlayAll,
            )
        }

        item {
            Spacer(Modifier.height(18.dp))
            SectionHeader(title = "Baru diputar")
        }

        if (recent.isEmpty()) {
            item {
                EmptyState(
                    icon = Ph.MusicNotes,
                    title = "Belum ada lagu",
                    subtitle = "Tekan tombol cari di atas untuk mulai.",
                )
            }
        } else {
            items(recent, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    onClick = { onPlay(track) },
                    isActive = nowPlaying?.id == track.id,
                    isResolving = isResolving(track.id),
                    modifier = Modifier.padding(horizontal = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun BrandHeader(onSearchTap: () -> Unit) {
    val colors = cleveColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Brush.linearGradient(listOf(colors.lime, colors.limeDeep))),
                contentAlignment = Alignment.Center,
            ) {
                PhIcon(icon = Ph.Waveform, size = 20.dp, tint = colors.onLime)
            }
            Column {
                Text(
                    text = "Clevemusic",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                )
                Text(
                    text = "Dengarkan sesukamu",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
            }
        }

        LimeActionButton(
            icon = Ph.Search,
            onClick = onSearchTap,
            size = 44.dp,
        )
    }
}

@Composable
private fun HeroCard(nowPlaying: Track?, onPlayAll: () -> Unit) {
    val colors = cleveColors()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.limeDeep.copy(alpha = 0.22f),
                        colors.surfaceDeep,
                    ),
                ),
            )
            .padding(20.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PhIcon(icon = Ph.Waveform, size = 16.dp, tint = colors.lime)
                Text(
                    text = "SIAP DIPUTAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.lime,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = nowPlaying?.title ?: "Mulai dari pencarian",
                style = MaterialTheme.typography.displaySmall,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = nowPlaying?.artist ?: "Audio langsung dari YouTube, tanpa iklan jeda.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                LimeActionButton(icon = Ph.Play, onClick = onPlayAll, size = 52.dp)
                Text(
                    text = "Putar",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
