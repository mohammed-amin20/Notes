package com.mohammed.notes.feature.privacy.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Two beats: prove you know the current PIN, then choose a new one. Forgetting it here
 * takes the same destructive escape hatch as the verify gate — the app will not pretend a
 * PIN change is possible without the old one.
 */
@Composable
fun ChangePinRoute(
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    var verified by rememberSaveable { mutableStateOf(false) }

    if (verified) {
        PinSetupScreen(
            onDone = onDone,
            onCancel = onCancel,
            changeMode = true
        )
    } else {
        PinVerifyScreen(
            onVerified = { verified = true },
            onCancel = onCancel,
            onResetDone = onDone,
            showForgotPin = true
        )
    }
}
