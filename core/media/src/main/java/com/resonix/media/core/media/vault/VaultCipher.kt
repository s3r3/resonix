package com.resonix.media.core.media.vault

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import org.koin.core.annotation.Single

/**
 * Vault encryption: AES-256-GCM with a key that lives in the Android Keystore and never leaves
 * the device. Losing the key — a factory reset, a restored backup — makes the vault unreadable,
 * which is the point: the bytes on disk are worthless without it.
 *
 * The record layout and the reasoning behind chunking live in [VaultCrypto]; this class only owns
 * the key.
 */
@Single
class VaultCipher {

    private val crypto by lazy { VaultCrypto(loadOrCreateKey()) }

    fun encrypt(input: InputStream, output: OutputStream) = crypto.encrypt(input, output)

    fun decrypt(input: InputStream, output: OutputStream) = crypto.decrypt(input, output)

    fun decryptToFile(source: File, destination: File) = crypto.decryptToFile(source, destination)

    /** Total plaintext length of [file], without reading it. */
    fun plaintextLength(file: File): Long = crypto.plaintextLength(file)

    /** A plaintext stream over [file] starting at [position]; supports seeking. */
    fun openAt(file: File, position: Long): InputStream = crypto.openAt(file, position)

    fun isEncrypted(file: File): Boolean = VaultCrypto.isEncrypted(file)

    private fun loadOrCreateKey(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(
            VaultCrypto.KEY_ALGORITHM,
            ANDROID_KEYSTORE,
        )
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(VaultCrypto.KEY_SIZE_BITS)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "resonix_vault_aes_gcm"
    }
}