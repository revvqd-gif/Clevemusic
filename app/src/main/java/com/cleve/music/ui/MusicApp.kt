package com.cleve.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cleve.music.data.model.Track
import com.cleve.music.ui.common.component.GhostIconButton
import com.cleve.music.ui.common.component.LimeActionButton
import com.cleve.music.ui.common.component.TrackArtwork
import com.cleve.music.ui.common.feedback.pressFeedback
import com.cleve.music.ui.common.icon.Ph
import com.cleve.music.ui.common.icon.PhIcon
import com.cleve.music.ui.common.theme.CleveTypography
import com.cleve.music.ui.common.theme.CleveTheme
import com.cleve.music.ui.common.theme.cleveColors
import com.cleve.music.ui.home.HomeScreen
import com.cleve.music.ui.library.LibraryScreen
import com.cleve.music.ui.library.PlaylistDetailScreen
import com.cleve.music.ui.player.MiniPlayer
import com.cleve.music.ui.player.NowPlayingScreen
import com.cleve.music.ui.search.SearchScreen
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.imePadding

private enum class Tab(val label: String, val icon: Ph) {
    Home("Beranda", Ph.House),
    Search("Cari", Ph.MagnifyingGlass),
    Library("Koleksi", Ph.Playlist),
}

@Composable
fun MusicApp(viewModel: MusicViewModel) {
    CleveTheme {
        val colors = cleveColors()
        val state by viewModel.state.collectAsState()
        val recent by viewModel.recent.collectAsState()
        val playlists by viewModel.playlists.collectAsState()
        val nowPlaying by viewModel.nowPlaying.collectAsState()
        val isPlaying by viewModel.isPlaying.collectAsState()

        var tab by remember { mutableStateOf(Tab.Home) }
        var expanded by remember { mutableStateOf(false) }
        var openPlaylistId by remember { mutableStateOf<Long?>(null) }
        var showCreateDialog by remember { mutableStateOf(false) }
        var sheetTrack by remember { mutableStateOf<Track?>(null) }
        var positionTick by remember { mutableStateOf(0) }

        val shuffle by viewModel.shuffleState.collectAsState()
        val repeatMode by viewModel.repeatModeState.collectAsState()

        // Poll playback position only while the full player is visible.
        LaunchedEffect(expanded, isPlaying) {
            if (!expanded) return@LaunchedEffect
            while (true) {
                positionTick++
                delay(500)
            }
        }
        val positionMs = remember(positionTick) { viewModel.positionMsCached() }
        val durationMs = remember(nowPlaying, positionTick) { viewModel.durationMsCached() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    when {
                        openPlaylistId != null -> {
                            val pid = openPlaylistId!!
                            val detail by viewModel.playlistTracksState(pid).collectAsState(emptyList())
                            PlaylistDetailScreen(
                                name = playlists.firstOrNull { it.id == pid }?.name ?: "Playlist",
                                tracks = detail,
                                nowPlaying = nowPlaying?.track,
                                isResolving = { state.resolvingTrackId == it },
                                onBack = { openPlaylistId = null },
                                onPlay = { track, queue -> viewModel.play(track, queue) },
                                onPlayAll = { detail.firstOrNull()?.let { viewModel.play(it, detail) } },
                            )
                        }

                        tab == Tab.Home -> HomeScreen(
                            recent = recent,
                            nowPlaying = nowPlaying?.track,
                            isResolving = { state.resolvingTrackId == it },
                            onPlay = { viewModel.play(it) },
                            onPlayAll = { recent.firstOrNull()?.let { viewModel.play(it, recent) } },
                            onSearchTap = { tab = Tab.Search },
                        )

                        tab == Tab.Search -> SearchScreen(
                            query = state.query,
                            results = state.results,
                            searching = state.searching,
                            error = state.error,
                            nowPlaying = nowPlaying?.track,
                            isResolving = { state.resolvingTrackId == it },
                            onQueryChange = viewModel::onQueryChange,
                            onSubmit = viewModel::searchNow,
                            onPlay = { track, queue -> viewModel.play(track, queue) },
                            onMore = { sheetTrack = it },
                        )

                        else -> LibraryScreen(
                            playlists = playlists,
                            recent = recent,
                            nowPlaying = nowPlaying?.track,
                            isResolving = { state.resolvingTrackId == it },
                            onOpenPlaylist = { openPlaylistId = it },
                            onNewPlaylist = { showCreateDialog = true },
                            onPlay = { viewModel.play(it) },
                        )
                    }
                }

                // Mini player sits right above the nav bar.
                MiniPlayer(
                    track = nowPlaying?.track,
                    isPlaying = isPlaying,
                    isLoading = state.resolvingTrackId != null,
                    onToggle = viewModel::togglePlayPause,
                    onNext = viewModel::next,
                    onExpand = { expanded = true },
                )

                BottomNav(
                    current = tab,
                    onSelect = { tab = it; openPlaylistId = null },
                )
            }

            // Full player overlay
            AnimatedVisibility(
                visible = expanded,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                NowPlayingScreen(
                    track = nowPlaying?.track,
                    isPlaying = isPlaying,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    shuffle = shuffle,
                    repeatMode = repeatMode,
                    onBack = { expanded = false },
                    onToggle = viewModel::togglePlayPause,
                    onNext = viewModel::next,
                    onPrevious = viewModel::previous,
                    onToggleShuffle = viewModel::toggleShuffle,
                    onCycleRepeat = { viewModel.cycleRepeat() },
                    onSeekRatio = { ratio ->
                        val d = viewModel.durationMsCached()
                        if (d > 0) viewModel.seekTo((d * ratio).toLong())
                    },
                )
            }

            // Create-playlist dialog
            if (showCreateDialog) {
                CreatePlaylistDialog(
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { name ->
                        viewModel.createPlaylist(name) { id ->
                            showCreateDialog = false
                            openPlaylistId = id
                        }
                    },
                )
            }

            // Track action sheet
            sheetTrack?.let { track ->
                TrackActionSheet(
                    track = track,
                    playlists = playlists,
                    onDismiss = { sheetTrack = null },
                    onAdd = { pid ->
                        viewModel.addToPlaylist(pid, track)
                        sheetTrack = null
                    },
                    onCreateNew = {
                        showCreateDialog = true
                        sheetTrack = null
                    },
                )
            }

            // Toast
            Toast(
                message = state.toast,
                onGone = viewModel::clearToast,
            )
        }
    }
}

