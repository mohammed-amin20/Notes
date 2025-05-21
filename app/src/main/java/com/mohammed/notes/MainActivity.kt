package com.mohammed.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mohammed.notes.feature.auth.presentation.AuthNavHost
import com.mohammed.notes.feature.note.presentation.NoteNavHost
import com.mohammed.notes.ui.theme.NotesTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotesTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Route.AuthNavHost
                ) {
                    composable<Route.AuthNavHost> {
                        AuthNavHost (
                            goToHome = {
                                navController.popBackStack()
                                navController.navigate(Route.NoteNavHost)
                            }
                        )
                    }
                    composable<Route.NoteNavHost> {
                        NoteNavHost(
                            goToLogin = {
                                navController.popBackStack()
                                navController.navigate(Route.AuthNavHost)
                            }
                        )
                    }
                }
            }
        }
    }
}

private sealed interface Route {
    @Serializable
    data object AuthNavHost : Route
    @Serializable
    data object NoteNavHost : Route
}