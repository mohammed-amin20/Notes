package com.mohammed.notes.feature.auth.presentation.signup_screen

sealed interface SignUpScreenAction {
    data class OnUsernameChanged ( val username : String) :SignUpScreenAction
    data class OnEmailChanged ( val email : String) :SignUpScreenAction
    data class OnPasswordChanged ( val password : String) :SignUpScreenAction
    data object OnSignUpClicked: SignUpScreenAction

}