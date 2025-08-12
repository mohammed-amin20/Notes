package com.mohammed.notes.feature.auth.presentation.login_screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohammed.notes.feature.core.presentation.components.CustomTextField
import com.mohammed.notes.ui.theme.Primary

@Composable
fun LoginScreen(
    goToSignUp : () -> Unit,
    goToHome: () -> Unit,
    viewModel: LoginScreenViewModel = hiltViewModel()
) {
    val passwordFocusRequester = FocusRequester() // next
    val keyboardController = LocalSoftwareKeyboardController.current // done

    val context = LocalContext.current

    val state = viewModel.state.collectAsStateWithLifecycle()
    val uiAction = viewModel.uiAction

    if(state.value.loggedIn) {
        goToHome()
    }

    LaunchedEffect(true) {
        uiAction.collect{ action ->
            when(action){
                is LoginScreenViewModel.UiAction.ShowToast ->{
                    Toast.makeText(context, action.message, Toast.LENGTH_SHORT).show()
                }
                LoginScreenViewModel.UiAction.NavigateToHome -> {
                    goToHome()
                }
            }
        }
    }

    if(!state.value.loggedIn) {
        Scaffold (
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background
        ){ innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LOGIN",
                    fontSize = 32.sp,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CustomTextField(
                        value = state.value.username,
                        onValueChange = {
                            viewModel.onAction(LoginScreenAction.OnUsernameChange(it))
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                passwordFocusRequester.requestFocus()
                            }
                        ),
                        hint = "Username"
                    )
                    Spacer(Modifier.height(16.dp))
                    CustomTextField(
                        value = state.value.password,
                        onValueChange = {
                            viewModel.onAction(LoginScreenAction.OnPasswordChange(it))
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
                        hint = "Password",
                        modifier = Modifier
                            .focusRequester(passwordFocusRequester)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.onAction(LoginScreenAction.OnLoginClicked)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                    ) {
                        Text(
                            text = "Login"
                        )
                    }
                }
                Row {
                    Text(
                        text = "You don't have an account ",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Sign Up",
                        fontSize = 16.sp,
                        color = Primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                goToSignUp()
                            }
                    )
                }

            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LoginPreview() {
    LoginScreen(
        goToSignUp = {},
        goToHome = {}
    )
}