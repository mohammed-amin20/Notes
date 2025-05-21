package com.mohammed.notes.feature.note.presentation.view_notes_screen

import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note

data class ViewNotesScreenState(
    val notes : List<Note> = emptyList<Note>(),
    val search : String = "",
    val dropDownMenuExpanded: Boolean = false,
    val searchList : List<Note> = emptyList(),
    val selectMode: Boolean = false,
    val selectedItems: List<Note> = emptyList(),
    val deleteDialogVisible: Boolean = false
)




















