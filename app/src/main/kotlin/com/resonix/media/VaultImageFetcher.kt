package com.resonix.media

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.resonix.media.core.media.vault.VaultCipher
import com.resonix.media.core.media.vault.VaultThumbnails
import com.resonix.media.core.media.vault.VaultUri
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath as toOkioPath

/**
 * Lets Coil show a frame from a vault video.
 *
 * Coil has no fetcher for the `resonix-vault` scheme, and it cannot read ciphertext anyway, so
 * this decrypts the file into [VaultThumbnails] and points the decoder at that copy. The copy is
 * plaintext, which is why it is discarded together with the vault file rather than left to the
 * cache to evict.
 */
class VaultImageFetcher(
    private val cipher: VaultCipher,
    private val context: Context,
    private val vaultFile: File,
) : Fetcher {

    override suspend fun fetch(): FetchResult = withContext(Dispatchers.IO) {
        val plaintext = VaultThumbnails.fileFor(context, vaultFile)
        cipher.decryptToFile(vaultFile, plaintext)

        SourceFetchResult(
            source = ImageSource(
                file = plaintext.absolutePath.toOkioPath(),
                fileSystem = FileSystem.SYSTEM,
                diskCacheKey = plaintext.absolutePath,
            ),
            mimeType = mimeTypeOf(vaultFile),
            dataSource = DataSource.DISK,
        )
    }

    class Factory(
        private val cipher: VaultCipher,
        private val context: Context,
    ) : Fetcher.Factory<Any> {
        override fun create(data: Any, options: Options, imageLoader: ImageLoader): VaultImageFetcher? {
            val uri = data as? Uri ?: return null
            val file = VaultUri.fileOf(uri) ?: return null
            return VaultImageFetcher(cipher, context, file)
        }
    }
}

private fun mimeTypeOf(file: File): String = MimeTypeMap.getSingleton()
    .getMimeTypeFromExtension(file.extension.lowercase())
    ?: "video/*"