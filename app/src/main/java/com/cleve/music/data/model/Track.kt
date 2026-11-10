package com.cleve.music.data.model

import androidx.compose.runtime.Immutable

/**
 * A track resolved from YouTube via NewPipe Extractor. Kept intentionally small so
 * it is cheap to serialize into the Room cache.
 */
@Immutable
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val durationSeconds: Long,
) {
    val durationLabel: String
        get() {
            val total = durationSeconds.coerceAtLeast(0)
            val m = total / 60
            val s = total % 60
            return if (m >= 60) {
                val h = m / 60
                "%d:%02d:%02d".format(h, m % 60, s)
            } else {
                "%d:%02d".format(m, s)
            }
        }
}

/** Result of an extractor lookup, including the resolved stream URL. */
@Immutable
data class ResolvedTrack(
    val track: Track,
    val streamUrl: String,
    val resolvedAtEpochMs: Long,
) {
    fun isFresh(nowMs: Long = System.currentTimeMillis()): Boolean =
        nowMs - resolvedAtEpochMs < TTL_MS

    companion object {
        /** Stream URLs from YouTube are short-lived; re-extract after this. */
        const val TTL_MS = 6L * 60L * 60L * 1000L
    }
}

enum class RepeatMode { Off, All, One }
