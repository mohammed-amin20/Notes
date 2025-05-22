package com.mohammed.notes.feature.note.presentation.add_edit_note_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.NotesDB
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note
import com.mohammed.notes.feature.core.data.data_source.local.shared_prefs.NotesPrefs
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
    val notesPrefs : NotesPrefs
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditNoteScreenState())
    val state = _state.asStateFlow()

    private val _uiAction = MutableSharedFlow<UiAction>()
    val uiAction = _uiAction.asSharedFlow()

    private var inProgress = false

    fun onAction(action: AddEditNoteScreenAction) {
        when (action) {
            is AddEditNoteScreenAction.OnTitleChanged -> {
                _state.update {
                    it.copy(
                        title = action.title
                    )
                }
            }

            is AddEditNoteScreenAction.OnTextChanged -> {
                _state.update {
                    it.copy(
                        text = action.text,
                        sizeOfText = action.text.length
                    )
                }
            }

            is AddEditNoteScreenAction.OnDoneClicked -> {
                if(!inProgress) {
                    inProgress = true
                    viewModelScope.launch {
                        val title = _state.value.title
                        val text = _state.value.text
                        if (text.isNotBlank() or title.isNotBlank()) {
                            db.noteDao.upsertNote(
                                Note(
                                    id = action.id,
                                    title = _state.value.title,
                                    text = _state.value.text,
                                    timestamp = System.currentTimeMillis(),
                                    userId = notesPrefs.getUserId(),
                                    pinned = _state.value.pinned,
                                    pinTimestamp = _state.value.pinTimestamp
                                )
                            )
                            _uiAction.emit(UiAction.OnBackNavigation)
                        }
                    }
                }
            }

            is AddEditNoteScreenAction.OnTimestampChanged -> {
                _state.update {
                    it.copy(
                        timestamp = action.timestamp
                    )
                }
            }

            is AddEditNoteScreenAction.OnPinnedChanged -> {
                _state.update {
                    it.copy(
                        pinned = action.pinned
                    )
                }
            }

            is AddEditNoteScreenAction.OnPinTimestampChanged -> {
                _state.update {
                    it.copy(
                        pinTimestamp = action.timestamp
                    )
                }
            }
        }
    }

    sealed interface UiAction {
        data object OnBackNavigation : UiAction
    }
}
