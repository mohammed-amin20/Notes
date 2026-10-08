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
    val category: String? = null,
    /**
     * Added in DB v3. Hidden notes are excluded from the public queries and keep their
     * title/text as opaque ciphertext. Room stores `Boolean` as `INTEGER 0`/`1`; the
     * migration defaults every existing row to 0 (visible), so nothing pre-existing changes.
     */
    val hidden: Boolean = false,
    /**
     * Present only on hidden rows: the GCM nonce that decrypts `text`. The ciphertext of
     * title+text lives in `text` (title is left empty) so a hidden row leaks nothing.
     */
    val enc_nonce: String? = null
)