package com.mohammed.notes.feature.auth.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mohammed.notes.feature.auth.presentation.login_screen.LoginScreen
import com.mohammed.notes.feature.auth.presentation.signup_screen.SignUpScreen
import kotlinx.serialization.Serializable

@Composable
fun AuthNavHost(
    goToHome: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Route.LoginScreen
    ) {
        composable<Route.LoginScreen> {
            LoginScreen(
                goToSignUp = {
                    navController.navigate(Route.SignUpScreen)
                },
                goToHome = {
                    goToHome()
                }
            )
        }
        composable<Route.SignUpScreen> {
            SignUpScreen(
                goToLogin = {
                    navController.navigateUp()
                }
            )
        }
    }
}


private sealed interface Route {
    @Serializable
    data object LoginScreen : Route

    @Serializable
    data object SignUpScreen : Route
}