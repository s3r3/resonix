package com.resonix.media.core.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.resonix.media.core.common.Logger
import com.resonix.media.core.common.Utils
import com.resonix.media.core.data.mappers.toAudioStreamInfo
import com.resonix.media.core.data.mappers.toSubtitleStreamInfo
import com.resonix.media.core.data.mappers.toVideoStreamInfo
import com.resonix.media.core.database.dao.HiddenVideoDao
import com.resonix.media.core.database.entities.HiddenVideoEntity
import com.resonix.media.core.media.services.MediaOperationsService
import com.resonix.media.core.media.vault.VaultCipher
import com.resonix.media.core.media.vault.VaultThumbnails
import com.resonix.media.core.media.vault.VaultUri
import com.resonix.media.core.model.MediaInfo
import com.resonix.media.core.model.Video
import io.github.anilbeesetti.nextlib.mediainfo.MediaInfoBuilder
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

/**
 * Hides videos in an encrypted vault.
 *
 * A hide is a copy-and-delete rather than a rename: the bytes are encrypted with [VaultCipher]
 * into the app's private storage and the original is removed from shared storage, so nothing that
 * can read the gallery — the system media scanner included — ever sees the file again. The order
 * matters. The vault record and the encrypted copy are written first and the source is only
 * deleted once both exist, so an interrupted hide leaves the video where it was instead of
 * destroying it. A copy that failed is removed again rather than kept as an orphan.
 *
 * The vault file keeps no real extension and is never handed out as a uri other apps can open;
 * [Video.uriString] is a [VaultUri], which only the app's own player data source can read.
 */
