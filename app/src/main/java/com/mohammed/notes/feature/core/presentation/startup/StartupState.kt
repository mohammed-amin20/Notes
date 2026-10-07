package com.mohammed.notes.feature.core.presentation.startup

/**
 * Sessions here are deliberately split: [Loading] means the stored session has not been
 * resolved yet (either prefs or the account row backing it), while [SignedOut] is the
 * confirmed, validated answer. Navigating to Login before that answer was decided is what
 * made an authenticated user see the form flash on every cold start.
 */
sealed interface StartupState {
    /** The stored session is being validated against the local account store. */
    data object Loading : StartupState

    /** No logged_in flag, or its user row no longer exists (stale session cleared). */
    data object SignedOut : StartupState

    /** logged_in is set and the user row resolves. The notes graph may open. */
    data object Authenticated : StartupState

    /** Session resolution failed (local store unreachable). Retry is offered. */
    data object Failed : StartupState
}