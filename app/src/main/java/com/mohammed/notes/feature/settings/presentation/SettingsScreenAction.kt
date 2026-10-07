package com.mohammed.notes.feature.settings.presentation

sealed interface SettingsScreenAction {
    data object OnLogoutClick : SettingsScreenAction
    data class OnLogoutDialogVisibleChange(val visible: Boolean) : SettingsScreenAction
    data object OnLogoutConfirmed : SettingsScreenAction
}