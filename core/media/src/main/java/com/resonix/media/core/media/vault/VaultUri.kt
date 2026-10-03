package com.resonix.media.core.media.vault

import android.net.Uri
import java.io.File

/**
 * Playback URIs for vault media: `resonix-vault:///<absolute path>`, e.g.
 * `resonix-vault:///storage/emulated/0/Android/data/com.resonix.media/files/vault/9f2c.vault`.
 *
 * A vault file is encrypted, so nothing outside the app can open it — not the system gallery, not
 * a `content://` provider handed to another app, not the player's own default data sources. The
 * scheme exists so [VaultDataSource] can tell those apart: it is the only thing that turns those
 * bytes back into a video.
 */
object VaultUri {

    const val SCHEME = "resonix-vault"

    fun isVaultUri(uri: Uri): Boolean = uri.scheme?.lowercase() == SCHEME

    fun build(file: File): Uri = Uri.Builder()
        .scheme(SCHEME)
        .path(file.absolutePath)
        .build()

    /** The file this uri points at, or `null` when it is not a vault uri. */
    fun fileOf(uri: Uri): File? {
        if (!isVaultUri(uri)) return null
        return uri.path?.takeIf(String::isNotBlank)?.let(::File)
    }
}