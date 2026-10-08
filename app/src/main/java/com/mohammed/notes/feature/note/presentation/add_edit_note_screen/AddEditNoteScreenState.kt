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
    val loadedCategory: String? = null,
    /**
     * The note being edited lives in the vault: it was decrypted on load and must be
     * re-encrypted on save, so a save can never silently move it into the visible list.
     */
    val hidden: Boolean = false,
    /**
     * Hide was pressed before a vault existed; the editor sends the user to the setup
     * gate and finishes the hide on their return instead of dropping the edit.
     */
    val hidePending: Boolean = false
) {
    val isDirty: Boolean
        get() = title != loadedTitle || text != loadedText || category != loadedCategory

    val hasContent: Boolean get() = title.isNotBlank() || text.isNotBlank()

    /** An untouched new note has nothing to lose, so it closes without a prompt. */
    val showDiscardPrompt: Boolean get() = isDirty && hasContent
}