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
    val userId: Int,
    val pinned : Boolean = false,
    val pinTimestamp: Long,
    /**
     * Added in DB v2 by an `ALTER TABLE ... ADD COLUMN`, so it has to stay nullable with no
     * default: that is exactly what the migration produces. `null` means uncategorized, which
     * is what every pre-existing note reads as.
     */
    val category: String? = null
)