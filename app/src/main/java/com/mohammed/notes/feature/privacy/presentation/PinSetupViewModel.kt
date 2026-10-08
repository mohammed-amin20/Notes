package com.mohammed.notes.feature.privacy.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammed.notes.feature.core.data.data_source.local.shared_prefs.NotesPrefs
import com.mohammed.notes.feature.core.security.PinCrypto
import com.mohammed.notes.feature.core.security.PrivacyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Two-beat PIN creation (create, confirm) plus the change flow, which reuses the same two
 * beats after the caller has already verified the current PIN. The confirm beat keeps the
 * first PIN in memory so a mismatch clears only the confirmation entry and lets the user
 * retry; Back (or the system back gesture) starts over from the create beat.
 *
 * While the PIN is being written the state is `saving`, which the screen uses to disable
 * every action and show progress so the write can't be submitted twice or abandoned
 * mid-flight. A failed write surfaces [PinSetupError.SAVE_FAILED] instead of `done`, so
 * nothing ever reports success it didn't have.
 *
 * Setting or changing the PIN re-wraps only the verifier; the per-user data key — the
 * thing that keeps existing hidden notes readable — is untouched.
 */
@HiltViewModel
class PinSetupViewModel @Inject constructor(
    private val store: PrivacyStore,
    private val notesPrefs: NotesPrefs
) : ViewModel() {

    private val _state = MutableStateFlow(PinSetupState())
    val state = _state.asStateFlow()

    private var firstPin: String = ""

    fun typeDigit(digit: Char) {
        val current = _state.value
        if (current.saving || current.done || current.digits.length >= PinCrypto.PIN_LENGTH) return
        _state.update { it.copy(digits = it.digits + digit, error = null) }
    }

    fun backspace() {
        _state.update { current ->
            if (current.digits.isEmpty()) current
            else current.copy(digits = current.digits.dropLast(1), error = null)
        }
    }

    fun clearDigits() {
        _state.update { if (it.digits.isEmpty()) it else it.copy(digits = "", error = null) }
    }

    /** Advances from create to confirm, or verifies and writes the PIN on the confirm beat. */
    fun submit() {
        val current = _state.value
        if (current.saving || current.done || current.digits.length < PinCrypto.PIN_LENGTH) return
        when (current.step) {
            PinStep.SET -> {
                firstPin = current.digits
                _state.update { it.copy(digits = "", step = PinStep.CONFIRM, error = null) }
            }

            PinStep.CONFIRM -> {
                if (current.digits == firstPin) save(current.digits)
                else _state.update { it.copy(digits = "", error = PinSetupError.MISMATCH) }
            }
        }
    }

    /** Back from confirm to create: the half-remembered choice is dropped entirely. */
    fun backToFirst() {
        firstPin = ""
        _state.update { it.copy(step = PinStep.SET, digits = "", error = null) }
    }

    private fun save(pin: String) {
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val result = runCatching { store.setPin(notesPrefs.getUserId(), pin) }
            _state.update {
                if (result.isSuccess) it.copy(saving = false, done = true)
                else it.copy(saving = false, error = PinSetupError.SAVE_FAILED)
            }
        }
    }

    data class PinSetupState(
        val step: PinStep = PinStep.SET,
        val digits: String = "",
        val done: Boolean = false,
        val error: PinSetupError? = null,
        val saving: Boolean = false
    )
}
