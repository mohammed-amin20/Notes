package com.mohammed.notes.feature.auth.presentation.signup_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.NotesDB
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpScreenViewModel @Inject constructor(
   private val db: NotesDB,
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpScreenState())
    val state = _state.asStateFlow()

    private val _uiAction = MutableSharedFlow<UiAction>()
    val uiAction = _uiAction.asSharedFlow()


    fun onAction(action: SignUpScreenAction) {
        when (action) {
            is SignUpScreenAction.OnUsernameChanged -> {
                _state.update {
                    it.copy(
                        username = action.username
                    )
                }
            }

            is SignUpScreenAction.OnEmailChanged -> {
                _state.update {
                    it.copy(
                        email = action.email
                    )
                }
            }

            is SignUpScreenAction.OnPasswordChanged -> {
                _state.update {
                    it.copy(
                        password = action.password
                    )
                }
            }

            SignUpScreenAction.OnSignUpClicked -> {
                viewModelScope.launch {
                    val username = state.value.username.trim()
                    val email = state.value.email.trim()
                    val password = state.value.password.trim()
                    if (username.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                        db.userDao.upsertUser(
                            user = User(
                                username = username,
                                email = email,
                                password = password
                            )
                        )
                        _uiAction.emit(UiAction.GoToLoginScreen)
                    } else {
                        _uiAction.emit(UiAction.OnToastMessage(message = "All Fields Required"))
                    }
                }
            }
        }
    }

    sealed interface UiAction {
        data class OnToastMessage(val message: String) : UiAction
        data object GoToLoginScreen : UiAction
    }
}