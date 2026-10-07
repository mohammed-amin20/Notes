package com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity

/**
 * The two user-facing categories behind the list's Work / Personal filters.
 *
 * Rows store [key], a stable lowercase token, rather than the localized label — renaming a
 * chip or translating the app must never rewrite stored notes.
 */
enum class NoteCategory(val key: String) {
    WORK("work"),
    PERSONAL("personal");

    companion object {
        /** `null` (uncategorized, or an unknown legacy value) maps back to no selection. */
        fun fromKey(key: String?): NoteCategory? = entries.firstOrNull { it.key == key }
    }
}
