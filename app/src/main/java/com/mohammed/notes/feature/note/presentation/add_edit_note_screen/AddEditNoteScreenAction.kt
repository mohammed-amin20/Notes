package com.mohammed.notes.feature.note.presentation.add_edit_note_screen

import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note

sealed interface AddEditNoteScreenAction {
    /**
     * The note to edit arrives from `SharedViewModel`, which is a plain `viewModel()` and
     * therefore cannot be injected into this Hilt ViewModel. Passing it as a one-shot
     * action from a `LaunchedEffect` keeps hydration out of the composition phase, which
     * is where the previous implementation dispatched its state changes and could
     * overwrite what the user had already typed.
     */
    data class OnNoteLoaded(val note: Note?) : AddEditNoteScreenAction
    data class OnTitleChanged(val title: String) : AddEditNoteScreenAction
    data class OnTextChanged(val text: String) : AddEditNoteScreenAction
    data class OnCategoryChanged(val category: String?) : AddEditNoteScreenAction
    data object OnPinToggled : AddEditNoteScreenAction
    data object OnSaveClicked : AddEditNoteScreenAction
    data object OnBackClicked : AddEditNoteScreenAction
    data object OnDiscardConfirmed : AddEditNoteScreenAction
}