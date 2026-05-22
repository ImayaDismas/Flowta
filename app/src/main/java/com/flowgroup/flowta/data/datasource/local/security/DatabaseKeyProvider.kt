package com.flowgroup.flowta.data.datasource.local.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the SQLCipher database passphrase. The passphrase is 32 random bytes generated once on
 * first launch, wrapped with an AES/GCM key in the Android Keystore, and persisted to internal
 * storage. The Keystore-backed key never leaves secure hardware on devices that support it.
 */
@Singleton
class DatabaseKeyProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun obtainPassphrase(): ByteArray {
        val secretKey = getOrCreateMasterKey()
        val keyFile = passphraseFile()
        return if (keyFile.exists()) decryptPassphrase(secretKey, keyFile)
        else generateAndStorePassphrase(secretKey, keyFile)
    }

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            return keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
        }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(MASTER_KEY_SIZE_BITS)
            .setRandomizedEncryptionRequired(true)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    private fun generateAndStorePassphrase(secretKey: SecretKey, keyFile: File): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, secretKey) }
        val ciphertext = cipher.doFinal(passphrase)
        val iv = cipher.iv
        keyFile.parentFile?.mkdirs()
        keyFile.outputStream().use { out ->
            out.write(iv.size)
            out.write(iv)
            out.write(ciphertext)
        }
        return passphrase
    }

    private fun decryptPassphrase(secretKey: SecretKey, keyFile: File): ByteArray {
        keyFile.inputStream().use { input ->
            val ivSize = input.read()
            check(ivSize > 0) { "Corrupt passphrase file: missing IV" }
            val iv = ByteArray(ivSize)
            check(input.read(iv) == ivSize) { "Corrupt passphrase file: short IV" }
            val ciphertext = input.readBytes()
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            }
            return cipher.doFinal(ciphertext)
        }
    }

    private fun passphraseFile(): File =
        File(File(context.filesDir, SECURITY_DIR), PASSPHRASE_FILE_NAME)

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val MASTER_KEY_ALIAS = "flowta_db_master_key"
        const val MASTER_KEY_SIZE_BITS = 256
        const val PASSPHRASE_LENGTH_BYTES = 32
        const val GCM_TAG_LENGTH_BITS = 128
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val SECURITY_DIR = "security"
        const val PASSPHRASE_FILE_NAME = "db_passphrase.enc"
    }
}
