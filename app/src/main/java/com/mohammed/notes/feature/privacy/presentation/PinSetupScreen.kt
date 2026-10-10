package com.mohammed.notes.feature.privacy.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohammed.notes.R
import com.mohammed.notes.feature.core.security.PinCrypto
import com.mohammed.notes.ui.theme.FormMeasure
import com.mohammed.notes.ui.theme.Motion
import com.mohammed.notes.ui.theme.NotesTheme
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space
import com.mohammed.notes.ui.theme.rememberAnimationsEnabled

/**
 * The two explicit beats of PIN creation: an OK-confirmed "Create PIN" step followed by a
 * "Confirm PIN" step that verifies before anything is written. Nothing auto-submits — the
 * OK button is the single commit action, and Back (button or system gesture) returns to
 * the first beat while Cancel abandons the flow entirely. All input handling lives in
 * [PinSetupViewModel] so the current beat, the digits and any inline error stay in sync.
 */
@Composable
fun PinSetupScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    changeMode: Boolean = false,
    viewModel: PinSetupViewModel = hiltViewModel()
) {
    SecureScreen()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val animationsOn = rememberAnimationsEnabled()

    BackHandler(enabled = !state.saving && state.step == PinStep.CONFIRM) {
        viewModel.backToFirst()
    }

    LaunchedEffect(state.done) {
        if (state.done) onDone()
    }

    PinSetupContent(
        state = state,
        changeMode = changeMode,
        onDigit = viewModel::typeDigit,
        onClear = viewModel::clearDigits,
        onBackspace = viewModel::backspace,
        onSubmit = viewModel::submit,
        onBackToFirst = viewModel::backToFirst,
        onCancel = onCancel
    )
}

/**
 * Stateless PIN setup body so the screen's Hilt-backed [PinSetupViewModel] stays out of
 * @Preview. The caller owns the state and routes every callback back to the ViewModel.
 */
@Composable
internal fun PinSetupContent(
    state: PinSetupViewModel.PinSetupState,
    changeMode: Boolean,
    onDigit: (Char) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    onBackToFirst: () -> Unit,
    onCancel: () -> Unit
) {
    val animationsOn = rememberAnimationsEnabled()

    val errorRes = when (state.error) {
        PinSetupError.MISMATCH -> R.string.pin_mismatch
        PinSetupError.SAVE_FAILED -> R.string.pin_save_failed
        null -> null
    }

    Scaffold { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(Space.xxl))
            Crossfade(
                targetState = state.step,
                animationSpec = if (animationsOn) tween(Motion.enter) else snap()
            ) { step ->
                val titleRes = when {
                    step == PinStep.SET && changeMode -> R.string.pin_change_title
                    step == PinStep.SET -> R.string.pin_create_title
                    else -> R.string.pin_confirm_title
                }
                val bodyRes = when {
                    step == PinStep.CONFIRM -> R.string.pin_confirm_body
                    step == PinStep.SET && !changeMode -> R.string.pin_create_body
                    else -> null
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = Space.lg)
                ) {
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    if (bodyRes != null) {
                        Spacer(Modifier.height(Space.sm))
                        Text(
                            text = stringResource(bodyRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(Modifier.height(Space.xl))
            PinDots(
                length = PinCrypto.PIN_LENGTH,
                filledCount = state.digits.length,
                hasError = state.error == PinSetupError.MISMATCH
            )
            Spacer(Modifier.height(Space.md))
            AnimatedVisibility(
                visible = errorRes != null,
                enter = if (animationsOn) {
                    expandVertically(tween(Motion.enter)) + fadeIn(tween(Motion.enter))
                } else {
                    EnterTransition.None
                },
                exit = ExitTransition.None
            ) {
                Text(
                    text = stringResource(errorRes ?: R.string.pin_mismatch),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(Space.lg))
            Button(
                onClick = onSubmit,
                enabled = state.digits.length == PinCrypto.PIN_LENGTH && !state.saving,
                modifier = Modifier
                    .widthIn(max = FormMeasure)
                    .fillMaxWidth()
                    .heightIn(min = Size.buttonHeight)
            ) {
                if (state.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(Space.xs))
                    Text(
                        text = stringResource(R.string.pin_saving),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(stringResource(R.string.action_ok))
                }
            }
            Spacer(Modifier.height(Space.lg))
            PinKeypad(
                onDigit = onDigit,
                onClear = onClear,
                onBackspace = onBackspace
            )
            Spacer(Modifier.height(Space.lg))
            Row(
                horizontalArrangement = Arrangement.spacedBy(Space.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.step == PinStep.CONFIRM) {
                    TextButton(
                        onClick = onBackToFirst,
                        enabled = !state.saving
                    ) { Text(stringResource(R.string.action_back)) }
                }
                TextButton(onClick = onCancel, enabled = !state.saving) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
            Spacer(Modifier.height(Space.xs))
        }
    }
}

// --- Previews --------------------------------------------------------------

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PinSetupLightPreview() {
    NotesTheme(darkTheme = false) {
        PinSetupContent(
            state = PinSetupViewModel.PinSetupState(),
            changeMode = false,
            onDigit = {},
            onClear = {},
            onBackspace = {},
            onSubmit = {},
            onBackToFirst = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PinSetupDarkPreview() {
    NotesTheme(darkTheme = true) {
        PinSetupContent(
            state = PinSetupViewModel.PinSetupState(digits = "12"),
            changeMode = false,
            onDigit = {},
            onClear = {},
            onBackspace = {},
            onSubmit = {},
            onBackToFirst = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PinSetupConfirmPreview() {
    NotesTheme(darkTheme = false) {
        PinSetupContent(
            state = PinSetupViewModel.PinSetupState(step = PinStep.CONFIRM),
            changeMode = false,
            onDigit = {},
            onClear = {},
            onBackspace = {},
            onSubmit = {},
            onBackToFirst = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PinSetupMismatchPreview() {
    NotesTheme(darkTheme = false) {
        PinSetupContent(
            state = PinSetupViewModel.PinSetupState(
                step = PinStep.CONFIRM,
                error = PinSetupError.MISMATCH
            ),
            changeMode = false,
            onDigit = {},
            onClear = {},
            onBackspace = {},
            onSubmit = {},
            onBackToFirst = {},
            onCancel = {}
        )
    }
}
