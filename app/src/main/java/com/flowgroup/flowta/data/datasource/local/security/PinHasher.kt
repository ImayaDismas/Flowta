package com.flowgroup.flowta.data.datasource.local.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

/**
 * PBKDF2-HMAC-SHA256 PIN hasher. 100k iterations, 256-bit output, 16-byte random salt.
 * Constant-time comparison on verify to mitigate timing attacks.
 */
class PinHasher @Inject constructor() {

    data class Hashed(val hash: ByteArray, val salt: ByteArray, val iterations: Int) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Hashed) return false
            return hash.contentEquals(other.hash) &&
                salt.contentEquals(other.salt) &&
                iterations == other.iterations
        }

        override fun hashCode(): Int {
            var result = hash.contentHashCode()
            result = 31 * result + salt.contentHashCode()
            result = 31 * result + iterations
            return result
        }
    }

    fun hash(pin: CharArray, salt: ByteArray = generateSalt(), iterations: Int = ITERATIONS): Hashed {
        val spec = PBEKeySpec(pin, salt, iterations, KEY_LENGTH_BITS)
        try {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            val hash = factory.generateSecret(spec).encoded
            return Hashed(hash = hash, salt = salt, iterations = iterations)
        } finally {
            spec.clearPassword()
        }
    }

    fun verify(pin: CharArray, expected: Hashed): Boolean {
        val computed = hash(pin, expected.salt, expected.iterations).hash
        return constantTimeEquals(computed, expected.hash)
    }

    private fun generateSalt(): ByteArray =
        ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) result = result or (a[i].toInt() xor b[i].toInt())
        return result == 0
    }

    private companion object {
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val ITERATIONS = 100_000
        const val SALT_LENGTH_BYTES = 16
        const val KEY_LENGTH_BITS = 256
    }
}
