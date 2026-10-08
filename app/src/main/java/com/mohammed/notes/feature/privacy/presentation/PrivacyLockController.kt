package com.mohammed.notes.feature.privacy.presentation

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The in-memory "hidden notes are open" session. Unlock survives navigation (so opening one
 * note and coming back doesn't re-ask), but nothing here survives the process, and the
 * VMs behind the private screens clear it on the documented triggers:
 *  - leaving the private area,
 *  - backgrounding the app,
 *  - locking the device,
 *  - signing out,
 *  - process death (memory-only by construction).
 */
@Singleton
class PrivacyLockController @Inject constructor() {

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked = _isUnlocked.asStateFlow()

    fun unlock() {
        _isUnlocked.value = true
    }

    fun lock() {
        _isUnlocked.value = false
    }
}