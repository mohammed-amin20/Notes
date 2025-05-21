package com.mohammed.notes.feature.auth.presentation.login_screen

sealed interface LoginScreenAction {

    data class OnUsernameChange(val  username : String): LoginScreenAction

    data class OnPasswordChange(val  password : String): LoginScreenAction

    data object OnLoginClicked : LoginScreenAction

}