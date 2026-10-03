package com.resonix.media.core.media.vault

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
import java.io.IOException
import java.io.InputStream

/**
 * Plays a vault file by decrypting it on the way to the decoder.
 *
 * Media3 reopens the data source at a new offset for every seek, so [open] may be called at any
 * position; the stream it hands back starts exactly there.
 */
@UnstableApi
class VaultDataSource(
    private val cipher: VaultCipher,
) : BaseDataSource(/* isNetwork = */ false) {

    private var uri: Uri? = null
    private var stream: InputStream? = null
    private var bytesRemaining = 0L
    private var opened = false

    override fun open(dataSpec: DataSpec): Long {
        transferInitializing(dataSpec)

        val file = VaultUri.fileOf(dataSpec.uri)
            ?: throw IOException("Not a vault uri: ${dataSpec.uri}")
        if (!file.isFile) throw IOException("Vault file is gone: $file")

        val plaintextLength = try {
            cipher.plaintextLength(file)
        } catch (e: IOException) {
            throw IOException("Cannot read vault file: ${dataSpec.uri}", e)
        }
        val position = dataSpec.position.coerceIn(0, plaintextLength)
        val available = plaintextLength - position
        val requested = dataSpec.length.takeIf { it != C.LENGTH_UNSET.toLong() } ?: available

        stream = try {
            cipher.openAt(file, position)
        } catch (e: Exception) {
            throw IOException("Cannot decrypt ${dataSpec.uri}", e)
        }
        bytesRemaining = minOf(requested, available)
        uri = dataSpec.uri
        opened = true
        transferStarted(dataSpec)
        return bytesRemaining
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT

        val readLength = minOf(length.toLong(), bytesRemaining).toInt()
        val bytesRead = stream?.read(buffer, offset, readLength) ?: return C.RESULT_END_OF_INPUT
        if (bytesRead == -1) return C.RESULT_END_OF_INPUT

        bytesRemaining -= bytesRead
        bytesTransferred(bytesRead)
        return bytesRead
    }

    override fun getUri(): Uri? = uri

    override fun close() {
        uri = null
        try {
            stream?.close()
        } finally {
            stream = null
            bytesRemaining = 0
            if (opened) {
                opened = false
                transferEnded()
            }
        }
    }
}