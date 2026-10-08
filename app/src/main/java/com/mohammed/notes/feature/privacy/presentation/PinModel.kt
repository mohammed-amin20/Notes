package com.mohammed.notes.feature.privacy.presentation

/** The beats of setting a PIN: choose it, then confirm it. */
enum class PinStep {
    SET,
    CONFIRM
}

/** Why the setup screen is showing its inline error. */
enum class PinSetupError {
    MISMATCH,
    SAVE_FAILED
}
