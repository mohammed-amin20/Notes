package com.mohammed.notes.feature.note.presentation.view_notes_screen

import  androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.NotesDB
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
class ViewNotesScreenViewModal @Inject constructor(
    val db: NotesDB,
    val notesPrefs: NotesPrefs,
) : ViewModel() {
    private val _state = MutableStateFlow(ViewNotesScreenState())
    val state = _state.asStateFlow()

    private val _uiAction = MutableSharedFlow<UiAction>()
    val uiAction = _uiAction.asSharedFlow()

    init {
        viewModelScope.launch {
            db.noteDao.getAllNotes(notesPrefs.getUserId()).collect { notes ->
                val pinnedNotes = notes.filter { it.pinned }.sortedBy { it.pinTimestamp }
                val unpinnedNotes = notes.filter { !it.pinned }
                val finalNotes = unpinnedNotes + pinnedNotes
                _state.update {
                    it.copy(
                        notes = finalNotes.reversed()
                    )
                }
            }
        }
    }

    fun onAction(action: ViewNotesScreenAction) {
        when (action) {
            is ViewNotesScreenAction.OnSearchChanged -> {
                _state.update {
                    it.copy(
                        search = action.search,
                        searchList = state.value.notes.filter {
                            it.title.contains(action.search) || it.text.contains(action.search)
                        }
                    )
                }
            }

            is ViewNotesScreenAction.OnDropDownMenuExpandedChange -> {
                _state.update {
                    it.copy(
                        dropDownMenuExpanded = action.expanded
                    )
                }
            }

            ViewNotesScreenAction.OnLogOut -> {
                notesPrefs.setLoggedIn(false)
                notesPrefs.setUserId(0)
                viewModelScope.launch {
                    _uiAction.emit(UiAction.GotoLogin)
                }
            }

            is ViewNotesScreenAction.OnSelectModeChange -> {
                _state.update {
                    it.copy(
                        selectMode = action.selectMode
                    )
                }
            }

            is ViewNotesScreenAction.OnSelectedItemsChange -> {
                _state.update {
                    it.copy(
                        selectedItems = action.selectedItems
                    )
                }
            }

            is ViewNotesScreenAction.OnDeleteDialogVisibleChange -> {
                _state.update {
                    it.copy(
                        deleteDialogVisible = action.visible
                    )
                }
            }

            ViewNotesScreenAction.OnDeleteNotesConfirmed -> {
                viewModelScope.launch {
                    db.noteDao.deleteNotes(
                        ids = _state.value.selectedItems.map { it.id!! }
                    )
                    _state.update {
                        it.copy(
                            deleteDialogVisible = false,
                            selectedItems = emptyList(),
                            selectMode = false
                        )
                    }
                }
            }

            ViewNotesScreenAction.OnPinClick -> {
                val pinnedItems = _state.value
                    .selectedItems
                    .filter { it.pinned }

                val unpinnedItems = _state.value
                    .selectedItems
                    .filter { !it.pinned }

                viewModelScope.launch {
                    db.noteDao.apply {
                        pinNotes(unpinnedItems.map { it.id!! })
                        unpinnedItems.forEach {
                            setPinTimestamp(it.id!!, System.currentTimeMillis())
                        }
                        if(unpinnedItems.isEmpty()) {
                            unpinNotes(pinnedItems.map { it.id!! })
                        }
                    }
                }
            }
        }
    }

    sealed interface UiAction {
        data object GotoLogin : UiAction
    }
}