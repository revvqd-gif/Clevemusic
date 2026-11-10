package com.cleve.music.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cleve.music.data.model.Track
import com.cleve.music.ui.common.component.Capsule
import com.cleve.music.ui.common.component.EmptyState
import com.cleve.music.ui.common.component.SearchField
import com.cleve.music.ui.common.component.SkeletonRow
import com.cleve.music.ui.common.component.TrackRow
import com.cleve.music.ui.common.icon.Ph
import com.cleve.music.ui.common.theme.cleveColors

private val suggestions = listOf(
    "Lo-fi chill", "Indie Indonesia", "Jazz malam", "K-pop hits", "Rock klasik", "Acoustic cover",
)

@Composable
fun SearchScreen(
    query: String,
    results: List<Track>,
    searching: Boolean,
    error: String?,
    nowPlaying: Track?,
    isResolving: (String) -> Boolean,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onPlay: (Track, List<Track>) -> Unit,
    onMore: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = cleveColors()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Cari",
                style = MaterialTheme.typography.displaySmall,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            SearchField(
                value = query,
                onValueChange = onQueryChange,
                onSubmit = onSubmit,
            )
            Spacer(Modifier.height(14.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            if (query.isBlank()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Coba ini",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textTertiary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(10.dp))
                        SuggestionWrap(onPick = onQueryChange)
                    }
                }
            }

            if (searching) {
                items(6) { index ->
                    SkeletonRow(modifier = Modifier.padding(horizontal = 10.dp))
                }
            } else if (results.isEmpty() && query.isNotBlank() && error == null) {
                item {
                    EmptyState(
                        icon = Ph.MagnifyingGlass,
                        title = "Tidak ada hasil",
                        subtitle = "Coba kata kunci lain atau periksa koneksi.",
                    )
                }
            } else if (error != null) {
                item {
                    EmptyState(
                        icon = Ph.ArrowClockwiseAlt,
                        title = "Gagal mencari",
                        subtitle = error,
                    )
                }
            } else if (results.isNotEmpty()) {
                item {
                    Text(
                        text = "${results.size} hasil untuk \"$query\"",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    )
                }
                items(results, key = { it.id }) { track ->
                    TrackRow(
                        track = track,
                        onClick = { onPlay(track, results) },
                        onMore = { onMore(track) },
                        isActive = nowPlaying?.id == track.id,
                        isResolving = isResolving(track.id),
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionWrap(onPick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        suggestions.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                pair.forEach { s ->
                    Capsule(text = s, onClick = { onPick(s) }, modifier = Modifier.weight(1f))
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
