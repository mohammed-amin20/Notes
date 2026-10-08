package com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Upsert
    suspend fun upsertNote(note : Note)

    @Query("DELETE FROM note WHERE id IN (:ids)")
    suspend fun deleteNotes(ids: List<Int>)

    @Query("select * from note WHERE userId = :userId AND hidden = 0 ORDER BY timestamp")
    fun getAllNotes(userId: Int) : Flow<List<Note>>

    @Query("SELECT * FROM note WHERE userId = :userId AND hidden = 1 ORDER BY timestamp")
    fun getHiddenNotes(userId: Int): Flow<List<Note>>

    /** Used by the forgotten-PIN reset: their ciphertext dies with the data key. */
    @Query("DELETE FROM note WHERE userId = :userId AND hidden = 1")
    suspend fun deleteHiddenNotes(userId: Int)

    @Query("UPDATE note SET pinned = 1 WHERE id IN (:ids)")
    suspend fun pinNotes(ids: List<Int>)

    @Query("UPDATE note SET pinTimestamp = :timestamp WHERE id = :id")
    suspend fun setPinTimestamp(id: Int, timestamp: Long)

    @Query("UPDATE note SET pinned = 0 WHERE id IN (:ids)")
    suspend fun unpinNotes(ids: List<Int>)
}