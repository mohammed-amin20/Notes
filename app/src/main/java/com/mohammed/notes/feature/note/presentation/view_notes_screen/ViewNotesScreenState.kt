package com.mohammed.notes.feature.note.presentation.view_notes_screen

import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.NoteCategory

data class ViewNotesScreenState(
    val notes: List<Note> = emptyList(),
    val search: String = "",
    val filter: NoteFilter = NoteFilter.ALL,
    val selectMode: Boolean = false,
    val selectedItems: List<Note> = emptyList(),
    val deleteDialogVisible: Boolean = false,
    val lastDeleted: List<Note> = emptyList(),
    /** Bumped on every delete so the undo snackbar re-shows even for identical counts. */
    val lastDeleteId: Int = 0
) {
    val isSearching: Boolean get() = search.isNotBlank()

    /** What the active chip admits, before the query narrows it. */
    val filteredNotes: List<Note>
        get() = when (filter) {
            NoteFilter.ALL -> notes
            NoteFilter.PINNED -> notes.filter { it.pinned }
            NoteFilter.WORK -> notes.filter { it.category == NoteCategory.WORK.key }
            NoteFilter.PERSONAL -> notes.filter { it.category == NoteCategory.PERSONAL.key }
        }

    /**
     * The grid's contents. Search runs *inside* the active chip so switching filters while
     * a query is typed keeps the two consistent, and an empty result set stays empty rather
     * than silently falling back to every note.
     */
    val visibleNotes: List<Note>
        get() = if (isSearching) filteredNotes.filter { it.matches(search) } else filteredNotes

    val allSelectedPinned: Boolean
        get() = selectedItems.isNotEmpty() && selectedItems.all { it.pinned }
}

private fun Note.matches(query: String): Boolean =
    title.contains(query, ignoreCase = true) || text.contains(query, ignoreCase = true)
