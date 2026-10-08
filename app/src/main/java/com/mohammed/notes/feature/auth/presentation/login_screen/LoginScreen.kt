package com.mohammed.notes.feature.auth.presentation.login_screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohammed.notes.R
import com.mohammed.notes.feature.core.presentation.components.CustomTextField
import com.mohammed.notes.ui.theme.NotesTheme
import com.mohammed.notes.ui.theme.FormMeasure
import com.mohammed.notes.ui.theme.NotesTheme
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space
import com.mohammed.notes.ui.theme.accent

@Composable
fun LoginScreen(
    goToSignUp: () -> Unit,
    goToHome: () -> Unit,
    viewModel: LoginScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    /**
     * The previous version called `goToHome()` straight from composition when
     * `loggedIn` was true, which mutated navigation state during a layout pass. Keying a
     * `LaunchedEffect` on the flag keeps the same behaviour but runs it as a side effect.
     */
    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) goToHome()
    }

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is LoginScreenViewModel.UiAction.ShowToast -> Toast.makeText(
                    context,
                    context.getString(action.messageRes),
                    Toast.LENGTH_SHORT
                ).show()

                LoginScreenViewModel.UiAction.NavigateToHome -> goToHome()
            }
        }
    }

    LoginContent(
        state = state,
        onAction = viewModel::onAction,
        onSignUp = goToSignUp
    )
}

@Composable
fun LoginContent(
    state: LoginScreenState,
    onAction: (LoginScreenAction) -> Unit,
    onSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val passwordFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val canSubmit = state.username.isNotBlank() && state.password.isNotBlank()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.xl, vertical = Space.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier.widthIn(max = FormMeasure),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.memo_logo),
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.accent)
                    )
                    Spacer(Modifier.height(Space.lg))
                    Text(
                        text = stringResource(R.string.auth_login_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(Space.xs))
                Text(
                    text = stringResource(R.string.auth_login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Space.xl))

                CustomTextField(
                    value = state.username,
                    onValueChange = { onAction(LoginScreenAction.OnUsernameChange(it)) },
                    label = stringResource(R.string.auth_username),
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() }
                    ),
                    autofillType = ContentType.Username
                )
                Spacer(Modifier.height(Space.md))
                CustomTextField(
                    value = state.password,
                    onValueChange = { onAction(LoginScreenAction.OnPasswordChange(it)) },
                    label = stringResource(R.string.auth_password),
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            if (canSubmit) onAction(LoginScreenAction.OnLoginClicked)
                        }
                    ),
                    autofillType = ContentType.Password,
                    isPassword = true,
                    modifier = Modifier.focusRequester(passwordFocusRequester)
                )
                Spacer(Modifier.height(Space.xl))

                Button(
                    onClick = { onAction(LoginScreenAction.OnLoginClicked) },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Size.buttonHeight),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = stringResource(R.string.auth_login_action),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(Modifier.height(Space.md))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.auth_no_account),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onSignUp) {
                        Text(
                            text = stringResource(R.string.auth_signup_action),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginContentPreview() {
    NotesTheme(darkTheme = false) {
        LoginContent(
            state = LoginScreenState(username = "reader", password = "secret"),
            onAction = {},
            onSignUp = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginContentDarkPreview() {
    NotesTheme(darkTheme = true) {
        LoginContent(
            state = LoginScreenState(),
            onAction = {},
            onSignUp = {}
        )
    }
}