@Single
class LocalVaultRepository(
    private val hiddenVideoDao: HiddenVideoDao,
    private val mediaOperationsService: MediaOperationsService,
    private val vaultCipher: VaultCipher,
    private val context: Context,
) : VaultRepository {

    private val vaultMutationMutex = Mutex()
    private val pendingVaultPaths = MutableStateFlow<Set<String>>(emptySet())

    private val vaultDir: File by lazy {
        val base = context.getExternalFilesDir(null) ?: context.filesDir
        File(base, VAULT_DIR_NAME).apply { if (!exists()) mkdirs() }
    }

    override fun observeHiddenVideos(): Flow<List<Video>> {
        return combine(hiddenVideoDao.getAll(), pendingVaultPaths) { entities, pendingPaths ->
            entities
                .filter { it.vaultPath !in pendingPaths && File(it.vaultPath).exists() }
                .map { it.toVideo() }
        }
    }

    override suspend fun hideVideos(videos: List<Video>): Unit = vaultMutationMutex.withLock {
        val reservations = reserveVideos(videos)
        if (reservations.isEmpty()) return@withLock

        try {
            val unencrypted = encryptReservations(reservations)
            if (unencrypted.isNotEmpty()) {
                discardHides(unencrypted)
                return@withLock
            }

            val removed = removeSources(reservations)
            discardHides(reservations.filter { !removed && sourceStillExists(it) })
        } finally {
            revealReservations(reservations.map { it.destination.absolutePath })
        }
    }

    /**
     * Encrypts each source into its reserved vault file, returning the reservations that could not
     * be written. The source is only readable until its copy is complete, so a failure here leaves
     * the video untouched.
     */
    private suspend fun encryptReservations(
        reservations: List<HideReservation>,
    ): List<HideReservation> = withContext(Dispatchers.IO) {
        reservations.filter { reservation ->
            val input = runCatching {
                context.contentResolver.openInputStream(reservation.sourceUri)
            }.getOrNull()
            if (input == null) {
                logFailure("open ${reservation.sourceUri}", null)
                return@filter true
            }
            try {
                input.buffered().use { source ->
                    reservation.destination.outputStream().buffered().use { target ->
                        vaultCipher.encrypt(source, target)
                    }
                }
                false
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logFailure("encrypt ${reservation.sourceUri}", e)
                true
            }
        }
    }

    private suspend fun removeSources(reservations: List<HideReservation>): Boolean =
        try {
            mediaOperationsService.deleteMedia(reservations.map { it.sourceUri }, permanently = true)
        } catch (e: CancellationException) {
            // The encrypted copies and their rows stay: the vault keeps what it already committed,
            // exactly as an interrupted hide did when it moved files.
            throw e
        } catch (e: Exception) {
            logFailure("delete vaulted sources", e)
            false
        }

    /**
     * Whether a source is still readable after a failed delete. Deletion is reported per batch, so
     * a `false` can mean "nothing was removed" or "some were"; the ones that survived have to keep
     * their vault copy out of the vault, since the user would otherwise see the video in both
     * places and be unable to unhide it from the gallery.
     */
    private fun sourceStillExists(reservation: HideReservation): Boolean {
        if (File(reservation.originalPath).exists()) return true
        return runCatching {
            context.contentResolver.query(
                reservation.sourceUri,
                arrayOf(MediaStore.MediaColumns._ID),
                null,
                null,
                null,
            )?.use { it.moveToFirst() } == true
        }.getOrDefault(false)
    }

    /** Undoes a half-finished hide: the vault copy and its record both go away. */
    private suspend fun discardHides(reservations: List<HideReservation>) {
        if (reservations.isEmpty()) return
        withContext(NonCancellable) {
            withContext(Dispatchers.IO) {
                reservations.forEach { reservation ->
                    runCatching { reservation.destination.delete() }
                        .onFailure { logFailure("delete vault copy ${reservation.destination}", it) }
                }
            }
            runCatching { hiddenVideoDao.deleteByIds(reservations.map { it.rowId }) }
                .onFailure { logFailure("delete failed hide reservations", it) }
        }
    }

    private suspend fun reserveVideos(videos: List<Video>): List<HideReservation> {
        val attemptedVaultPaths = mutableListOf<String>()
        return try {
            videos.mapNotNull { video ->
                val destination = createVaultDestination(video.nameWithExtension)
                attemptedVaultPaths += destination.absolutePath
                pendingVaultPaths.update { it + destination.absolutePath }
                val rowId = try {
                    hiddenVideoDao.insert(video.toHiddenVideoEntity(destination))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    cleanUpReservations(listOf(destination.absolutePath))
                    return@mapNotNull null
                }
                HideReservation(
                    rowId = rowId,
                    sourceUri = video.uriString.toUri(),
                    originalPath = video.path,
                    displayName = video.nameWithExtension,
                    destination = destination,
                )
            }
        } catch (e: Exception) {
            cleanUpReservations(attemptedVaultPaths)
            throw e
        }
    }

    private fun Video.toHiddenVideoEntity(destination: File): HiddenVideoEntity {
        return HiddenVideoEntity(
            vaultPath = destination.absolutePath,
            originalPath = path,
            displayName = nameWithExtension,
            duration = duration,
            size = size,
            width = width,
            height = height,
            hiddenAt = System.currentTimeMillis(),
        )
    }

    private data class HideReservation(
        val rowId: Long,
        val sourceUri: Uri,
        val originalPath: String,
        val displayName: String,
        val destination: File,
    )

    /**
     * The vault file is a UUID, a `.vault` marker and the original extension — e.g.
     * `9f2c3d.vault.mp4`. The name says the bytes are ciphertext while the trailing extension is
     * what lets the media tools read a decrypted copy without being told what the file used to be.
     */
    private fun createVaultDestination(displayName: String): File {
        val extension = File(displayName).extension
            .lowercase()
            .filter(Char::isLetterOrDigit)
            .takeIf { it.isNotEmpty() }
            ?: DEFAULT_EXTENSION
        return generateSequence { File(vaultDir, "${UUID.randomUUID()}.vault.$extension") }
            .first { !it.exists() }
    }

    private suspend fun cleanUpReservations(vaultPaths: List<String>) {
        if (vaultPaths.isEmpty()) return
        withContext(NonCancellable) {
            runCatching { hiddenVideoDao.deleteByVaultPaths(vaultPaths) }
                .onFailure { logFailure("delete reservations by vault path", it) }
        }
        revealReservations(vaultPaths)
    }

    private fun revealReservations(vaultPaths: List<String>) {
        pendingVaultPaths.update { it - vaultPaths.toSet() }
    }

    private fun logFailure(operation: String, throwable: Throwable?) {
        val detail = throwable?.stackTraceToString().orEmpty()
        Logger.logError(TAG, "Failed to $operation: $detail")
    }

    override suspend fun unhideVideos(videos: List<Video>): UnhideResult = vaultMutationMutex.withLock {
        val restored = withContext(Dispatchers.IO) {
            entitiesFor(videos).mapNotNull { entity ->
                restoreToMediaStore(entity)?.let { relocated -> entity to relocated }
            }
        }
        if (restored.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                restored.forEach { (entity, _) ->
                    VaultThumbnails.discard(context, File(entity.vaultPath))
                }
            }
            hiddenVideoDao.deleteByIds(restored.map { (entity, _) -> entity.id })
        }
        UnhideResult(relocatedCount = restored.count { (_, relocated) -> relocated })
    }

    /**
     * Recreates the video in shared storage via a MediaStore insert, decrypting the vault file into
     * it, then deletes the vault copy. Returns `null` on failure, or whether the file had to be
     * relocated to the fallback directory (`true`) rather than its original folder (`false`).
     *
     * MediaStore is used for every API level because scoped storage (API 29+) forbids writing
     * directly into shared storage. On older versions the exact original path is restored via the
     * `DATA` column; on newer ones the folder is expressed as a `RELATIVE_PATH`.
     */
    private fun restoreToMediaStore(entity: HiddenVideoEntity): Boolean? {
        val vaultFile = File(entity.vaultPath)
        if (!vaultFile.exists()) return null

        val resolver = context.contentResolver
        val inserted = insertPendingItem(entity) ?: return null

        return try {
            resolver.openOutputStream(inserted.uri)?.use { output ->
                vaultFile.inputStream().use { input -> vaultCipher.decrypt(input, output) }
            } ?: error("Unable to open output stream for ${inserted.uri}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.update(
                    inserted.uri,
                    ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                    null,
                    null,
                )
            }
            vaultFile.delete()
            inserted.relocated
        } catch (e: Exception) {
            resolver.delete(inserted.uri, null, null) // roll back the partially-written entry
            null
        }
    }

    /**
     * Inserts an empty (pending) MediaStore row to receive the restored file.
     *
     * On API 29+ it tries the original folder first, then falls back to [DEFAULT_RESTORE_DIR] when
     * MediaStore rejects it — which happens for custom top-level folders (e.g. `ls/`) or files that
     * were at storage root, since scoped storage only permits the standard media directories.
     * Pre-scoped-storage, the exact original path is restored via the `DATA` column.
     */
    private fun insertPendingItem(entity: HiddenVideoEntity): InsertedItem? {
        val resolver = context.contentResolver
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(File(entity.displayName).extension.lowercase()) ?: "video/*"

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, entity.displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.DATA, entity.originalPath)
            }
            val uri = runCatching { resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) }.getOrNull()
            return uri?.let { InsertedItem(it, relocated = false) }
        }

        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val originalDir = relativePathFor(entity.originalPath)
        val candidateDirs = listOfNotNull(originalDir, DEFAULT_RESTORE_DIR).distinct()

        return candidateDirs.firstNotNullOfOrNull { relativePath ->
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, entity.displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            runCatching { resolver.insert(collection, values) }.getOrNull()
                ?.let { InsertedItem(it, relocated = relativePath != originalDir) }
        }
    }

    private data class InsertedItem(val uri: Uri, val relocated: Boolean)

    /**
     * The MediaStore `RELATIVE_PATH` (e.g. `DCIM/Camera/`) for [originalPath], or `null` when it
     * isn't under primary shared storage (root-level files, or a secondary volume).
     */
    private fun relativePathFor(originalPath: String): String? {
        val root = Environment.getExternalStorageDirectory().path
        val parent = File(originalPath).parent ?: return null
        if (!parent.startsWith(root)) return null
        return parent.removePrefix(root).trim('/').takeIf { it.isNotEmpty() }?.let { "$it/" }
    }

    override suspend fun deleteHiddenVideos(videos: List<Video>): Unit = vaultMutationMutex.withLock {
        val entities = entitiesFor(videos)
        if (entities.isEmpty()) return@withLock
        entities.forEach { entity ->
            File(entity.vaultPath).delete()
            VaultThumbnails.discard(context, File(entity.vaultPath))
        }
        hiddenVideoDao.deleteByIds(entities.map { it.id })
    }

    override suspend fun getHiddenVideoInfo(id: Long): MediaInfo? {
        val entity = hiddenVideoDao.getById(id) ?: return null
        val vaultFile = File(entity.vaultPath)
        if (!vaultFile.exists()) return null

        // Probing needs a seekable, unencrypted file, so it gets a decrypted copy that is removed
        // again as soon as the metadata has been read.
        val probeFile = VaultThumbnails.fileFor(context, vaultFile)
        val mediaInfo = try {
            vaultCipher.decryptToFile(vaultFile, probeFile)
            runCatching {
                MediaInfoBuilder().from(context = context, uri = probeFile.toUri()).build()
            }.getOrNull()
        } catch (e: Exception) {
            logFailure("probe ${entity.vaultPath}", e)
            null
        } finally {
            probeFile.delete()
        }

        return MediaInfo(
            video = entity.toVideo().copy(format = mediaInfo?.format),
            videoStream = mediaInfo?.videoStream?.toVideoStreamInfo(),
            audioStreams = mediaInfo?.audioStreams?.map { it.toAudioStreamInfo() } ?: emptyList(),
            subtitleStreams = mediaInfo?.subtitleStreams?.map { it.toSubtitleStreamInfo() } ?: emptyList(),
        ).also { mediaInfo?.release() }
    }

    /** Looks up the vault entities backing the given [videos] by their row id. */
    private suspend fun entitiesFor(videos: List<Video>): List<HiddenVideoEntity> {
        val ids = videos.map { it.id }.toSet()
        return hiddenVideoDao.getAll().first().filter { it.id in ids }
    }

    private fun HiddenVideoEntity.toVideo(): Video {
        return Video(
            id = id,
            path = vaultPath,
            parentPath = File(vaultPath).parent ?: "",
            duration = duration,
            uriString = VaultUri.build(File(vaultPath)).toString(),
            nameWithExtension = displayName,
            width = width,
            height = height,
            size = size,
            dateModified = hiddenAt,
            formattedDuration = Utils.formatDurationMillis(duration),
            formattedFileSize = Utils.formatFileSize(size),
        )
    }

    companion object {
        private const val TAG = "LocalVaultRepository"
        private const val VAULT_DIR_NAME = "vault"
        private const val DEFAULT_EXTENSION = "bin"

        /** Fallback folder for videos whose original location MediaStore won't accept. */
        private val DEFAULT_RESTORE_DIR = Environment.DIRECTORY_MOVIES + "/"
    }
}