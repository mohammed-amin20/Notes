package com.mohammed.notes.feature.core.security

import android.content.Context
import androidx.core.content.edit

/**
 * Where every hidden-note secret lives. This exact file — `privacy_prefs.xml` — is the one
 * excluded from Android Auto Backup and device transfer (see `backup_rules.xml` and
 * `data_extraction_rules.xml`), so a cloud snapshot never contains the verifier, the
 * wrapped data key or the throttle counters.
 *
 * Every key is scoped per user id; the constructors do the key-prefixing so callers never
 * concatenate strings themselves.
 */
class PrivacyPrefs(context: Context) {

    private val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun hasPin(userId: Int): Boolean = prefs.getBoolean(key(userId, "pin_set"), false)

    fun setPin(userId: Int, value: Boolean) {
        prefs.edit { putBoolean(key(userId, "pin_set"), value) }
    }

    fun getWrappedDataKey(userId: Int): String? = prefs.getString(key(userId, "wrapped_key"), null)

    fun setWrappedDataKey(userId: Int, base64: String) {
        prefs.edit { putString(key(userId, "wrapped_key"), base64) }
    }

    fun getSalt(userId: Int): String? = prefs.getString(key(userId, "salt"), null)

    fun setSalt(userId: Int, base64: String) {
        prefs.edit { putString(key(userId, "salt"), base64) }
    }

    fun getVerifier(userId: Int): String? = prefs.getString(key(userId, "verifier"), null)

    fun setVerifier(userId: Int, base64: String) {
        prefs.edit { putString(key(userId, "verifier"), base64) }
    }

    fun getFailCount(userId: Int): Int = prefs.getInt(key(userId, "fail_count"), 0)

    fun setFailCount(userId: Int, count: Int) {
        prefs.edit { putInt(key(userId, "fail_count"), count) }
    }

    /** Epoch millis before which the PIN is rejected outright, regardless of correctness. */
    fun getLockedUntil(userId: Int): Long = prefs.getLong(key(userId, "locked_until"), 0L)

    fun setLockedUntil(userId: Int, millis: Long) {
        prefs.edit { putLong(key(userId, "locked_until"), millis) }
    }

    fun resetThrottle(userId: Int) {
        prefs.edit {
            putInt(key(userId, "fail_count"), 0)
            putLong(key(userId, "locked_until"), 0L)
        }
    }

    /** Wipes every byte that could resurrect or brute-force this account's protected data. */
    fun clearUserSecrets(userId: Int) {
        prefs.edit {
            remove(key(userId, "pin_set"))
            remove(key(userId, "wrapped_key"))
            remove(key(userId, "salt"))
            remove(key(userId, "verifier"))
            remove(key(userId, "fail_count"))
            remove(key(userId, "locked_until"))
        }
    }

    private fun key(userId: Int, name: String): String = "user_${userId}_$name"

    private companion object {
        const val FILE_NAME = "privacy_prefs"
    }
}