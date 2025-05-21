package com.mohammed.notes.feature.auth.presentation.login_screen

data class LoginScreenState(
    val username : String = "",
    val password : String = "",
    val loggedIn: Boolean = false
)
