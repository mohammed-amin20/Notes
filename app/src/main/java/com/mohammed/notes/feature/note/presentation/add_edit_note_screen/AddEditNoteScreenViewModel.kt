package com.mohammed.notes.feature.note.presentation.add_edit_note_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.NotesDB
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note
import com.mohammed.notes.feature.core.data.data_source.local.shared_prefs.NotesPrefs
import com.mohammed.notes.feature.core.presentation.util.wordCount
import com.mohammed.notes.feature.core.security.PrivacyStore
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
    val notesPrefs: NotesPrefs,
    private val store: PrivacyStore
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

            AddEditNoteScreenAction.OnSaveClicked -> save(hidden = _state.value.hidden)

            AddEditNoteScreenAction.OnHideClicked -> hide()

            AddEditNoteScreenAction.OnUnhideClicked -> unhide()

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
        if (note != null && note.hidden) {
            hydrateHidden(note)
            return
        }
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

    /**
     * A vault note arrives as ciphertext, so it decrypts before hydrating: the editor
     * must never show the blob, and a failed decrypt (lost Keystore key) bails straight
     * back to the vault instead of presenting a note the user could then save over.
     */
    private fun hydrateHidden(note: Note) {
        viewModelScope.launch {
            val nonce = note.enc_nonce
            val content = if (nonce == null) null else runCatching {
                store.decryptHiddenContent(notesPrefs.getUserId(), note.text, nonce)
            }.getOrNull()
            if (content == null) {
                _uiAction.emit(UiAction.OnBackNavigation)
                return@launch
            }
            _state.update { current ->
                current.copy(
                    hydrated = true,
                    isNewNote = false,
                    noteId = note.id,
                    hidden = true,
                    title = content.title,
                    text = content.text,
                    loadedTitle = content.title,
                    loadedText = content.text,
                    category = note.category,
                    loadedCategory = note.category,
                    charCount = content.text.length,
                    wordCount = wordCount(content.text),
                    timestamp = note.timestamp,
                    pinned = note.pinned,
                    pinTimestamp = note.pinTimestamp
                )
            }
        }
    }

    /**
     * The editor's quick-hide. Without a vault the edit is parked in state and the user
     * is sent to setup; `onReturnedFromGate` finishes the job with whatever they typed,
     * so a round-trip through the PIN screen never costs them the note.
     */
    private fun hide() {
        if (_state.value.hidden) return
        if (!store.hasPin(notesPrefs.getUserId())) {
            _state.update { it.copy(hidePending = true) }
            viewModelScope.launch { _uiAction.emit(UiAction.SetupPinRequired) }
            return
        }
        save(hidden = true)
    }

    /**
     * The inverse of hide from inside the note: writes the current (possibly edited)
     * content back as a plain visible note. It runs unconditionally — even a note whose
     * body was emptied should leave the vault instead of being trapped behind the gate.
     */
    private fun unhide() {
        if (saving) return
        saving = true
        viewModelScope.launch {
            val current = _state.value
            try {
                if (current.noteId != null) {
                    db.noteDao.upsertNote(
                        Note(
                            id = current.noteId,
                            title = current.title.trim(),
                            text = current.text,
                            timestamp = current.timestamp,
                            userId = notesPrefs.getUserId(),
                            pinned = current.pinned,
                            pinTimestamp = current.pinTimestamp,
                            category = current.category,
                            hidden = false,
                            enc_nonce = null
                        )
                    )
                }
                _state.update { it.copy(hidePending = false) }
                _uiAction.emit(UiAction.OnBackNavigation)
            } finally {
                saving = false
            }
        }
    }

    fun onReturnedFromGate() {
        if (!_state.value.hidePending) return
        if (store.hasPin(notesPrefs.getUserId())) {
            save(hidden = true)
        } else {
            _state.update { it.copy(hidePending = false) }
        }
    }

    private fun save(hidden: Boolean) {
        if (saving) return
        saving = true
        viewModelScope.launch {
            val current = _state.value
            try {
                if (current.hydrated && current.hasContent) {
                    val userId = notesPrefs.getUserId()
                    val box = if (hidden) {
                        store.encryptHiddenContent(
                            userId,
                            current.title.trim(),
                            current.text
                        )
                    } else {
                        null
                    }
                    db.noteDao.upsertNote(
                        Note(
                            id = current.noteId,
                            title = if (hidden) "" else current.title.trim(),
                            text = box?.blobBase64 ?: current.text,
                            timestamp = current.timestamp,
                            userId = userId,
                            pinned = if (hidden) false else current.pinned,
                            pinTimestamp = if (hidden) 0L else current.pinTimestamp,
                            category = current.category,
                            hidden = hidden,
                            enc_nonce = box?.nonceBase64
                        )
                    )
                }
                _state.update { it.copy(hidePending = false) }
                _uiAction.emit(UiAction.OnBackNavigation)
            } finally {
                saving = false
            }
        }
    }

    sealed interface UiAction {
        data object OnBackNavigation : UiAction
        data object AskDiscardConfirmation : UiAction
        data object SetupPinRequired : UiAction
    }
}