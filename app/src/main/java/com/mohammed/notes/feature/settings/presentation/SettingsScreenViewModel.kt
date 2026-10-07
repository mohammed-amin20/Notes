package com.mohammed.notes.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.NotesDB
import com.mohammed.notes.feature.core.data.data_source.local.shared_prefs.NotesPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val db: NotesDB,
    private val prefs: NotesPrefs,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsScreenState())
    val state = _state.asStateFlow()

    /**
     * One-shot so re-entering the screen cannot re-fire the navigation. The host clears its
     * authenticated back stack on receipt, which is why this is an event rather than state.
     */
    private val _loggedOut = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val loggedOut = _loggedOut.asSharedFlow()

    init {
        viewModelScope.launch {
            val user = db.userDao.getUserById(prefs.getUserId())
            _state.update {
                it.copy(
                    username = user?.username.orEmpty(),
                    email = user?.email.orEmpty()
                )
            }
        }
    }

    fun onAction(action: SettingsScreenAction) {
        when (action) {
            is SettingsScreenAction.OnLogoutDialogVisibleChange -> {
                _state.update { it.copy(logoutDialogVisible = action.visible) }
            }

            SettingsScreenAction.OnLogoutClick -> {
                _state.update { it.copy(logoutDialogVisible = true) }
            }

            SettingsScreenAction.OnLogoutConfirmed -> {
                prefs.setLoggedIn(false)
                prefs.setUserId(0)
                _state.update { it.copy(logoutDialogVisible = false) }
                _loggedOut.tryEmit(Unit)
            }
        }
    }
}
