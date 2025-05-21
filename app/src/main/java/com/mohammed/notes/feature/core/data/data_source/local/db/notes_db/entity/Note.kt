package com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id : Int? = null,
    val title: String,
    val timestamp: Long,
    val text : String,
    val userId: Int
)