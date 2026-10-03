package com.resonix.media.core.media.vault

import java.io.EOFException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Chunked AES-256-GCM for vault media.
 *
 * A single GCM stream cannot be read from the middle: every byte depends on the nonce and on all
 * ciphertext before it, so a seek would mean decrypting the whole file again. Vault videos are
 * played, not just opened, and the player seeks constantly, so the file is instead a sequence of
 * independent records, each holding at most [CHUNK_PLAINTEXT_SIZE] bytes under its own nonce:
 *
 * ```
 * "RSV1" | record | record | ...
 * record := ivLength:1 | iv:12 | plainLength:4 | cipherText:(plainLength + 16)
 * ```
 *
 * Because every record but the last is exactly [RECORD_SIZE] bytes long, the record holding a
 * byte offset is `offset / RECORD_SIZE` — a seek is one read, not a scan. Each record is
 * authenticated on its own, so tampering with one chunk fails that chunk rather than the file.
 */
internal class VaultCrypto(private val secretKey: SecretKey) {

    fun encrypt(input: InputStream, output: OutputStream) {
        output.write(MAGIC)
        val buffer = ByteArray(CHUNK_PLAINTEXT_SIZE)
        while (true) {
            val filled = input.readAtMost(buffer)
            if (filled == 0) break
            writeRecord(output, buffer, filled)
            if (filled < buffer.size) break
        }
    }

    fun decrypt(input: InputStream, output: OutputStream) {
        readMagic(input)
        while (true) {
            val record = readRecord(input) ?: break
            output.write(record)
        }
    }

    /** Writes the plaintext of [source] into [destination], which is replaced. */
    fun decryptToFile(source: File, destination: File) {
        val parent = destination.absoluteFile.parentFile
        parent?.mkdirs()
        val temporary = File(parent, destination.name + TEMPORARY_SUFFIX)
        try {
            temporary.outputStream().buffered().use { output -> decrypt(source.inputStream().buffered(), output) }
            if (!temporary.renameTo(destination)) {
                temporary.copyTo(destination, overwrite = true)
                temporary.delete()
            }
        } catch (e: Exception) {
            temporary.delete()
            throw e
        }
    }

    fun plaintextLength(file: File): Long {
        val body = file.length() - MAGIC.size
        if (body < 0) throw IOException("Not a vault file: $file")
        val records = body / RECORD_SIZE.toLong()
        val tail = body % RECORD_SIZE
        val lastRecordLength = if (tail == 0L) 0L else tail - RECORD_HEADER_SIZE - TAG_LENGTH
        if (lastRecordLength < 0 || lastRecordLength > CHUNK_PLAINTEXT_SIZE) {
            throw IOException("Corrupt vault file: $file")
        }
        return records * CHUNK_PLAINTEXT_SIZE + lastRecordLength
    }

    /** A plaintext stream over [file] starting at plaintext [position]; supports seeking. */
    fun openAt(file: File, position: Long): InputStream {
        val length = plaintextLength(file)
        if (position < 0 || position > length) throw IOException("Position $position outside $file")
        return VaultPlaintextStream(
            file = file,
            crypto = this,
            recordIndex = position / CHUNK_PLAINTEXT_SIZE.toLong(),
            offsetInRecord = (position % CHUNK_PLAINTEXT_SIZE).toInt(),
        )
    }

    private fun writeRecord(output: OutputStream, plaintext: ByteArray, length: Int) {
        val iv = ByteArray(IV_LENGTH).also(SecureRandom()::nextBytes)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        }
        val cipherText = cipher.doFinal(plaintext, 0, length)

