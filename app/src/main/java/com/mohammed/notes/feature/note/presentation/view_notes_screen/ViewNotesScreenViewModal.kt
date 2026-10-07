package com.mohammed.notes.feature.note.presentation.view_notes_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.NotesDB
import com.mohammed.notes.feature.core.data.data_source.local.shared_prefs.NotesPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ViewNotesScreenViewModal @Inject constructor(
    private val db: NotesDB,
    private val notesPrefs: NotesPrefs,
) : ViewModel() {
    private val _state = MutableStateFlow(ViewNotesScreenState())
    val state = _state.asStateFlow()

    /**
     * Retry trigger. The Store keeps its no-replay semantics (a retry past the first has
     * to re-read the DB anyway), so this is just a monotonic counter.
     */
    private val reload = MutableStateFlow(0)

    /**
     * Pinned notes keep their historical position at the head of the list (newest first
     * within each group) — the pinned rail is gone, but pinning still reorders the grid, so
     * a pinned note does not vanish into the middle of a date run.
     *
     * The Room flow is collected under a `flatMapLatest`: each retry drops the previous
     * subscription and reads a fresh flow. A raised error exposes the failure surface
     * instead of the blink of a false "no notes yet".
     */
    init {
        viewModelScope.launch {
            reload.flatMapLatest {
                // Flow.catch rethrows CancellationException on its own, so cancellation of
                // the previous subscription (a new retry, or the screen leaving) is never
                // mistaken for a load failure.
                db.noteDao.getAllNotes(notesPrefs.getUserId()).catch {
                    _state.update { it.copy(isLoading = false, loadFailed = true) }
                }
            }.collect { notes ->
                val pinnedNotes = notes.filter { it.pinned }.sortedBy { it.pinTimestamp }
                val unpinnedNotes = notes.filter { !it.pinned }
                _state.update {
                    it.copy(
                        notes = (unpinnedNotes + pinnedNotes).reversed(),
                        isLoading = false,
                        loadFailed = false
                    )
                }
            }
        }
    }

    fun onAction(action: ViewNotesScreenAction) {
        when (action) {
            is ViewNotesScreenAction.OnSearchChanged -> {
                _state.update { it.copy(search = action.search) }
            }

            is ViewNotesScreenAction.OnFilterChanged -> {
                _state.update { it.copy(filter = action.filter) }
            }

            is ViewNotesScreenAction.OnSelectModeChange -> {
                _state.update { it.copy(selectMode = action.selectMode) }
            }

            is ViewNotesScreenAction.OnSelectedItemsChange -> {
                _state.update { it.copy(selectedItems = action.selectedItems) }
            }

            is ViewNotesScreenAction.OnDeleteDialogVisibleChange -> {
                _state.update { it.copy(deleteDialogVisible = action.visible) }
            }

            ViewNotesScreenAction.OnDeleteNotesConfirmed -> {
                val doomed = _state.value.selectedItems
                if (doomed.isEmpty()) return
                viewModelScope.launch {
                    db.noteDao.deleteNotes(ids = doomed.map { it.id!! })
                    _state.update {
                        it.copy(
                            deleteDialogVisible = false,
                            selectedItems = emptyList(),
                            selectMode = false,
                            lastDeleted = doomed,
                            lastDeleteId = it.lastDeleteId + 1
                        )
                    }
                }
            }

            /**
             * Re-inserts the deleted rows with their original ids through the existing
             * `@Upsert`, so undo needs no schema change and no extra DAO query.
             */
            ViewNotesScreenAction.OnUndoDelete -> {
                val restore = _state.value.lastDeleted
                if (restore.isEmpty()) return
                viewModelScope.launch {
                    restore.forEach { db.noteDao.upsertNote(it) }
                    _state.update { it.copy(lastDeleted = emptyList()) }
                }
            }

            ViewNotesScreenAction.OnPinClick -> {
                val pinnedItems = _state.value.selectedItems.filter { it.pinned }
                val unpinnedItems = _state.value.selectedItems.filter { !it.pinned }

                viewModelScope.launch {
                    db.noteDao.apply {
                        if (unpinnedItems.isNotEmpty()) {
                            pinNotes(unpinnedItems.map { it.id!! })
                            unpinnedItems.forEach {
                                setPinTimestamp(it.id!!, System.currentTimeMillis())
                            }
                        } else {
                            unpinNotes(pinnedItems.map { it.id!! })
                        }
                    }
                }
            }

            ViewNotesScreenAction.OnRetryLoad -> {
                reload.value += 1
                _state.update { it.copy(isLoading = true, loadFailed = false) }
            }
        }
    }
}
