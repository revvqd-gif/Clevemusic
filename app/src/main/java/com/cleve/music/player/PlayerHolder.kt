package com.cleve.music.player

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.cleve.music.data.model.ResolvedTrack
import com.cleve.music.data.model.Track
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Thin, app-facing facade over the Media3 controller. Views observe [nowPlaying] and
 * [isPlaying] rather than touching MediaController directly, which keeps Compose
 * recomposition narrow.
 */
class PlayerHolder(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val _nowPlaying = MutableStateFlow<ResolvedTrack?>(null)
    val nowPlaying: StateFlow<ResolvedTrack?> = _nowPlaying.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val id = mediaItem?.mediaId ?: return
            _nowPlaying.value = _nowPlaying.value?.takeIf { it.track.id == id }
                ?: queue.value.firstOrNull { it.id == id }?.let { track ->
                    ResolvedTrack(track, mediaItem.localConfiguration?.uri.toString(), 0L)
                }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                _isPlaying.value = false
            }
        }
    }

    fun connect() {
        if (controller != null || controllerFuture != null) return
        ContextCompat.startForegroundService(
            context,
            android.content.Intent(context, PlaybackService::class.java),
        )

        val token = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java),
        )
        controllerFuture = MediaController.Builder(context, token).buildAsync().also { future ->
            future.addListener(
                {
                    controller = runCatching { future.get() }.getOrNull()
                    controller?.addListener(listener)
                    syncQueueFromController()
                },
                ContextCompat.getMainExecutor(context),
            )
        }
    }

    fun release() {
        controller?.removeListener(listener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        controllerFuture = null
    }

    /** Resolves the stream then enqueues it, replacing the queue for direct play. */
    fun playNow(resolved: ResolvedTrack, queue: List<Track> = emptyList()) {
        val c = controller ?: return
        val items = if (queue.isEmpty()) listOf(resolved.track) else queue
        _queue.value = items
        val mediaItems = items.mapIndexed { index, track ->
            buildMediaItem(
                if (track.id == resolved.track.id) resolved else null,
                track,
            )
        }
        c.setMediaItems(mediaItems, items.indexOfFirst { it.id == resolved.track.id }, 0L)
        c.prepare()
        c.play()
    }

    fun enqueue(resolved: ResolvedTrack) {
        val c = controller ?: return
        c.addMediaItem(buildMediaItem(resolved, resolved.track))
        c.prepare()
    }

    fun togglePlayPause() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun next() = controller?.seekToNextMediaItem() ?: Unit
    fun previous() = controller?.seekToPreviousMediaItem() ?: Unit

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    fun positionMs(): Long = controller?.currentPosition?.coerceAtLeast(0L) ?: 0L
    fun durationMs(): Long = controller?.duration?.takeIf { it > 0 } ?: 0L

    fun setRepeatMode(mode: Int) {
        controller?.repeatMode = mode
    }

    fun repeatMode(): Int = controller?.repeatMode ?: Player.REPEAT_MODE_OFF

    fun setShuffle(enabled: Boolean) {
        controller?.shuffleModeEnabled = enabled
    }

    fun isShuffle(): Boolean = controller?.shuffleModeEnabled ?: false

    private fun syncQueueFromController() {
        val c = controller ?: return
        val ids = (0 until c.mediaItemCount).map { c.getMediaItemAt(it).mediaId }
        _queue.value = ids.mapNotNull { id -> _queue.value.firstOrNull { it.id == id } }
    }

    private fun buildMediaItem(resolved: ResolvedTrack?, track: Track): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setArtworkUri(track.thumbnailUrl?.let { android.net.Uri.parse(it) })
            .build()

        return MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(resolved?.streamUrl)
            .setMediaMetadata(metadata)
            .build()
    }

    @Suppress("unused")
    private fun startConnectAsync() = scope.launch { withContext(Dispatchers.IO) { connect() } }
}
