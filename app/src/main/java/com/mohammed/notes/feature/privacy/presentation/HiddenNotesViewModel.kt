package com.mohammed.notes.feature.privacy.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.NotesDB
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note
import com.mohammed.notes.feature.core.data.data_source.local.shared_prefs.NotesPrefs
import com.mohammed.notes.feature.core.security.PrivacyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The private area: gate (set PIN → verify → open) plus the decrypted hidden list.
 *
 * Decryption happens only while [Gate.OPEN]; the hidden rows are fetched from Room but
 * never turned into plaintext until the in-memory lock says so, and a row whose payload
 * no longer decrypts (lost Keystore key) is surfaced as `broken` so the user can still
 * delete it instead of staring at a phantom.
 */
@HiltViewModel
class HiddenNotesViewModel @Inject constructor(
    private val db: NotesDB,
    private val notesPrefs: NotesPrefs,
    private val store: PrivacyStore,
    private val lock: PrivacyLockController
) : ViewModel() {

    private val _state = MutableStateFlow(HiddenState())
    val state = _state.asStateFlow()

    /** True once the vault's rows are decryptable this session, so later refreshes skip the spinner. */
    private var loaded = false

    private val userId: Int get() = notesPrefs.getUserId()

    init {
        _state.update { it.copy(gate = gateFor(lock.isUnlocked.value)) }

        viewModelScope.launch {
            lock.isUnlocked.collect { unlocked ->
                _state.update { it.copy(gate = gateFor(unlocked)) }
            }
        }

        viewModelScope.launch {
            combine(
                db.noteDao.getHiddenNotes(userId),
                _state.map { it.gate }.distinctUntilChanged()
            ) { notes, gate -> notes to gate }
                .collect { (notes, gate) ->
                    // Unlocking the vault decrypts every row in one go; until that first
                    // pass lands, the screen shows a spinner instead of a false "empty".
                    if (gate == Gate.OPEN && !loaded) {
                        _state.update { it.copy(loading = true) }
                    }
                    val decrypted = if (gate == Gate.OPEN) decryptAll(notes) else emptyList()
                    loaded = gate == Gate.OPEN
                    _state.update {
                        it.copy(
                            items = decrypted,
                            notes = notes,
                            loading = false
                        )
                    }
                }
        }
    }

    private fun gateFor(unlocked: Boolean): Gate = when {
        !store.hasPin(userId) -> Gate.SETUP
        !unlocked -> Gate.VERIFY
        else -> Gate.OPEN
    }

    /** Called after the PIN was chosen (or changed) — mirrors a successful verify. */
    fun onUnlocked() {
        lock.unlock()
    }

    fun refreshGate() {
        _state.update { it.copy(gate = gateFor(lock.isUnlocked.value)) }
    }

    /** Forgotten-PIN escape hatch: destroys the vault, then drops straight back to setup. */
    fun resetPrivacy() {
        viewModelScope.launch {
            store.resetPrivacy(userId)
            db.noteDao.deleteHiddenNotes(userId)
            lock.lock()
            refreshGate()
        }
    }

    /** Leaving the private area locks it; coming back re-asks for the PIN. */
    fun onLeave(onLeft: () -> Unit) {
        lock.lock()
        onLeft()
    }

    fun toggleSelect(id: Int) = _state.update {
        val selected = it.selected.toMutableSet().apply { if (!add(id)) remove(id) }
        it.copy(selected = selected, selectMode = selected.isNotEmpty())
    }

    fun selectAll() = _state.update {
        it.copy(selected = it.items.mapTo(HashSet()) { item -> item.id })
    }

    fun clearSelection() = _state.update { it.copy(selected = emptySet(), selectMode = false) }

    /** Decrypts back to plaintext and writes the rows as ordinary visible notes. */
    fun unhideSelected() {
        viewModelScope.launch {
            val ids = _state.value.selected
            _state.value.notes.filter { it.id in ids }.forEach { note ->
                val nonce = note.enc_nonce ?: return@forEach
                runCatching { store.decryptHiddenContent(userId, note.text, nonce) }
                    .onSuccess { content ->
                        db.noteDao.upsertNote(
                            note.copy(
                                title = content.title,
                                text = content.text,
                                hidden = false,
                                enc_nonce = null
                            )
                        )
                    }
            }
            clearSelection()
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            db.noteDao.deleteNotes(_state.value.selected.toList())
            clearSelection()
        }
    }

    private suspend fun decryptAll(notes: List<Note>): List<HiddenNoteUi> =
        notes.map { note ->
            val nonce = note.enc_nonce
            val content = if (nonce == null) null else runCatching {
                store.decryptHiddenContent(userId, note.text, nonce)
            }.getOrNull()
            HiddenNoteUi(
                id = requireNotNull(note.id),
                title = content?.title.orEmpty(),
                snippet = content?.text.orEmpty(),
                timestamp = note.timestamp,
                broken = content == null,
                note = note
            )
        }

    enum class Gate { SETUP, VERIFY, OPEN }

    data class HiddenNoteUi(
        val id: Int,
        val title: String,
        val snippet: String,
        val timestamp: Long,
        val broken: Boolean,
        val note: Note
    )

    data class HiddenState(
        val gate: Gate = Gate.SETUP,
        val notes: List<Note> = emptyList(),
        val items: List<HiddenNoteUi> = emptyList(),
        val selected: Set<Int> = emptySet(),
        val selectMode: Boolean = false,
        /** True while the vault is decrypting its rows for the first time after a lock. */
        val loading: Boolean = false
    )
}
