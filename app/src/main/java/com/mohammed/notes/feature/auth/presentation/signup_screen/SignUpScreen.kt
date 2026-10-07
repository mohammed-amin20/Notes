package com.mohammed.notes.feature.auth.presentation.signup_screen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.mohammed.notes.ui.theme.FormMeasure
import com.mohammed.notes.ui.theme.NotesTheme
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space

@Composable
fun SignUpScreen(
    goToLogin: () -> Unit,
    viewModel: SignUpScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is SignUpScreenViewModel.UiAction.OnToastMessage -> Toast.makeText(
                    context,
                    context.getString(action.messageRes),
                    Toast.LENGTH_SHORT
                ).show()

                SignUpScreenViewModel.UiAction.GoToLoginScreen -> goToLogin()
            }
        }
    }

    SignUpContent(
        state = state,
        onAction = viewModel::onAction,
        onBackToLogin = goToLogin
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpContent(
    state: SignUpScreenState,
    onAction: (SignUpScreenAction) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val canSubmit = state.username.isNotBlank() &&
        state.email.isNotBlank() &&
        state.password.isNotBlank()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.auth_signup_action),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
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
                    .padding(horizontal = Space.xl, vertical = Space.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier.widthIn(max = FormMeasure),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                Image(
                    painter = painterResource(R.drawable.ic_note_brand),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(Space.lg))
                Text(
                    text = stringResource(R.string.auth_signup_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = stringResource(R.string.auth_signup_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Space.xl))

                CustomTextField(
                    value = state.username,
                    onValueChange = { onAction(SignUpScreenAction.OnUsernameChanged(it)) },
                    label = stringResource(R.string.auth_username),
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { emailFocusRequester.requestFocus() }
                    ),
                    autofillType = ContentType.Username
                )
                Spacer(Modifier.height(Space.md))
                CustomTextField(
                    value = state.email,
                    onValueChange = { onAction(SignUpScreenAction.OnEmailChanged(it)) },
                    label = stringResource(R.string.auth_email),
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() }
                    ),
                    autofillType = ContentType.EmailAddress,
                    modifier = Modifier.focusRequester(emailFocusRequester)
                )
                Spacer(Modifier.height(Space.md))
                CustomTextField(
                    value = state.password,
                    onValueChange = { onAction(SignUpScreenAction.OnPasswordChanged(it)) },
                    label = stringResource(R.string.auth_password),
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            if (canSubmit) onAction(SignUpScreenAction.OnSignUpClicked)
                        }
                    ),
                    autofillType = ContentType.NewPassword,
                    isPassword = true,
                    modifier = Modifier.focusRequester(passwordFocusRequester)
                )
                Spacer(Modifier.height(Space.xl))

                Button(
                    onClick = { onAction(SignUpScreenAction.OnSignUpClicked) },
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
                        text = stringResource(R.string.auth_signup_action),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(Modifier.height(Space.sm))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.auth_have_account),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onBackToLogin) {
                        Text(
                            text = stringResource(R.string.auth_login_action),
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
private fun SignUpContentPreview() {
    NotesTheme(darkTheme = false) {
        SignUpContent(
            state = SignUpScreenState(
                username = "new_reader",
                email = "reader@example.com",
                password = "secret"
            ),
            onAction = {},
            onBackToLogin = {}
        )
    }
}
