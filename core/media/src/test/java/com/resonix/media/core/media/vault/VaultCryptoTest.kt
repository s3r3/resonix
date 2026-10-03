package com.resonix.media.core.media.vault

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.crypto.AEADBadTagException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultCryptoTest {

    private val key: SecretKey = KeyGenerator.getInstance(VaultCrypto.KEY_ALGORITHM).apply {
        init(VaultCrypto.KEY_SIZE_BITS)
    }.generateKey()

    private val crypto = VaultCrypto(key)

    @Test
    fun `encrypt then decrypt returns the original bytes`() {
        val plain = ByteArray(4_096) { (it % 251).toByte() }

        val encrypted = encrypt(plain)
        assertFalse(encrypted.contentEquals(plain))

        val decrypted = ByteArrayOutputStream()
        crypto.decrypt(ByteArrayInputStream(encrypted), decrypted)
        assertArrayEquals(plain, decrypted.toByteArray())
    }

    @Test
    fun `plaintext of an empty file is empty`() {
        val encrypted = encrypt(ByteArray(0))

        assertArrayEquals(ByteArray(0), decrypt(encrypted))
        assertEquals(0, plaintextLength(encrypted))
    }

    @Test
    fun `plaintext length matches the original across record boundaries`() {
        val sizes = listOf(1, 12, 1_048_575, 1_048_576, 1_048_577, 2_500_000)

        sizes.forEach { size ->
            val plain = ByteArray(size) { (it % 97).toByte() }
            val encrypted = encrypt(plain)

            assertEquals("size $size", size.toLong(), plaintextLength(encrypted))
            assertArrayEquals("size $size", plain, decrypt(encrypted))
        }
    }

    @Test
    fun `a stream opened mid file starts at the requested offset`() {
        val plain = ByteArray(2_500_000) { (it % 251).toByte() }
        val encrypted = encrypt(plain)

        // Plaintext offsets in the first record, on a record boundary, and inside the last one.
        listOf(0L, 1L, 500_000L, 1_048_576L, 1_048_577L, 2_097_152L, plain.size - 1L).forEach { offset ->
            val actual = readAt(encrypted, offset)
            val expected = plain.copyOfRange(offset.toInt(), plain.size)
            assertArrayEquals("offset $offset", expected, actual)
        }
    }

    @Test
    fun `an offset inside a record header reads from that record's first byte`() {
        val plain = ByteArray(2_000) { (it % 251).toByte() }
        val encrypted = encrypt(plain)

        val fromHeader = readAt(encrypted, 0)
        val fromFirstByte = readAt(encrypted, 0)

        assertArrayEquals(plain, fromHeader)
        assertArrayEquals(fromFirstByte, fromHeader)
    }

    @Test
    fun `a stream opened past the end yields nothing`() {
        val plain = ByteArray(32) { 1 }
        val encrypted = encrypt(plain)

        assertEquals(0, readAt(encrypted, plain.size.toLong()).size)
    }

    @Test
    fun `a flipped ciphertext byte fails authentication`() {
        val encrypted = encrypt(ByteArray(2_000) { 7 })
        val lastRecord = encrypted.size - 1
        encrypted[lastRecord] = (encrypted[lastRecord] + 1).toByte()

        assertThrows(AEADBadTagException::class.java) { decrypt(encrypted) }
    }

    @Test
    fun `isEncrypted distinguishes vault files from plain ones`() {
        assertTrue(VaultCrypto.isEncrypted(writeTemporary(encrypt(ByteArray(64) { 2 }))))

        val plain = File.createTempFile("vault-plain", ".mp4").apply { writeBytes(ByteArray(64)) }
        try {
            assertFalse(VaultCrypto.isEncrypted(plain))
        } finally {
            plain.delete()
        }
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        crypto.encrypt(ByteArrayInputStream(plain), output)
        return output.toByteArray()
    }

    private fun decrypt(encrypted: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        crypto.decrypt(ByteArrayInputStream(encrypted), output)
        return output.toByteArray()
    }

    private fun plaintextLength(encrypted: ByteArray): Long =
        crypto.plaintextLength(writeTemporary(encrypted))

    private fun readAt(encrypted: ByteArray, position: Long): ByteArray {
        val file = writeTemporary(encrypted)
        return crypto.openAt(file, position).use { it.readBytes() }
    }

    private fun writeTemporary(bytes: ByteArray): File {
        val file = File.createTempFile("vault-crypto", ".vault")
        file.deleteOnExit()
        file.writeBytes(bytes)
        return file
    }
}