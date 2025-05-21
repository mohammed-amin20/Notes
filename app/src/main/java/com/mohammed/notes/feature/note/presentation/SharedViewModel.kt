package com.mohammed.notes.feature.note.presentation

import androidx.lifecycle.ViewModel
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note

class SharedViewModel : ViewModel() {
    var note : Note? = null
}