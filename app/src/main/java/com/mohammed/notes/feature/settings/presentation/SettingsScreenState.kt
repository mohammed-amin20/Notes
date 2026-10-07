package com.mohammed.notes.feature.settings.presentation

data class SettingsScreenState(
    val username: String = "",
    val email: String = "",
    val logoutDialogVisible: Boolean = false
)