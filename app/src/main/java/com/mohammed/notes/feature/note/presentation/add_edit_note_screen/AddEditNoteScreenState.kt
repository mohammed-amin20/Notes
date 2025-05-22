package com.mohammed.notes.feature.note.presentation.add_edit_note_screen

data class AddEditNoteScreenState(
    val title : String = "",
    val text : String = "",
    val sizeOfText : Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
    val pinTimestamp: Long = Long.MIN_VALUE
)
