package com.resonix.media.core.media.network.opensubtitles

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private const val HOST = "https://api.opensubtitles.com/api/v1"
private const val USER_AGENT = "Resonix"
private const val COOLDOWN_MILLIS = 30_000L

/**
 * OpenSubtitles REST v1 client.
 *
 * Every request needs an `Api-Key` header (free at opensubtitles.com/en/api).
 * Search works anonymously, but downloads additionally need the user token as
 * a Bearer header. HttpURLConnection keeps the module free of extra deps.
 */
class OpenSubtitlesApi {

    /** Search subtitles. `languages` is an OpenSubtitles tag such as `eng`. */
    suspend fun search(
        apiKey: String,
        query: String,
        languages: String,
        token: String? = null,
        imdbId: Int? = null,
        year: Int? = null,
        season: Int? = null,
        episode: Int? = null,
    ): List<OpenSubtitlesSearchResult> = withContext(Dispatchers.IO) {
        val response = request(
            method = "GET",
            path = searchPath(query, languages, imdbId, year, season, episode),
            apiKey = apiKey,
            token = token,
            body = null,
        )
        parseSearchResponse(response)
    }

    /** Request a time-limited download link for a subtitle file. */
    suspend fun downloadLink(
        apiKey: String,
        token: String,
        fileId: Long,
    ): OpenSubtitlesDownloadLink = withContext(Dispatchers.IO) {
        val response = request(
            method = "POST",
            path = "/download",
            apiKey = apiKey,
            token = token,
            body = """{"file_id":$fileId}""",
        )
        jsonFormat.decodeFromString(OpenSubtitlesDownloadLink.serializer(), response)
    }

    internal fun searchPath(
        query: String,
        languages: String,
        imdbId: Int?,
        year: Int?,
        season: Int?,
        episode: Int?,
    ): String {
        val params = StringBuilder("query=")
            .append(URLEncoder.encode(query, "UTF-8"))
            .append("&languages=")
            .append(URLEncoder.encode(languages, "UTF-8"))
        imdbId?.let { params.append("&imdb_id=").append(it) }
        year?.let { params.append("&year=").append(it) }
        season?.let { params.append("&season_number=").append(it) }
        episode?.let { params.append("&episode_number=").append(it) }
        return "/subtitles?$params"
    }

    internal fun parseSearchResponse(json: String): List<OpenSubtitlesSearchResult> {
        return jsonFormat.decodeFromString(OpenSubtitlesSearchResponse.serializer(), json).data
    }

    private fun request(
        method: String,
        path: String,
        apiKey: String,
        token: String?,
        body: String?,
    ): String {
        val now = System.currentTimeMillis()
        if (now < cooldownUntil) {
            throw OpenSubtitlesException.RateLimited(cooldownUntil - now)
        }
        val connection = URL(HOST + path).openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = method
            setRequestProperty("Api-Key", apiKey)
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Content-Type", "application/json")
            token?.let { setRequestProperty("Authorization", "Bearer $it") }
            if (body != null) {
                doOutput = true
                outputStream.use { it.write(body.toByteArray()) }
            }
        }
        val responseCode = connection.responseCode
        when (responseCode) {
            401, 403 -> throw OpenSubtitlesException.Authentication
            429 -> {
                cooldownUntil = now + COOLDOWN_MILLIS
                throw OpenSubtitlesException.RateLimited(COOLDOWN_MILLIS)
            }
            else -> if (responseCode !in 200..299) {
                val error = connection.errorStream?.readBytes()?.decodeToString()?.take(200).orEmpty()
                throw OpenSubtitlesException.Network("HTTP $responseCode: $error")
            }
        }
        return connection.inputStream.readBytes().decodeToString()
    }

    companion object {
        private val jsonFormat = Json { ignoreUnknownKeys = true }

        @Volatile
        private var cooldownUntil = 0L
    }
}

@Serializable
data class OpenSubtitlesSearchResponse(
    val data: List<OpenSubtitlesSearchResult> = emptyList(),
)

@Serializable
data class OpenSubtitlesSearchResult(
    val id: String = "",
    val type: String = "",
    val attributes: OpenSubtitlesAttributes = OpenSubtitlesAttributes(),
) {
    /** Best display name: subtitle file name, else the feature it belongs to. */
    val displayName: String
        get() {
            val attr = attributes
            return attr.files.firstOrNull()?.fileName?.takeIf { it.isNotBlank() }
                ?: attr.featureDetails.movieName?.takeIf { it.isNotBlank() }
                ?: attr.featureDetails.title?.takeIf { it.isNotBlank() }
                ?: attr.release?.takeIf { it.isNotBlank() }
                ?: attr.featureDetails.parentTitle
                ?: "subtitle"
        }
}

@Serializable
data class OpenSubtitlesAttributes(
    @SerialName("subtitle_id") val subtitleId: String = "",
    val language: String = "",
    val release: String? = null,
    val files: List<OpenSubtitlesFile> = emptyList(),
    @SerialName("feature_details") val featureDetails: OpenSubtitlesFeatureDetails = OpenSubtitlesFeatureDetails(),
    @SerialName("hearing_impaired") val hearingImpaired: Boolean = false,
)

@Serializable
data class OpenSubtitlesFile(
    @SerialName("file_id") val fileId: Long = 0L,
    @SerialName("file_name") val fileName: String? = null,
)

@Serializable
data class OpenSubtitlesFeatureDetails(
    val year: Int? = null,
    val title: String? = null,
    @SerialName("movie_name") val movieName: String? = null,
    @SerialName("imdb_id") val imdbId: Int? = null,
    @SerialName("season_number") val seasonNumber: Int? = null,
    @SerialName("episode_number") val episodeNumber: Int? = null,
    @SerialName("parent_title") val parentTitle: String? = null,
)

@Serializable
data class OpenSubtitlesDownloadLink(
    val link: String = "",
    @SerialName("file_name") val fileName: String? = null,
)

sealed class OpenSubtitlesException(message: String) : Exception(message) {
    data object MissingApiKey : OpenSubtitlesException("OpenSubtitles API key is not set")

    data object MissingToken : OpenSubtitlesException("OpenSubtitles user token is not set")

    data object Authentication : OpenSubtitlesException("Invalid OpenSubtitles API key or token")

    class RateLimited(val retryInMillis: Long) : OpenSubtitlesException("Too many requests")

    class Network(message: String) : OpenSubtitlesException(message)
}
