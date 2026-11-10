package com.cleve.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.cleve.music.ClevemusicApp
import com.cleve.music.data.local.PlaylistEntity
import com.cleve.music.data.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val query: String = "",
    val results: List<Track> = emptyList(),
    val searching: Boolean = false,
    val error: String? = null,
    val resolvingTrackId: String? = null,
    val toast: String? = null,
)

class MusicViewModel(private val app: ClevemusicApp) : ViewModel() {

    private val repo = app.repository
    private val player = app.playerHolder

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    val recent: StateFlow<List<Track>> = repo.recent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = repo.playlists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val nowPlaying = player.nowPlaying
    val isPlaying = player.isPlaying
    val queue = player.queue

    private var searchJob: Job? = null

    init {
        player.connect()
    }

    fun onQueryChange(value: String) {
        _state.value = _state.value.copy(query = value)
        searchJob?.cancel()
        if (value.isBlank()) {
            _state.value = _state.value.copy(results = emptyList(), searching = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(320) // debounce keystrokes
            runSearch(value)
        }
    }

    fun searchNow() {
        val q = _state.value.query
        if (q.isBlank()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runSearch(q) }
    }

    private suspend fun runSearch(query: String) {
        _state.value = _state.value.copy(searching = true, error = null)
        runCatching { repo.search(query) }
            .onSuccess { tracks ->
                _state.value = _state.value.copy(results = tracks, searching = false)
            }
            .onFailure { error ->
                _state.value = _state.value.copy(
                    searching = false,
                    error = error.message ?: "Pencarian gagal",
                )
            }
    }

    /** Resolves the stream URL then hands it to the player. */
    fun play(track: Track, queue: List<Track> = emptyList()) {
        if (_state.value.resolvingTrackId != null) return
        _state.value = _state.value.copy(resolvingTrackId = track.id, toast = null)
        viewModelScope.launch {
            val resolved = runCatching { repo.resolve(track) }.getOrNull()
            _state.value = _state.value.copy(resolvingTrackId = null)
            if (resolved == null) {
                _state.value = _state.value.copy(toast = "Gagal mengambil audio untuk lagu ini")
                return@launch
            }
            repo.markPlayed(track.id)
            player.playNow(resolved, queue.ifEmpty { listOf(track) })
        }
    }

    fun togglePlayPause() = player.togglePlayPause()
    fun next() = player.next()
    fun previous() = player.previous()
    fun seekTo(ms: Long) = player.seekTo(ms)

    /** Polled by the UI while the full player is open; no reactive player state needed. */
    fun positionMsCached(): Long = player.positionMs()
    fun durationMsCached(): Long = player.durationMs()

    private val _shuffle = MutableStateFlow(false)
    val shuffleState: StateFlow<Boolean> = _shuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatModeState: StateFlow<Int> = _repeatMode.asStateFlow()

    fun toggleShuffle() {
        val next = !_shuffle.value
        _shuffle.value = next
        player.setShuffle(next)
    }

    fun cycleRepeat() {
        val next = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = next
        player.setRepeatMode(next)
    }

    /** Flow of a playlist's tracks, resolved on demand. */
    fun playlistTracksState(playlistId: Long): StateFlow<List<Track>> {
        var cached = playlistCache[playlistId]
        if (cached == null) {
            cached = repo.playlistTracks(playlistId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
            playlistCache[playlistId] = cached
        }
        return cached
    }

    private val playlistCache = mutableMapOf<Long, StateFlow<List<Track>>>()

    fun addToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch {
            runCatching { repo.addToPlaylist(playlistId, track) }
                .onSuccess {
                    _state.value = _state.value.copy(toast = "Ditambahkan ke playlist")
                }
                .onFailure {
                    _state.value = _state.value.copy(toast = "Gagal menambahkan")
                }
        }
    }

    fun createPlaylist(name: String, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repo.createPlaylist(name)
            onDone(id)
        }
    }

    fun clearToast() {
        _state.value = _state.value.copy(toast = null)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
