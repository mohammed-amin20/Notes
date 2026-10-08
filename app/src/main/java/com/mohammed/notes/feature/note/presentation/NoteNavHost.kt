package com.mohammed.notes.feature.note.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mohammed.notes.feature.note.presentation.add_edit_note_screen.AddEditNoteScreen
import com.mohammed.notes.feature.note.presentation.view_notes_screen.ViewNotesScreen
import com.mohammed.notes.feature.privacy.presentation.ChangePin
import com.mohammed.notes.feature.privacy.presentation.ChangePinRoute
import com.mohammed.notes.feature.privacy.presentation.HiddenNotes
import com.mohammed.notes.feature.privacy.presentation.HiddenNotesRoute
import com.mohammed.notes.feature.settings.presentation.SettingsScreen
import com.mohammed.notes.ui.theme.Motion
import kotlinx.serialization.Serializable

/**
 * The list and the editor share the card's bounds through one `SharedTransitionLayout`.
 * Each screen needs the same scope and its own `AnimatedVisibilityScope` (the
 * `AnimatedContentScope` Navigation provides per destination) to attach the modifier to
 * the matching note card.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NoteNavHost(
    goToLogin: () -> Unit,
    sharedViewModel: SharedViewModel = viewModel()
) {
    val navController = rememberNavController()

    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        val sharedTransitionScope = this

        NavHost(
            navController = navController,
            startDestination = Route.ViewNotesScreen,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { listEnter() },
            exitTransition = { listExit() },
            popEnterTransition = { editorEnter() },
            popExitTransition = { editorExit() }
        ) {
            composable<Route.ViewNotesScreen> {
                ViewNotesScreen(
                    goToAddEditNote = {
                        navController.navigate(Route.AddEditNoteScreen)
                    },
                    goToSettings = { navController.navigate(Route.Settings) },
                    goToHiddenNotes = { navController.navigate(HiddenNotes) },
                    sharedViewModel = sharedViewModel,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = this
                )
            }
            composable<Route.AddEditNoteScreen> {
                AddEditNoteScreen(
                    goToHome = {
                        navController.navigateUp()
                    },
                    goToHiddenNotes = { navController.navigate(HiddenNotes) },
                    sharedViewModel = sharedViewModel,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = this
                )
            }
            composable<Route.Settings> {
                SettingsScreen(
                    onBack = { navController.navigateUp() },
                    // Host-level: pops this graph and lands on the auth flow, which is what
                    // clears the authenticated back stack.
                    onLoggedOut = goToLogin,
                    onGoToHiddenNotes = { navController.navigate(HiddenNotes) },
                    onGoToChangePin = { navController.navigate(ChangePin) }
                )
            }
            composable<HiddenNotes> {
                HiddenNotesRoute(onBack = { navController.navigateUp() })
            }
            composable<ChangePin> {
                ChangePinRoute(
                    onDone = { navController.navigateUp() },
                    onCancel = { navController.navigateUp() }
                )
            }
        }
    }
}

/**
 * Transitions are deliberately gentle. The card-to-editor morph is carried by the shared
 * bounds themselves, so a large scale on top of it reads as a double animation.
 */
private fun listEnter(): EnterTransition =
    fadeIn(tween(Motion.enter))

private fun listExit(): ExitTransition =
    fadeOut(tween(Motion.exit))

private fun editorEnter(): EnterTransition =
    fadeIn(tween(Motion.enter)) + scaleIn(tween(Motion.enter), initialScale = 0.98f)

private fun editorExit(): ExitTransition =
    fadeOut(tween(Motion.exit)) + scaleOut(tween(Motion.exit), targetScale = 0.98f)

private sealed interface Route {
    @Serializable
    data object ViewNotesScreen : Route

    @Serializable
    data object AddEditNoteScreen : Route

    @Serializable
    data object Settings : Route
}