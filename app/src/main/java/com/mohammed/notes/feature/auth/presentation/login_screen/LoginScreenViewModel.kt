package com.mohammed.notes.feature.auth.presentation.login_screen

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
class LoginScreenViewModel @Inject constructor(
    private val db: NotesDB,
    private val prefs: NotesPrefs,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginScreenState())
    val state = _state.asStateFlow()

    private val _uiAction = MutableSharedFlow<UiAction>()
    val uiAction = _uiAction.asSharedFlow()

    init {
        if (prefs.getLoggedIn()) {
            _state.update {
                it.copy(
                    loggedIn = true
                )
            }
        }
    }

    fun onAction(action: LoginScreenAction) {
        when (action) {
            is LoginScreenAction.OnUsernameChange -> {
                _state.update {
                    it.copy(
                        username = action.username
                    )
                }
            }

            is LoginScreenAction.OnPasswordChange -> {
                _state.update {
                    it.copy(
                        password = action.password
                    )
                }
            }

            LoginScreenAction.OnLoginClicked -> {
                viewModelScope.launch {
                    val username = _state.value.username.trim()
                    val password = _state.value.password.trim()
                    if (username.isNotBlank() && password.isNotBlank()) {
                        val user = db.userDao
                            .getUser(username, password)
                        if (user == null) {
                            _uiAction.emit(UiAction.ShowToast("Error in username or password"))
                        } else {
                            prefs.setUserId(user.id!!)
                            prefs.setLoggedIn(true)
                            _uiAction.emit(UiAction.NavigateToHome)
                        }
                    } else {
                        _uiAction.emit(UiAction.ShowToast("Error in username or password"))
                    }
                }
            }
        }
    }

    sealed interface UiAction {
        data class ShowToast(val message: String) : UiAction
        data object NavigateToHome : UiAction
    }
}