        output.write(IV_LENGTH)
        output.write(iv)
        output.write(length ushr 24)
        output.write(length ushr 16)
        output.write(length ushr 8)
        output.write(length)
        output.write(cipherText)
    }

    private fun readRecord(source: RandomAccessFile, index: Long): ByteArray? {
        val start = MAGIC.size + index * RECORD_SIZE
        if (start >= source.length()) return null
        source.seek(start)

        val ivLength = source.read()
        if (ivLength == -1) return null
        if (ivLength != IV_LENGTH) throw IOException("Corrupt vault record at $start")
        val iv = ByteArray(IV_LENGTH)
        source.readFully(iv)

        val plainLength = source.readInt()
        if (plainLength <= 0 || plainLength > CHUNK_PLAINTEXT_SIZE) {
            throw IOException("Corrupt vault record length $plainLength at $start")
        }
        val cipherText = ByteArray(plainLength + TAG_LENGTH)
        source.readFully(cipherText)

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        }
        return cipher.doFinal(cipherText)
    }

    private fun readRecord(source: InputStream): ByteArray? {
        val ivLength = source.read()
        if (ivLength == -1) return null
        if (ivLength != IV_LENGTH) throw IOException("Corrupt vault record")
        val iv = ByteArray(IV_LENGTH)
        source.readExactly(iv)

        val plainLength = readInt(source)
        if (plainLength <= 0 || plainLength > CHUNK_PLAINTEXT_SIZE) {
            throw IOException("Corrupt vault record length $plainLength")
        }
        val cipherText = ByteArray(plainLength + TAG_LENGTH)
        source.readExactly(cipherText)

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        }
        return cipher.doFinal(cipherText)
    }

    private fun readInt(source: InputStream): Int {
        val bytes = ByteArray(4)
        source.readExactly(bytes)
        return (bytes[0].toInt() and 0xFF shl 24) or
            (bytes[1].toInt() and 0xFF shl 16) or
            (bytes[2].toInt() and 0xFF shl 8) or
            (bytes[3].toInt() and 0xFF)
    }

    private fun readMagic(source: InputStream) {
        val magic = ByteArray(MAGIC.size)
        source.readExactly(magic)
        if (!magic.contentEquals(MAGIC)) throw IOException("Not a vault file")
    }

    /** Fills [destination] completely or throws; a short read means the file is truncated. */
    private fun InputStream.readExactly(destination: ByteArray) {
        var offset = 0
        while (offset < destination.size) {
            val read = read(destination, offset, destination.size - offset)
            if (read == -1) throw EOFException("Vault file ended after $offset of ${destination.size} bytes")
            offset += read
        }
    }

    private fun InputStream.readAtMost(buffer: ByteArray): Int {
        var filled = 0
        while (filled < buffer.size) {
            val read = read(buffer, filled, buffer.size - filled)
            if (read == -1) break
            filled += read
        }
        return filled
    }

    /**
     * Reads whole records off disk and hands out their plaintext, starting mid-record for
     * [offsetInRecord]. Only one record is held in memory at a time.
     */
    private class VaultPlaintextStream(
        file: File,
        private val crypto: VaultCrypto,
        recordIndex: Long,
        offsetInRecord: Int,
    ) : InputStream() {

        private val source = RandomAccessFile(file, "r")
        private var nextRecordIndex = recordIndex
        private var nextOffsetInRecord = offsetInRecord
        private var buffer = EMPTY
        private var bufferOffset = 0
        private var finished = false

        override fun read(): Int {
            while (bufferOffset >= buffer.size) {
                if (!loadNextRecord()) return -1
            }
            return buffer[bufferOffset++].toInt() and 0xFF
        }

        override fun read(destination: ByteArray, offset: Int, length: Int): Int {
            if (length == 0) return 0
            while (bufferOffset >= buffer.size) {
                if (!loadNextRecord()) return -1
            }
            val count = minOf(length, buffer.size - bufferOffset)
            System.arraycopy(buffer, bufferOffset, destination, offset, count)
            bufferOffset += count
            return count
        }

        override fun available(): Int = buffer.size - bufferOffset

        override fun close() {
            finished = true
            source.close()
        }

        private fun loadNextRecord(): Boolean {
            while (!finished) {
                val record = crypto.readRecord(source, nextRecordIndex)
                nextRecordIndex++
                if (record == null) {
                    finished = true
                    break
                }
                buffer = record
                bufferOffset = nextOffsetInRecord.coerceAtMost(record.size)
                nextOffsetInRecord = 0
                if (bufferOffset < record.size) return true
            }
            buffer = EMPTY
            bufferOffset = 0
            return false
        }
    }

    companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_ALGORITHM = "AES"
        const val KEY_SIZE_BITS = 256
        const val IV_LENGTH = 12
        const val TAG_LENGTH = 16
        const val CHUNK_PLAINTEXT_SIZE = 1024 * 1024

        val MAGIC = byteArrayOf('R'.code.toByte(), 'S'.code.toByte(), 'V'.code.toByte(), '1'.code.toByte())
        const val RECORD_HEADER_SIZE = 1 + IV_LENGTH + 4
        const val RECORD_SIZE = RECORD_HEADER_SIZE + CHUNK_PLAINTEXT_SIZE + TAG_LENGTH
        const val TEMPORARY_SUFFIX = ".part"

        private const val TAG_LENGTH_BITS = TAG_LENGTH * 8
        private val EMPTY = ByteArray(0)

        fun isEncrypted(file: File): Boolean {
            if (!file.isFile || file.length() <= MAGIC.size) return false
            return file.inputStream().use { input ->
                val magic = ByteArray(MAGIC.size)
                if (input.read(magic) != magic.size) return@use false
                magic.contentEquals(MAGIC)
            }
        }
    }
}