package com.mohammed.notes.feature.core.security

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * The cryptographic primitives behind the hidden-notes PIN. Everything lives in the JDK
 * (`javax.crypto`), so the exact same code runs the host unit tests and the app.
 *
 * All secrets here are *malformed only as raw bytes*; callers are responsible for scoping
 * them to an account and for persisting them in places that never leave the device.
 */
object PinCrypto {

    /** Hidden notes are protected by exactly four digits. */
    const val PIN_LENGTH = 4

    /**
     * OWASP's 2023 guidance for PBKDF2-HMAC-SHA256. Expensive enough to make offline
     * guessing of a 4-digit PIN impractical for a leaked verifier, without stalling the
     * device (a few hundred milliseconds on a modern phone).
     */
    const val PBKDF2_ITERATIONS = 600_000

    private const val KEY_LENGTH_BITS = 256
    private const val GCM_TAG_BITS = 128
    private const val GCM_IV_LENGTH = 12

    /**
     * Serializer for the pair (title, text) so a hidden note encrypts as one opaque blob
     * instead of two independently-nonced columns.
     */
    val hiddenContentJson: Json = Json { encodeDefaults = true }

    /**
     * Maps Arabic-Indic (٠-٩), Eastern-Arabic (۰-۹) and Latin (0-9) digits to a single
     * comparable Latin form. Leading zeros survive because the comparison is textual, never
     * numeric. Non-digit characters are skipped so pasting "12 34" still yields "1234".
     */
    fun normalizeDigits(raw: String): String = buildString(raw.length) {
        for (ch in raw) {
            val digit = when (ch) {
                in '0'..'9' -> ch - '0'
                in '\u0660'..'\u0669' -> ch - '\u0660'
                in '\u06F0'..'\u06F9' -> ch - '\u06F0'
                else -> continue
            }
            append(digit)
        }
    }

    /** Crypto-strength random bytes (used for salts, nonces and data keys). */
    fun randomBytes(size: Int): ByteArray = ByteArray(size).also { SecureRandom().nextBytes(it) }

    /** Slow hash of the normalized PIN, never stored in plaintext form. */
    fun derivePinKey(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(
            normalizeDigits(pin).toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_LENGTH_BITS
        )
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded
    }

    /** Compares two digests in constant time; a mismatch can never leak its position. */
    fun isEqual(a: ByteArray, b: ByteArray): Boolean = MessageDigest.isEqual(a, b)

    fun aesGcmEncrypt(key: ByteArray, plaintext: ByteArray): AesGcmBox {
        val iv = randomBytes(GCM_IV_LENGTH)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
        return AesGcmBox(iv = iv, ciphertext = cipher.doFinal(plaintext))
    }

    fun aesGcmDecrypt(key: ByteArray, box: AesGcmBox): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_BITS, box.iv))
        return cipher.doFinal(box.ciphertext)
    }

    fun encodeBase64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    fun decodeBase64(encoded: String): ByteArray = Base64.getDecoder().decode(encoded)
}

/** A GCM box: a fresh random nonce plus the (tagged) ciphertext. */
data class AesGcmBox(val iv: ByteArray, val ciphertext: ByteArray)

/** The plaintext shape of a hidden note right before it is encrypted. */
@Serializable
data class HiddenContent(val title: String, val text: String)