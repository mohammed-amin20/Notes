package com.mohammed.notes.feature.note.presentation.add_edit_note_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.NotesDB
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note
import com.mohammed.notes.feature.core.data.data_source.local.shared_prefs.NotesPrefs
import com.mohammed.notes.feature.core.presentation.util.wordCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditNoteScreenViewModel @Inject constructor(
    val db: NotesDB,
    val notesPrefs: NotesPrefs
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditNoteScreenState())
    val state = _state.asStateFlow()

    private val _uiAction = MutableSharedFlow<UiAction>()
    val uiAction = _uiAction.asSharedFlow()

    private var saving = false

    fun onAction(action: AddEditNoteScreenAction) {
        when (action) {
            is AddEditNoteScreenAction.OnNoteLoaded -> hydrate(action.note)

            is AddEditNoteScreenAction.OnTitleChanged -> {
                _state.update { it.copy(title = action.title) }
            }

            is AddEditNoteScreenAction.OnTextChanged -> {
                _state.update {
                    it.copy(
                        text = action.text,
                        charCount = action.text.length,
                        wordCount = wordCount(action.text)
                    )
                }
            }

            is AddEditNoteScreenAction.OnCategoryChanged -> {
                _state.update { it.copy(category = action.category) }
            }

            AddEditNoteScreenAction.OnPinToggled -> {
                _state.update { current ->
                    val pinned = !current.pinned
                    current.copy(
                        pinned = pinned,
                        pinTimestamp = if (pinned && current.pinTimestamp == 0L) {
                            System.currentTimeMillis()
                        } else {
                            current.pinTimestamp
                        }
                    )
                }
            }

            AddEditNoteScreenAction.OnSaveClicked -> save()

            AddEditNoteScreenAction.OnBackClicked -> {
                if (_state.value.showDiscardPrompt) {
                    viewModelScope.launch { _uiAction.emit(UiAction.AskDiscardConfirmation) }
                } else {
                    viewModelScope.launch { _uiAction.emit(UiAction.OnBackNavigation) }
                }
            }

            AddEditNoteScreenAction.OnDiscardConfirmed -> {
                viewModelScope.launch { _uiAction.emit(UiAction.OnBackNavigation) }
            }
        }
    }

    /**
     * Loads the note exactly once. `timestamp` deliberately keeps the stored value:
     * the previous version wrote `System.currentTimeMillis()` on every save, so editing
     * an old note silently moved it to the top of the list.
     */
    private fun hydrate(note: Note?) {
        if (_state.value.hydrated) return
        _state.update { current ->
            if (note == null) {
                current.copy(
                    hydrated = true,
                    isNewNote = true,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                current.copy(
                    hydrated = true,
                    isNewNote = false,
                    noteId = note.id,
                    title = note.title,
                    text = note.text,
                    loadedTitle = note.title,
                    loadedText = note.text,
                    category = note.category,
                    loadedCategory = note.category,
                    charCount = note.text.length,
                    wordCount = wordCount(note.text),
                    timestamp = note.timestamp,
                    pinned = note.pinned,
                    pinTimestamp = note.pinTimestamp
                )
            }
        }
    }

    private fun save() {
        if (saving) return
        saving = true
        viewModelScope.launch {
            val current = _state.value
            try {
                if (current.hydrated && current.hasContent) {
                    db.noteDao.upsertNote(
                        Note(
                            id = current.noteId,
                            title = current.title.trim(),
                            text = current.text,
                            timestamp = current.timestamp,
                            userId = notesPrefs.getUserId(),
                            pinned = current.pinned,
                            pinTimestamp = current.pinTimestamp,
                            category = current.category
                        )
                    )
                }
                _uiAction.emit(UiAction.OnBackNavigation)
            } finally {
                saving = false
            }
        }
    }

    sealed interface UiAction {
        data object OnBackNavigation : UiAction
        data object AskDiscardConfirmation : UiAction
    }
}