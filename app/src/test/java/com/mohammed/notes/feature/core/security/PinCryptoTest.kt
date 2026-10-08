package com.mohammed.notes.feature.core.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinCryptoTest {

    @Test
    fun `latin digits pass through unchanged`() {
        assertEquals("0123", PinCrypto.normalizeDigits("0123"))
    }

    @Test
    fun `leading zeros survive normalization`() {
        assertEquals("0000", PinCrypto.normalizeDigits("0000"))
        assertEquals("0070", PinCrypto.normalizeDigits("0070"))
    }

    @Test
    fun `arabic-indic digits map to latin`() {
        assertEquals("0123", PinCrypto.normalizeDigits("\u0660\u0661\u0662\u0663"))
    }

    @Test
    fun `eastern-arabic digits map to latin`() {
        assertEquals("9876", PinCrypto.normalizeDigits("\u06F9\u06F8\u06F7\u06F6"))
    }

    @Test
    fun `mixed scripts and separators collapse to one pin`() {
        assertEquals("1234", PinCrypto.normalizeDigits("1\u0662 3\u06F4"))
    }

    @Test
    fun `non-digit characters are dropped`() {
        assertEquals("1234", PinCrypto.normalizeDigits("PIN: 12-34!"))
    }

    @Test
    fun `derivation is deterministic for the same pin and salt`() {
        val salt = PinCrypto.randomBytes(16)
        val first = PinCrypto.derivePinKey("1234", salt)
        val second = PinCrypto.derivePinKey("1234", salt)
        assertArrayEquals(first, second)
    }

    @Test
    fun `equivalent pins across scripts derive the same key`() {
        val salt = PinCrypto.randomBytes(16)
        val latin = PinCrypto.derivePinKey("1234", salt)
        val arabic = PinCrypto.derivePinKey("\u0661\u0662\u0663\u0664", salt)
        assertArrayEquals(latin, arabic)
    }

    @Test
    fun `different pins derive different keys`() {
        val salt = PinCrypto.randomBytes(16)
        val a = PinCrypto.derivePinKey("1234", salt)
        val b = PinCrypto.derivePinKey("1235", salt)
        assertFalse(PinCrypto.isEqual(a, b))
    }

    @Test
    fun `different salts change the derived key`() {
        val a = PinCrypto.derivePinKey("1234", PinCrypto.randomBytes(16))
        val b = PinCrypto.derivePinKey("1234", PinCrypto.randomBytes(16))
        assertFalse(PinCrypto.isEqual(a, b))
    }

    @Test
    fun `aes-gcm round trip returns the original plaintext`() {
        val key = PinCrypto.randomBytes(32)
        val plaintext = "secret note body".encodeToByteArray()
        val box = PinCrypto.aesGcmEncrypt(key, plaintext)
        val recovered = PinCrypto.aesGcmDecrypt(key, box)
        assertArrayEquals(plaintext, recovered)
    }

    @Test
    fun `each encryption uses a fresh nonce`() {
        val key = PinCrypto.randomBytes(32)
        val plaintext = "same".encodeToByteArray()
        val first = PinCrypto.aesGcmEncrypt(key, plaintext)
        val second = PinCrypto.aesGcmEncrypt(key, plaintext)
        assertFalse(first.iv.contentEquals(second.iv))
        assertNotEquals(first.ciphertext.toList(), second.ciphertext.toList())
    }

    @Test
    fun `tampered ciphertext fails to decrypt`() {
        val key = PinCrypto.randomBytes(32)
        val box = PinCrypto.aesGcmEncrypt(key, "payload".encodeToByteArray())
        val tampered = box.copy(ciphertext = box.ciphertext.clone().also { it[0] = (it[0] + 1).toByte() })
        try {
            PinCrypto.aesGcmDecrypt(key, tampered)
            throw AssertionError("tampered ciphertext decrypted")
        } catch (expected: Exception) {
            // GCM authentication failure is the whole point of the tag.
        }
    }

    @Test
    fun `base64 round trip preserves bytes`() {
        val bytes = PinCrypto.randomBytes(33)
        assertArrayEquals(bytes, PinCrypto.decodeBase64(PinCrypto.encodeBase64(bytes)))
    }

    @Test
    fun `isEqual accepts equal arrays of different instances`() {
        assertTrue(PinCrypto.isEqual(byteArrayOf(1, 2, 3), byteArrayOf(1, 2, 3)))
        assertFalse(PinCrypto.isEqual(byteArrayOf(1, 2, 3), byteArrayOf(1, 2, 4)))
        assertFalse(PinCrypto.isEqual(byteArrayOf(1, 2, 3), byteArrayOf(1, 2)))
    }
}
