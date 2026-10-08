package com.mohammed.notes.feature.privacy.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammed.notes.feature.core.data.data_source.local.shared_prefs.NotesPrefs
import com.mohammed.notes.feature.core.security.PrivacyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Two-beat PIN creation (choose, confirm) plus the change flow, which reuses the same two
 * beats after the caller has already verified the current PIN. A mismatch restarts from
 * the first beat so a stray shoulder-surf never leaves half a choice behind.
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

    fun submit(pin: String) {
        when (_state.value.step) {
            PinStep.SET -> {
                firstPin = pin
                _state.update { it.copy(step = PinStep.CONFIRM, error = false) }
            }

            PinStep.CONFIRM -> {
                if (pin == firstPin) {
                    viewModelScope.launch {
                        store.setPin(notesPrefs.getUserId(), pin)
                        _state.update { it.copy(done = true) }
                    }
                } else {
                    firstPin = ""
                    _state.update { it.copy(step = PinStep.SET, error = true) }
                }
            }
        }
    }

    /** The caller clears the error as soon as the user starts a fresh attempt. */
    fun clearError() {
        if (_state.value.error) _state.update { it.copy(error = false) }
    }

    data class PinSetupState(
        val step: PinStep = PinStep.SET,
        val done: Boolean = false,
        val error: Boolean = false
    )
}
