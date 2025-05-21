package com.mohammed.notes.feature.note.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mohammed.notes.feature.note.presentation.add_edit_note_screen.AddEditNoteScreen
import com.mohammed.notes.feature.note.presentation.view_notes_screen.ViewNotesScreen
import kotlinx.serialization.Serializable

@Composable
fun NoteNavHost(
    goToLogin: () -> Unit,
    sharedViewModel: SharedViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.ViewNotesScreen
    ) {
        composable<Route.ViewNotesScreen> {
            ViewNotesScreen(
                goToAddEditNote = {
                    navController.navigate(Route.AddEditNoteScreen)
                },
                goToLogin = {
                    goToLogin()
                },
                sharedViewModel = sharedViewModel
            )
        }
        composable<Route.AddEditNoteScreen> {
            AddEditNoteScreen(
                goToHome = {
                    navController.navigateUp()
                },
                sharedViewModel = sharedViewModel
            )
        }
    }
}

private sealed interface Route {
    @Serializable
    data object ViewNotesScreen : Route
    
    @Serializable
    data object AddEditNoteScreen : Route
}