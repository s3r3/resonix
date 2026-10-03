package com.resonix.media.core.data.repository

import android.content.Context
import android.net.Uri
import com.resonix.media.core.media.network.opensubtitles.OpenSubtitlesApi
import com.resonix.media.core.media.network.opensubtitles.OpenSubtitlesException
import com.resonix.media.core.media.network.opensubtitles.OpenSubtitlesDownloadLink
import com.resonix.media.core.media.network.opensubtitles.OpenSubtitlesSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.io.File
import java.net.URL
import java.util.zip.ZipInputStream

/**
 * Searches and downloads subtitles from OpenSubtitles.
 *
 * Downloaded files land in the app-private `subtitles/` directory, so no
 * storage permission is needed. The returned `file://` [Uri] can be passed
 * straight to `MediaController.addSubtitleTrack`.
 */
@Single
class OpenSubtitlesRepository(
    private val context: Context,
    private val preferencesRepository: PreferencesRepository,
) {
    private val api = OpenSubtitlesApi()

    suspend fun search(query: String, language: String): List<OpenSubtitlesSearchResult> {
        val preferences = preferencesRepository.playerPreferences.value
        val apiKey = preferences.openSubtitlesApiKey
        if (apiKey.isBlank()) throw OpenSubtitlesException.MissingApiKey
        return api.search(
            apiKey = apiKey,
            query = query,
            languages = language.ifBlank { DEFAULT_LANGUAGE },
            token = preferences.openSubtitlesToken.takeIf { it.isNotBlank() },
        )
    }

    /** Downloads a subtitle and returns a `file://` [Uri] ready to attach to the player. */
    suspend fun download(result: OpenSubtitlesSearchResult): Uri = withContext(Dispatchers.IO) {
        val preferences = preferencesRepository.playerPreferences.value
        val apiKey = preferences.openSubtitlesApiKey
        if (apiKey.isBlank()) throw OpenSubtitlesException.MissingApiKey
        val token = preferences.openSubtitlesToken
        if (token.isBlank()) throw OpenSubtitlesException.MissingToken
        val fileId = result.attributes.files.firstOrNull()?.fileId
            ?: throw OpenSubtitlesException.Network("Subtitle has no downloadable file")

        val link = api.downloadLink(apiKey = apiKey, token = token, fileId = fileId)
        val bytes = URL(link.link).openConnection().getInputStream().use { it.readBytes() }
        val content = unzipFirstSubtitle(bytes) ?: bytes
        val file = File(context.filesDir, SUBTITLES_DIR).resolve(subtitleFileName(link, result))
        file.parentFile?.mkdirs()
        file.writeBytes(content)
        Uri.fromFile(file)
    }

    private fun subtitleFileName(
        link: OpenSubtitlesDownloadLink,
        result: OpenSubtitlesSearchResult,
    ): String {
        val raw = link.fileName?.takeIf { it.isNotBlank() } ?: result.displayName
        val sanitized = raw.replace(Regex("[/\\\\:]"), "_").takeIf { it.isNotBlank() } ?: "subtitle"
        val extension = sanitized.substringAfterLast('.', "").lowercase()
        return if (extension in SUBTITLE_EXTENSIONS) sanitized else "$sanitized.srt"
    }

    /** Some downloads are zipped when a result has multiple formats; unpack the first subtitle. */
    private fun unzipFirstSubtitle(bytes: ByteArray): ByteArray? {
        if (bytes.size < 2 || bytes[0].toInt() != 0x50 || bytes[1].toInt() != 0x4B) return null
        ZipInputStream(bytes.inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val extension = entry.name.substringAfterLast('.', "").lowercase()
                if (!entry.isDirectory && extension in SUBTITLE_EXTENSIONS) {
                    return zip.readBytes()
                }
                zip.closeEntry()
            }
        }
        return null
    }

    companion object {
        private const val SUBTITLES_DIR = "subtitles"
        private const val DEFAULT_LANGUAGE = "eng"
        private val SUBTITLE_EXTENSIONS = setOf("srt", "vtt", "ass", "ssa", "ttml", "xml", "sub")
    }
}
