package com.mohammed.notes.feature.note.presentation.add_edit_note_screen

sealed interface AddEditNoteScreenAction {
    data class OnTitleChanged(val title : String): AddEditNoteScreenAction
    data class OnTextChanged(val text : String): AddEditNoteScreenAction
    data class OnTimestampChanged(val timestamp : Long): AddEditNoteScreenAction
    data class OnPinnedChanged(val pinned : Boolean): AddEditNoteScreenAction
    data class OnPinTimestampChanged(val timestamp : Long): AddEditNoteScreenAction
    data class OnDoneClicked(val id : Int?) : AddEditNoteScreenAction
}