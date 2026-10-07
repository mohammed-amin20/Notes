package com.mohammed.notes.feature.note.presentation.add_edit_note_screen

data class AddEditNoteScreenState(
    val title: String = "",
    val text: String = "",
    val charCount: Int = 0,
    val wordCount: Int = 0,
    val timestamp: Long = 0L,
    val pinned: Boolean = false,
    val pinTimestamp: Long = 0L,
    val noteId: Int? = null,
    val isNewNote: Boolean = true,
    val hydrated: Boolean = false,
    /** `null` is the uncategorised state and is a real value, not "unset". */
    val category: String? = null,
    /** Snapshot of what was loaded from storage, used to detect unsaved edits. */
    val loadedTitle: String = "",
    val loadedText: String = "",
    val loadedCategory: String? = null
) {
    val isDirty: Boolean
        get() = title != loadedTitle || text != loadedText || category != loadedCategory

    val hasContent: Boolean get() = title.isNotBlank() || text.isNotBlank()

    /** An untouched new note has nothing to lose, so it closes without a prompt. */
    val showDiscardPrompt: Boolean get() = isDirty && hasContent
}