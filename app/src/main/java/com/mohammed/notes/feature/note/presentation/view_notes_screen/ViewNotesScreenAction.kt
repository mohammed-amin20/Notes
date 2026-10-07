package com.mohammed.notes.feature.note.presentation.view_notes_screen

import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note

sealed interface ViewNotesScreenAction {
    data class OnSearchChanged(val search: String) : ViewNotesScreenAction
    data class OnFilterChanged(val filter: NoteFilter) : ViewNotesScreenAction
    data class OnSelectModeChange(val selectMode: Boolean) : ViewNotesScreenAction
    data class OnSelectedItemsChange(val selectedItems: List<Note>) : ViewNotesScreenAction
    data class OnDeleteDialogVisibleChange(val visible: Boolean) : ViewNotesScreenAction
    data object OnDeleteNotesConfirmed : ViewNotesScreenAction
    data object OnUndoDelete : ViewNotesScreenAction
    data object OnPinClick : ViewNotesScreenAction
    data object OnRetryLoad : ViewNotesScreenAction
}
