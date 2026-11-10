package com.cleve.music.data.repo

import com.cleve.music.data.local.TrackEntity
import com.cleve.music.data.local.CleveDatabase
import com.cleve.music.data.local.PlaylistEntity
import com.cleve.music.data.local.PlaylistTrackEntity
import com.cleve.music.data.model.ResolvedTrack
import com.cleve.music.data.model.Track
import com.cleve.music.data.remote.YouTubeExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

class MusicRepository(
    private val db: CleveDatabase,
    private val extractor: YouTubeExtractor,
) {
    private val trackDao = db.tracks()
    private val playlistDao = db.playlists()

    /** Caps concurrent YouTube extraction so we stay polite and avoid rate limits. */
    private val gate = Semaphore(3)

    suspend fun search(query: String): List<Track> = withContext(Dispatchers.IO) {
        removeDeadStreamUrls()
        val results = extractor.search(query, limit = 30)
        cacheMetadata(results)
        results
    }

    /**
     * Returns a fresh, playable URL for a track. A cached URL is reused while it is
     * still inside its TTL; otherwise the extractor runs again and the cache updates.
     */
    suspend fun resolve(track: Track): ResolvedTrack? = withContext(Dispatchers.IO) {
        val cached = trackDao.getById(track.id)
        val now = System.currentTimeMillis()

        if (cached?.streamUrl != null && now - cached.resolvedAt < ResolvedTrack.TTL_MS) {
            return@withContext ResolvedTrack(
                track = cached.toTrack(),
                streamUrl = cached.streamUrl,
                resolvedAtEpochMs = cached.resolvedAt,
            )
        }

        val url = gate.withPermit { extractor.resolveStreamUrl(track.id) } ?: return@withContext null
        trackDao.upsert(
            TrackEntity(
                id = track.id,
                title = track.title,
                artist = track.artist,
                thumbnailUrl = track.thumbnailUrl,
                durationSeconds = track.durationSeconds,
                streamUrl = url,
                resolvedAt = now,
                lastPlayed = cached?.lastPlayed ?: 0L,
            ),
        )
        ResolvedTrack(track, url, now)
    }

    suspend fun related(trackId: String): List<Track> = withContext(Dispatchers.IO) {
        extractor.related(trackId).also { cacheMetadata(it) }
    }

    suspend fun markPlayed(trackId: String) = withContext(Dispatchers.IO) {
        trackDao.touch(trackId, System.currentTimeMillis())
    }

    fun recent(): Flow<List<Track>> = trackDao.recent(40).map { list -> list.map { it.toTrack() } }

    fun playlists(): Flow<List<PlaylistEntity>> = playlistDao.observeAll()

    fun playlistTracks(playlistId: Long): Flow<List<Track>> =
        playlistDao.tracksIn(playlistId).map { list -> list.map { it.toTrack() } }

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.create(PlaylistEntity(name = name, createdAt = System.currentTimeMillis()))
    }

    suspend fun addToPlaylist(playlistId: Long, track: Track) = withContext(Dispatchers.IO) {
        cacheMetadata(listOf(track))
        val position = playlistDao.nextPosition(playlistId)
        playlistDao.addTrack(PlaylistTrackEntity(playlistId, track.id, position))
    }

    suspend fun removeFromPlaylist(playlistId: Long, trackId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeTrack(playlistId, trackId)
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.delete(playlistId)
    }

    private suspend fun cacheMetadata(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        val now = System.currentTimeMillis()
        trackDao.upsertAll(
            tracks.map { track ->
                val existing = trackDao.getById(track.id)
                TrackEntity(
                    id = track.id,
                    title = track.title,
                    artist = track.artist,
                    thumbnailUrl = track.thumbnailUrl,
                    durationSeconds = track.durationSeconds,
                    streamUrl = existing?.streamUrl,
                    resolvedAt = existing?.resolvedAt ?: 0L,
                    lastPlayed = existing?.lastPlayed ?: 0L,
                )
            },
        )
        // Touch so freshly seen results also surface in "recent".
        tracks.forEach { trackDao.touch(it.id, now) }
    }

    /** Drops cached URLs past their TTL so a stale entry never plays silently. */
    private suspend fun removeDeadStreamUrls() {
        val now = System.currentTimeMillis()
        trackDao.recentOnce(200).forEach { entity ->
            val url = entity.streamUrl ?: return@forEach
            if (now - entity.resolvedAt >= ResolvedTrack.TTL_MS) {
                trackDao.upsert(entity.copy(streamUrl = null, resolvedAt = 0L))
                if (url.isBlank()) entity.copy(streamUrl = null)
            }
        }
    }

    private fun TrackEntity.toTrack() = Track(
        id = id,
        title = title,
        artist = artist,
        thumbnailUrl = thumbnailUrl,
        durationSeconds = durationSeconds,
    )
}
