package com.resonix.media.core.media.vault

import android.content.Context
import java.io.File

/**
 * Where a vault video is decrypted for the tools that cannot read ciphertext.
 *
 * `MediaMetadataRetriever` and `MediaThumbnailRetriever` work off a seekable file descriptor, so a
 * frame can only be pulled from a decrypted copy on disk. Those copies are plaintext, so they
 * live in the cache directory and are deleted together with the vault file that produced them —
 * a decrypted frame must not outlive the video it came from.
 */
object VaultThumbnails {

    private const val DIRECTORY_NAME = "vault-plaintext"

    fun directory(context: Context): File =
        File(context.cacheDir, DIRECTORY_NAME).apply { if (!exists()) mkdirs() }

    /**
     * The plaintext copy of [vaultFile], keeping its name — and therefore the extension the
     * retriever picks its demuxer from.
     */
    fun fileFor(context: Context, vaultFile: File): File =
        File(directory(context), vaultFile.name)

    fun discard(context: Context, vaultFile: File) {
        fileFor(context, vaultFile).delete()
    }
}