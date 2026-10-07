package com.mohammed.notes.feature.core.presentation.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.NotesDB
import com.mohammed.notes.feature.core.data.data_source.local.shared_prefs.NotesPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Single source of truth for "what is the app during this cold start". Runs exactly once per
 * instance (it is scoped to the Startup back-stack entry, which is popped once it fires), so
 * recomposition can never re-run the resolution or re-fire navigation.
 */
@HiltViewModel
class StartupViewModel @Inject constructor(
    private val db: NotesDB,
    private val prefs: NotesPrefs,
) : ViewModel() {

    private val _state = MutableStateFlow<StartupState>(StartupState.Loading)
    val state = _state.asStateFlow()

    init {
        restore()
    }

    /** Offered by the failure layout: re-run the same resolution once. */
    fun retry() {
        restore()
    }

    private fun restore() {
        viewModelScope.launch {
            _state.value = StartupState.Loading
            val resolved = try {
                when {
                    // Not the answer yet — just the flag. A true "signed out" requires the
                    // account row below to also be absent or cleared.
                    !prefs.getLoggedIn() -> StartupState.SignedOut

                    // Session validation: the stored userId must still resolve to a real
                    // account. A stale pair is cleared so we never route into another
                    // user's (or nobody's) data.
                    db.userDao.getUserById(prefs.getUserId()) == null -> {
                        prefs.setLoggedIn(false)
                        prefs.setUserId(0)
                        StartupState.SignedOut
                    }

                    else -> StartupState.Authenticated
                }
            } catch (_: Throwable) {
                StartupState.Failed
            }
            _state.value = resolved
        }
    }
}