package com.flowgroup.flowta.data.datasource.local.security

import android.content.Context
import com.flowgroup.flowta.di.qualifier.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun read(): PinHasher.Hashed? = withContext(ioDispatcher) {
        val file = pinFile()
        if (!file.exists()) return@withContext null
        DataInputStream(file.inputStream()).use { input ->
            val version = input.readInt()
            check(version == FORMAT_VERSION) { "Unknown PIN format version: $version" }
            val iterations = input.readInt()
            val saltSize = input.readInt()
            val salt = ByteArray(saltSize).also { input.readFully(it) }
            val hashSize = input.readInt()
            val hash = ByteArray(hashSize).also { input.readFully(it) }
            PinHasher.Hashed(hash = hash, salt = salt, iterations = iterations)
        }
    }

    suspend fun write(hashed: PinHasher.Hashed) = withContext(ioDispatcher) {
        val file = pinFile()
        file.parentFile?.mkdirs()
        DataOutputStream(file.outputStream()).use { output ->
            output.writeInt(FORMAT_VERSION)
            output.writeInt(hashed.iterations)
            output.writeInt(hashed.salt.size)
            output.write(hashed.salt)
            output.writeInt(hashed.hash.size)
            output.write(hashed.hash)
        }
    }

    suspend fun clear() = withContext(ioDispatcher) {
        pinFile().delete()
    }

    suspend fun exists(): Boolean = withContext(ioDispatcher) { pinFile().exists() }

    private fun pinFile(): File = File(File(context.filesDir, SECURITY_DIR), PIN_FILE_NAME)

    private companion object {
        const val FORMAT_VERSION = 1
        const val SECURITY_DIR = "security"
        const val PIN_FILE_NAME = "pin.bin"
    }
}