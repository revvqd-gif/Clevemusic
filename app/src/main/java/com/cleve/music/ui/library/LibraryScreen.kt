package com.cleve.music.ui.library

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cleve.music.data.local.PlaylistEntity
import com.cleve.music.data.model.Track
import com.cleve.music.ui.common.component.EmptyState
import com.cleve.music.ui.common.component.GhostIconButton
import com.cleve.music.ui.common.component.LimeActionButton
import com.cleve.music.ui.common.component.SectionHeader
import com.cleve.music.ui.common.component.TrackArtwork
import com.cleve.music.ui.common.component.TrackRow
import com.cleve.music.ui.common.feedback.pressFeedback
import com.cleve.music.ui.common.icon.Ph
import com.cleve.music.ui.common.icon.PhIcon
import com.cleve.music.ui.common.theme.cleveColors
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember

@Composable
fun LibraryScreen(
    playlists: List<PlaylistEntity>,
    recent: List<Track>,
    nowPlaying: Track?,
    isResolving: (String) -> Boolean,
    onOpenPlaylist: (Long) -> Unit,
    onNewPlaylist: () -> Unit,
    onPlay: (Track) -> Unit,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Koleksi",
                        style = MaterialTheme.typography.displaySmall,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${playlists.size} playlist · ${recent.size} lagu",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                    )
                }
                LimeActionButton(icon = Ph.Plus, onClick = onNewPlaylist, size = 44.dp)
            }
        }

        item { SectionHeader(title = "Playlist") }

        if (playlists.isEmpty()) {
            item {
                EmptyState(
                    icon = Ph.Playlist,
                    title = "Belum ada playlist",
                    subtitle = "Tekan + untuk membuat koleksi pertamamu.",
                )
            }
        } else {
            items(playlists, key = { it.id }) { playlist ->
                PlaylistRow(
                    playlist = playlist,
                    onClick = { onOpenPlaylist(playlist.id) },
                )
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            SectionHeader(title = "Semua lagu tersimpan")
        }

        items(recent, key = { "recent-${it.id}" }) { track ->
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

@Composable
private fun PlaylistRow(playlist: PlaylistEntity, onClick: () -> Unit) {
    val colors = cleveColors()
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(18.dp))
            .pressFeedback(interaction, pressedScale = 0.97f, haptic = false)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.lime.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            PhIcon(icon = Ph.Playlist, size = 24.dp, tint = colors.lime)
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Playlist",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
            )
        }
        GhostIconButton(icon = Ph.CaretLeft, onClick = onClick, size = 34.dp, tint = colors.textTertiary)
    }
}

@Composable
fun PlaylistDetailScreen(
    name: String,
    tracks: List<Track>,
    nowPlaying: Track?,
    isResolving: (String) -> Boolean,
    onBack: () -> Unit,
    onPlay: (Track, List<Track>) -> Unit,
    onPlayAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = cleveColors()

    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GhostIconButton(icon = Ph.CaretLeft, onClick = onBack, tint = colors.textPrimary)
            Column(Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${tracks.size} lagu",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
            }
            if (tracks.isNotEmpty()) {
                LimeActionButton(icon = Ph.Play, onClick = onPlayAll, size = 44.dp)
            }
        }

        if (tracks.isEmpty()) {
            EmptyState(
                icon = Ph.MusicNotes,
                title = "Playlist kosong",
                subtitle = "Tambahkan lagu dari hasil pencarian.",
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                items(tracks, key = { it.id }) { track ->
                    TrackRow(
                        track = track,
                        onClick = { onPlay(track, tracks) },
                        isActive = nowPlaying?.id == track.id,
                        isResolving = isResolving(track.id),
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )
                }
            }
        }
    }
}
