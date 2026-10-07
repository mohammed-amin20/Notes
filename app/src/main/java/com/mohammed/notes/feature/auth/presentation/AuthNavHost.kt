package com.mohammed.notes.feature.auth.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mohammed.notes.feature.auth.presentation.login_screen.LoginScreen
import com.mohammed.notes.feature.auth.presentation.signup_screen.SignUpScreen
import com.mohammed.notes.ui.theme.Motion
import kotlinx.serialization.Serializable

@Composable
fun AuthNavHost(
    goToHome: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Route.LoginScreen,
        enterTransition = { fadeIn(tween(Motion.enter)) },
        exitTransition = { fadeOut(tween(Motion.exit)) },
        popEnterTransition = { fadeIn(tween(Motion.enter)) },
        /**
         * Sign up slides in from the trailing edge and leaves that way on back, so the
         * direction matches the reading order instead of cross-fading two forms.
         */
        popExitTransition = {
            slideOutHorizontally(tween(Motion.enter)) { it } + fadeOut(tween(Motion.exit))
        }
    ) {
        composable<Route.LoginScreen> {
            LoginScreen(
                goToSignUp = {
                    navController.navigate(Route.SignUpScreen)
                },
                goToHome = goToHome
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