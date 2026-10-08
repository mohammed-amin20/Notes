package com.mohammed.notes.feature.privacy.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohammed.notes.R
import com.mohammed.notes.feature.core.security.PinCrypto
import com.mohammed.notes.ui.theme.Space
import java.util.Locale

@Composable
fun PinVerifyScreen(
    onVerified: () -> Unit,
    onCancel: () -> Unit,
    onResetDone: () -> Unit,
    showForgotPin: Boolean = false,
    viewModel: PinVerifyViewModel = hiltViewModel()
) {
    SecureScreen()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var digits by rememberSaveable { mutableStateOf("") }
    var resetDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.verified) {
        if (state.verified) onVerified()
    }
    LaunchedEffect(state.reset) {
        if (state.reset) onResetDone()
    }

    Scaffold { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.pin_verify_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Space.lg))
            PinDots(PinCrypto.PIN_LENGTH, digits.length, state.hasError || state.locked)
            Spacer(Modifier.height(Space.xl))
            when {
                state.verifying -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(Space.xs))
                        Text(
                            stringResource(R.string.pin_verifying),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(Space.lg))
                }

                state.locked -> {
                    Text(
                        stringResource(R.string.pin_locked_try_later),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(Space.xs))
                    Text(
                        stringResource(
                            R.string.pin_try_again_in,
                            formatCooldown(state.lockedRemainingMs)
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(Space.lg))
                }

                state.hasError -> {
                    Text(stringResource(R.string.pin_wrong), color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(Space.lg))
                }
            }
            PinKeypad(
                onDigit = { d ->
                    if (state.verifying) return@PinKeypad
                    if (digits.isEmpty()) viewModel.clearError()
                    if (digits.length < PinCrypto.PIN_LENGTH && !state.locked) {
                        digits += d
                        if (digits.length == PinCrypto.PIN_LENGTH) {
                            viewModel.verify(digits)
                            digits = ""
                        }
                    }
                },
                onClear = { if (!state.verifying) digits = "" },
                onBackspace = {
                    if (digits.isNotEmpty() && !state.locked && !state.verifying) {
                        digits = digits.dropLast(1)
                    }
                }
            )
            Spacer(Modifier.height(Space.xl))
            Button(onClick = onCancel, enabled = !state.verifying) { Text(stringResource(R.string.action_cancel)) }
            if (showForgotPin && !state.locked && !state.verifying) {
                TextButton(onClick = { resetDialog = true }) {
                    Text(stringResource(R.string.pin_forgot))
                }
            }
        }
    }

    if (resetDialog) {
        AlertDialog(
            onDismissRequest = { resetDialog = false },
            title = { Text(stringResource(R.string.pin_reset_title)) },
            text = { Text(stringResource(R.string.pin_reset_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        resetDialog = false
                        viewModel.resetPrivacy()
                    }
                ) { Text(stringResource(R.string.pin_reset_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { resetDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

private fun formatCooldown(remainingMs: Long): String {
    val totalSeconds = (remainingMs + 999) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
