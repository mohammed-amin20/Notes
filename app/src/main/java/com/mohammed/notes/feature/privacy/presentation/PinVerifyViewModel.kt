package com.mohammed.notes.feature.privacy.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.NotesDB
import com.mohammed.notes.feature.core.data.data_source.local.shared_prefs.NotesPrefs
import com.mohammed.notes.feature.core.security.PinVerifyResult
import com.mohammed.notes.feature.core.security.PrivacyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PIN gate for the private area. Besides the plain right/wrong answer it surfaces the
 * escalating throttle as a live countdown: [PrivacyStore] owns the persisted schedule,
 * this VM only mirrors the remaining time so the screen can tick it down without
 * re-deriving the PIN.
 */
@HiltViewModel
class PinVerifyViewModel @Inject constructor(
    private val store: PrivacyStore,
    private val notesPrefs: NotesPrefs,
    private val db: NotesDB
) : ViewModel() {

    private val _state = MutableStateFlow(PinVerifyState())
    val state = _state.asStateFlow()

    fun verify(pin: String) {
        if (_state.value.lockedRemainingMs > 0L || _state.value.verifying) return
        _state.update { it.copy(hasError = false) }
        viewModelScope.launch {
            try {
                _state.update { it.copy(verifying = true) }
                when (val res = store.verifyPin(notesPrefs.getUserId(), pin)) {
                    PinVerifyResult.Success -> _state.update {
                        it.copy(verified = true, hasError = false, lockedRemainingMs = 0L)
                    }

                    PinVerifyResult.Wrong -> _state.update { it.copy(hasError = true) }

                    is PinVerifyResult.Locked -> startCooldown(res.remainingMillis)
                }
            } finally {
                _state.update { it.copy(verifying = false) }
            }
        }
    }

    /** Clears the wrong-PIN banner the moment a fresh attempt starts. */
    fun clearError() {
        if (_state.value.hasError) _state.update { it.copy(hasError = false) }
    }

    private fun startCooldown(remainingMillis: Long) {
        _state.update { it.copy(hasError = false, lockedRemainingMs = remainingMillis) }
        viewModelScope.launch {
            var remaining = remainingMillis
            while (remaining > 0L) {
                delay(1_000L)
                remaining -= 1_000L
                _state.update { it.copy(lockedRemainingMs = remaining.coerceAtLeast(0L)) }
            }
        }
    }

    /**
     * The forgotten-PIN escape hatch the product approved: drops the verifier and the
     * wrapped data key, then deletes the hidden rows they protected — their ciphertext is
     * unrecoverable the moment the key is gone, so keeping them would only show broken
     * entries forever. Visible notes are untouched. Irreversible by design.
     */
    fun resetPrivacy() {
        viewModelScope.launch {
            val userId = notesPrefs.getUserId()
            store.resetPrivacy(userId)
            db.noteDao.deleteHiddenNotes(userId)
            _state.update { it.copy(reset = true) }
        }
    }

    data class PinVerifyState(
        val verified: Boolean = false,
        val hasError: Boolean = false,
        val lockedRemainingMs: Long = 0L,
        val reset: Boolean = false,
        /** PBKDF2 verification can take a beat; the screen shows progress while it runs. */
        val verifying: Boolean = false
    ) {
        val locked: Boolean get() = lockedRemainingMs > 0L
    }
}
