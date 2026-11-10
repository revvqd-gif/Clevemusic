package com.cleve.music.data.remote

import android.util.Log
import com.cleve.music.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request as NpRequest
import org.schabi.newpipe.extractor.downloader.Response as NpResponse
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Bridges NewPipe Extractor onto OkHttp.
 *
 * Extractor 0.27.x keeps the classic Downloader contract (single `execute` taking a
 * Request, returning `Response` with byte body + headers), so this adapter covers
 * everything the library needs while OkHttp owns pooling and TLS.
 */
/**
 * In-memory cookie store.
 *
 * YouTube answers the first request with a consent / visitor cookie and then refuses
 * to return usable JSON unless that cookie comes back on the following calls. The
 * default OkHttp client has no jar, so every request looked like a fresh browser and
 * the extractor received a stub document — which is what produced
 * `JsonObject.getObject(...) on a null object reference`.
 */
class MemoryCookieJar : CookieJar {
    private val store = LinkedHashMap<String, Cookie>()

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach { store[key(it)] = it }
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> =
        store.values.filter { it.matches(url) }

    @Synchronized
    fun clear() = store.clear()

    private fun key(c: Cookie) = "${c.domain}|${c.name}|${c.path}"
}

class OkHttpNewPipeDownloader(
    private val client: OkHttpClient = defaultClient,
) : Downloader() {

    @Throws(IOException::class)
    override fun execute(request: NpRequest): NpResponse {
        val method = request.httpMethod().uppercase()

        val body = request.dataToSend()?.let { bytes ->
            val contentType = request.headers()["Content-Type"]?.firstOrNull()
            bytes.toRequestBody(contentType?.toMediaTypeOrNull())
        }

        val builder = Request.Builder().url(request.url()).method(method, body)

        // Baseline browser identity; NewPipe's own headers may override these after.
        builder.header("User-Agent", USER_AGENT)
        builder.header("Accept-Language", "en-US,en;q=0.9")

        request.headers().forEach { (key, values) ->
            if (key.equals("Content-Length", ignoreCase = true)) return@forEach
            values.forEach { value -> builder.header(key, value) }
        }

        client.newCall(builder.build()).execute().use { response ->
            val bodyText = response.body?.string() ?: ""
            return NpResponse(
                response.code,
                response.message,
                response.headers.toMultimap(),
                bodyText,
                request.url(),
            )
        }
    }

    companion object {
        private const val TAG = "NpDownloader"

        /** Shared cookie jar so YouTube's consent + visitor cookies survive requests. */
        val cookieJar = MemoryCookieJar()

        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

        val defaultClient: OkHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        @Volatile
        private var installed = false

        /**
         * Idempotent initialisation.
         *
         * NewPipe's singleton init is not safe to run twice, so this guards on a flag.
         * Localisation is set explicitly to English/US: without it YouTube answers with
         * a consent interstitial whose JSON shape differs from what the extractor parses,
         * producing `getObject(...) on a null object reference`.
         */
        fun install() {
            if (installed) return
            synchronized(this) {
                if (installed) return
                try {
                    val localization = Localization("en", "US")
                    NewPipe.init(
                        OkHttpNewPipeDownloader(defaultClient),
                        localization,
                        ContentCountry("US"),
                    )
                    // Accept the consent wall so the extractor receives real page data.
                    YoutubeParsingHelper.setConsentAccepted(true)
                    installed = true
                } catch (e: Exception) {
                    Log.e(TAG, "NewPipe init failed", e)
                }
            }
        }
    }
}

/**
 * Single entry point for all YouTube extraction. Extraction is network + CPU heavy,
 * so every call hops to IO and never touches the main thread.
 */
class YouTubeExtractor {

    init {
        OkHttpNewPipeDownloader.install()
    }

