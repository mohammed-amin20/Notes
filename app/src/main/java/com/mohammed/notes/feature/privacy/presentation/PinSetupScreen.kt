package com.mohammed.notes.feature.privacy.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohammed.notes.R
import com.mohammed.notes.feature.core.security.PinCrypto
import com.mohammed.notes.ui.theme.Space

@Composable
fun PinSetupScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    changeMode: Boolean = false,
    viewModel: PinSetupViewModel = hiltViewModel()
) {
    SecureScreen()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var digits by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.done) {
        if (state.done) onDone()
    }

    val title = when {
        state.step == PinStep.SET && changeMode -> stringResource(R.string.pin_change_title)
        state.step == PinStep.SET -> stringResource(R.string.pin_set_title)
        else -> stringResource(R.string.pin_confirm_title)
    }

    Scaffold { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (!changeMode && state.step == PinStep.SET) {
                Spacer(Modifier.height(Space.sm))
                Text(
                    text = stringResource(R.string.hidden_setup_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Space.lg))
            PinDots(PinCrypto.PIN_LENGTH, digits.length, state.error)
            Spacer(Modifier.height(Space.xl))
            if (state.error) {
                Text(stringResource(R.string.pin_mismatch), color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(Space.lg))
            }
            PinKeypad(
                onDigit = { d ->
                    if (digits.isEmpty()) viewModel.clearError()
                    if (digits.length < PinCrypto.PIN_LENGTH) {
                        digits += d
                        if (digits.length == PinCrypto.PIN_LENGTH) {
                            viewModel.submit(digits)
                            digits = ""
                        }
                    }
                },
                onClear = { digits = "" }
            )
            Spacer(Modifier.height(Space.xl))
            Button(onClick = onCancel) { Text(stringResource(R.string.action_cancel)) }
        }
    }
}
