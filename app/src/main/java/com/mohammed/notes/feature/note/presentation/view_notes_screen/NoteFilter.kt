package com.mohammed.notes.feature.note.presentation.view_notes_screen

import androidx.annotation.StringRes
import com.mohammed.notes.R

/**
 * The four chips above the note grid. Order here is RTL reading order, so `ALL` renders
 * first (rightmost in Arabic).
 *
 * The label lives on the enum rather than in the composable so the chip row is a plain
 * `NoteFilter.entries.forEach` and the labels cannot drift from the filter they drive.
 */
enum class NoteFilter(@StringRes val labelRes: Int) {
    ALL(R.string.filter_all),
    PINNED(R.string.filter_pinned),
    WORK(R.string.filter_work),
    PERSONAL(R.string.filter_personal)
}