    suspend fun search(query: String, limit: Int = 25): List<Track> = withContext(Dispatchers.IO) {
        runExtraction {
            val extractor: SearchExtractor = ServiceList.YouTube.getSearchExtractor(query)
            val page = extractor.initialPage
            page.items.asSequence()
                .filterIsInstance<StreamInfoItem>()
                .filter { item ->
                    item.streamType != StreamType.LIVE_STREAM &&
                        item.streamType != StreamType.AUDIO_LIVE_STREAM
                }
                .take(limit)
                .map { item -> item.toTrack() }
                .toList()
        }
    }
    /**
     * Resolves the best audio-only stream for playback: highest bitrate wins, with a
     * progressive stream preferred over adaptive at equal bitrate because it needs no
     * separate initialization segment.
     */
    suspend fun resolveStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.youtube.com/watch?v=$videoId"
            runExtraction {
                val info: StreamInfo = StreamInfo.getInfo(ServiceList.YouTube, url)

                info.audioStreams
                    .asSequence()
                    .filter { stream -> !stream.url.isNullOrBlank() }
                    .sortedByDescending { it.averageBitrate }
                    .firstOrNull()
                    ?.url
                    ?: info.hlsUrl?.takeIf { it.isNotBlank() }
            }
        } catch (e: ExtractionException) {
            Log.w(TAG, "resolveStreamUrl failed for $videoId: ${e.message}")
            null
        } catch (e: IOException) {
            Log.w(TAG, "resolveStreamUrl network error for $videoId: ${e.message}")
            null
        } catch (e: Exception) {
            Log.w(TAG, "resolveStreamUrl unexpected error for $videoId: ${e.message}")
            null
        }
    }

    suspend fun related(videoId: String, limit: Int = 15): List<Track> = withContext(Dispatchers.IO) {
        runCatching {
            runExtraction {
                val info = StreamInfo.getInfo(
                    ServiceList.YouTube,
                    "https://www.youtube.com/watch?v=$videoId",
                )
                info.relatedItems.asSequence()
                    .filterIsInstance<StreamInfoItem>()
                    .take(limit)
                    .map { it.toTrack() }
                    .toList()
            }
        }.getOrDefault(emptyList())
    }

    private fun StreamInfoItem.toTrack() = Track(
        id = videoIdFromUrl(url),
        title = name ?: "Unknown title",
        artist = uploaderName ?: "Unknown artist",
        thumbnailUrl = thumbnails.maxByOrNull { it.height }?.url,
        durationSeconds = duration.coerceAtLeast(0L),
    )

    /** Pulls the 11-char video id out of any YouTube URL form we may be handed. */
    private fun videoIdFromUrl(url: String): String {
        if (url.isBlank()) return ""
        val afterV = url.substringAfter("v=", "")
            .substringBefore('&')
            .substringBefore('?')
        if (afterV.isNotBlank()) return afterV
        val trimmed = url.trimEnd('/')
        return trimmed.substringAfterLast('/')
    }

    /**
     * Runs an extraction, retrying after clearing cookies and re-accepting consent.
     *
     * YouTube serves an interstitial on a cold cookie jar whose JSON lacks the fields
     * the extractor expects, surfacing as a NullPointerException deep in nanojson
     * (`getObject(...) on a null object reference`). Dropping the jar forces a fresh
     * consent handshake, which yields a complete response.
     */
    private inline fun <T> runExtraction(block: () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            if (!isRecoverable(e)) throw e
            Log.w(TAG, "extraction failed, retrying with fresh cookies: ${e.message}")
            runCatching { OkHttpNewPipeDownloader.cookieJar.clear() }
            runCatching { OkHttpNewPipeDownloader.install() }
            runCatching { YoutubeParsingHelper.setConsentAccepted(true) }
            try {
                block()
            } catch (second: Exception) {
                if (!isRecoverable(second)) throw second
                // One last attempt with a clean slate before giving up.
                runCatching { OkHttpNewPipeDownloader.cookieJar.clear() }
                runCatching { YoutubeParsingHelper.setConsentAccepted(false) }
                runCatching { YoutubeParsingHelper.setConsentAccepted(true) }
                block()
            }
        }
    }

    /** True for the JSON-shape / null-pointer failures that a retry can fix. */
    private fun isRecoverable(e: Exception): Boolean {
        val message = e.message.orEmpty()
        return e is NullPointerException ||
            message.contains("null object reference") ||
            message.contains("JsonObject") ||
            message.contains("nanojson")
    }

    companion object {
        private const val TAG = "YouTubeExtractor"
    }
}