@Composable
private fun BottomNav(current: Tab, onSelect: (Tab) -> Unit) {
    val colors = cleveColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Tab.entries.forEach { tab ->
            val selected = tab == current
            val tint by animateColorAsState(
                targetValue = if (selected) colors.onLime else colors.textSecondary,
                animationSpec = tween(220),
                label = "nav-tint",
            )
            val bg by animateColorAsState(
                targetValue = if (selected) colors.lime else colors.surfaceElevated.copy(alpha = 0.6f),
                animationSpec = tween(220),
                label = "nav-bg",
            )
            val interaction = remember { MutableInteractionSource() }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(bg)
                    .then(
                        if (selected) Modifier else Modifier.border(
                            1.dp,
                            colors.border,
                            RoundedCornerShape(18.dp),
                        ),
                    )
                    .pressFeedback(interaction, pressedScale = 0.93f)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = { onSelect(tab) },
                    )
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PhIcon(icon = tab.icon, size = 20.dp, tint = tint)
                if (selected) {
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = tint,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun Toast(message: String?, onGone: () -> Unit) {
    val colors = cleveColors()
    LaunchedEffect(message) {
        if (message != null) {
            delay(2400)
            onGone()
        }
    }
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = Modifier.align(Alignment.BottomCenter),
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfacePressed)
                .border(1.dp, colors.lime.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            Text(
                text = message.orEmpty(),
                style = CleveTypography.bodySmall,
                color = colors.textPrimary,
            )
        }
    }
}

@Composable
private fun CreatePlaylistDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val colors = cleveColors()
    var name by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.75f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(24.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Playlist baru",
                style = CleveTypography.headlineSmall,
                color = colors.textPrimary,
            )
            androidx.compose.foundation.text.BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = CleveTypography.bodyLarge.copy(color = colors.textPrimary),
                cursorBrush = Brush.verticalGradient(listOf(colors.lime, colors.limeDim)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                decorationBox = { inner ->
                    if (name.isEmpty()) {
                        Text(
                            text = "Nama playlist",
                            style = CleveTypography.bodyLarge,
                            color = colors.textTertiary,
                        )
                    }
                    inner()
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "Batal",
                    style = CleveTypography.labelLarge,
                    color = colors.textSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (name.isNotBlank()) colors.lime else colors.surfacePressed,
                        )
                        .clickable(enabled = name.isNotBlank()) { onConfirm(name.trim()) }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = "Buat",
                        style = CleveTypography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (name.isNotBlank()) colors.onLime else colors.textTertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackActionSheet(
    track: Track,
    playlists: List<com.cleve.music.data.local.PlaylistEntity>,
    onDismiss: () -> Unit,
    onAdd: (Long) -> Unit,
    onCreateNew: () -> Unit,
) {
    val colors = cleveColors()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.7f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(colors.surface)
                .border(
                    1.dp,
                    colors.border,
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                )
                .padding(20.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .systemBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TrackArtwork(url = track.thumbnailUrl, modifier = Modifier.size(52.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = CleveTypography.titleMedium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Text(
                        text = track.artist,
                        style = CleveTypography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                    )
                }
            }

            Spacer(Modifier.size(2.dp))
            Text(
                text = "Tambah ke playlist",
                style = CleveTypography.labelMedium,
                color = colors.textTertiary,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                playlists.forEach { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onAdd(playlist.id) }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        PhIcon(icon = Ph.Playlist, size = 18.dp, tint = colors.lime)
                        Text(
                            text = playlist.name,
                            style = CleveTypography.bodyLarge,
                            color = colors.textPrimary,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onCreateNew)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PhIcon(icon = Ph.Plus, size = 18.dp, tint = colors.lime)
                    Text(
                        text = "Playlist baru",
                        style = CleveTypography.bodyLarge,
                        color = colors.lime,
                    )
                }
            }
        }
    }
}
