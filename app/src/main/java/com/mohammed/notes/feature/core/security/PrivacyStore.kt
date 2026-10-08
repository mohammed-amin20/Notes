package com.mohammed.notes.feature.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.mohammed.notes.feature.core.data.data_source.local.shared_prefs.NotesPrefs
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The hidden-notes vault. One per process.
 *
 * Model:
 *  - A non-exportable AES-256-GCM key lives in the Android KeyStore under one alias. It
 *    never leaves the device and dies with the app (uninstall wipes the KeyStore).
 *  - Each user owns a random 256-bit *data* key, generated once, that actually encrypts
 *    hidden content. It is stored only as a GCM-wrapped blob next to the verifier, so it is
 *    removable, re-wrappable and never exposed as raw bytes on disk.
 *  - The PIN never appears on disk. Only PBKDF2(salt, pin) is stored, and verification is
 *    constant-time.
 *
 * All heavy work (PBKDF2 up to 600k rounds, AES-GCM, KeyStore ops) runs on the default
 * dispatcher so callers get suspend functions instead of ANRs.
 */
class PrivacyStore(
    private val notesPrefs: NotesPrefs,
    private val privacy: PrivacyPrefs,
) {

    fun hasPin(userId: Int): Boolean = privacy.hasPin(userId)

    suspend fun hasUsableDataKey(userId: Int): Boolean = withContext(Dispatchers.Default) {
        val wrapped = privacy.getWrappedDataKey(userId) ?: return@withContext false
        runCatching { unwrapDataKey(wrapped) }.isSuccess
    }

    /**
     * Sets (or replaces) the PIN for an account. The data key — the thing that makes
     * existing hidden content readable — is created once and reused, which is exactly why
     * a PIN change never re-encrypts a single note.
     */
    suspend fun setPin(userId: Int, pin: String) {
        withContext(Dispatchers.Default) {
            ensureDataKey(userId)
            val salt = PinCrypto.randomBytes(SALT_SIZE)
            val verifier = PinCrypto.derivePinKey(pin, salt)
            privacy.setSalt(userId, PinCrypto.encodeBase64(salt))
            privacy.setVerifier(userId, PinCrypto.encodeBase64(verifier))
            privacy.setPin(userId, true)
            privacy.resetThrottle(userId)
        }
    }

    /** Verifies against the stored verifier and applies the throttle bookkeeping. */
    suspend fun verifyPin(userId: Int, pin: String): PinVerifyResult = withContext(Dispatchers.Default) {
        val now = System.currentTimeMillis()
        val lockedUntil = privacy.getLockedUntil(userId)
        if (now < lockedUntil) {
            return@withContext PinVerifyResult.Locked(remainingMillis = lockedUntil - now)
        }

        if (verify(userId, pin)) {
            privacy.resetThrottle(userId)
            return@withContext PinVerifyResult.Success
        }

        val failCount = privacy.getFailCount(userId) + 1
        privacy.setFailCount(userId, failCount)
        val cooldown = PinThrottle.cooldownFor(failCount)
        if (cooldown > 0L) {
            privacy.setLockedUntil(userId, now + cooldown)
        }
        PinVerifyResult.Wrong
    }

    private fun verify(userId: Int, pin: String): Boolean {
        val saltB64 = privacy.getSalt(userId) ?: return false
        val verifierB64 = privacy.getVerifier(userId) ?: return false
        return runCatching {
            val actual = PinCrypto.derivePinKey(pin, PinCrypto.decodeBase64(saltB64))
            PinCrypto.isEqual(actual, PinCrypto.decodeBase64(verifierB64))
        }.getOrDefault(false)
    }

    /** Hides `title`/`text` as one encrypted blob. Nonces are random per call. */
    suspend fun encryptHiddenContent(userId: Int, title: String, text: String): EncryptedNoteContent {
        return withContext(Dispatchers.Default) {
            val wrapped = requireNotNull(privacy.getWrappedDataKey(userId)) { "No data key" }
            val dataKey = unwrapDataKey(wrapped)
            val payload = PinCrypto.hiddenContentJson
                .encodeToString(HiddenContent.serializer(), HiddenContent(title, text))
                .encodeToByteArray()
            val box = PinCrypto.aesGcmEncrypt(dataKey, payload)
            EncryptedNoteContent(
                blobBase64 = PinCrypto.encodeBase64(box.ciphertext),
                nonceBase64 = PinCrypto.encodeBase64(box.iv)
            )
        }
    }

    suspend fun decryptHiddenContent(userId: Int, blobBase64: String, nonceBase64: String): HiddenContent {
        return withContext(Dispatchers.Default) {
            val wrapped = requireNotNull(privacy.getWrappedDataKey(userId)) { "No data key" }
            val dataKey = unwrapDataKey(wrapped)
            val box = AesGcmBox(
                iv = PinCrypto.decodeBase64(nonceBase64),
                ciphertext = PinCrypto.decodeBase64(blobBase64)
            )
            val plaintext = PinCrypto.aesGcmDecrypt(dataKey, box)
            PinCrypto.hiddenContentJson.decodeFromString(HiddenContent.serializer(), plaintext.decodeToString())
        }
    }

    /** Data-losing reset: drops the verifier and the wrapped key. Content stays encrypted and unreadable. */
    suspend fun resetPrivacy(userId: Int) {
        withContext(Dispatchers.Default) { privacy.clearUserSecrets(userId) }
    }

    private fun ensureDataKey(userId: Int) {
        if (privacy.getWrappedDataKey(userId) != null) return
        val rawKey = PinCrypto.randomBytes(DATA_KEY_SIZE)
        val box = wrapDataKey(rawKey)
        val blob = PinCrypto.encodeBase64(box.iv) + SEPARATOR + PinCrypto.encodeBase64(box.ciphertext)
        privacy.setWrappedDataKey(userId, blob)
    }

    private fun wrapDataKey(dataKey: ByteArray): AesGcmBox {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey())
        return AesGcmBox(
            iv = cipher.iv,
            ciphertext = cipher.doFinal(dataKey)
        )
    }

    private fun unwrapDataKey(wrappedBase64: String): ByteArray {
        // A wrapped blob is iv||ciphertext; iv is 96 bits, so the split is unambiguous.
        val parts = wrappedBase64.split(SEPARATOR)
        require(parts.size == 2) { "Malformed wrapped data key" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            masterKey(),
            javax.crypto.spec.GCMParameterSpec(GCM_TAG_BITS, PinCrypto.decodeBase64(parts[0]))
        )
        return cipher.doFinal(PinCrypto.decodeBase64(parts[1]))
    }

    private fun masterKey() = synchronized(lock) {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (keyStore.getEntry(MASTER_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey ?: createMasterKey()
    }

    private fun createMasterKey(): javax.crypto.SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                MASTER_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(DATA_KEY_SIZE * 8)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val KEYSTORE = "AndroidKeyStore"
        const val MASTER_ALIAS = "memo_privacy_master"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
        const val DATA_KEY_SIZE = 32
        const val SALT_SIZE = 16
        const val SEPARATOR = ":"
        private val lock = Any()
    }
}

/** Successful or throttled outcome of a PIN check. */
sealed interface PinVerifyResult {
    data object Success : PinVerifyResult
    data object Wrong : PinVerifyResult
    data class Locked(val remainingMillis: Long) : PinVerifyResult
}

/** The stored shape of one encrypted hidden note: blob (tagged ciphertext) + nonce. */
data class EncryptedNoteContent(
    val blobBase64: String,
    val nonceBase64: String,
)