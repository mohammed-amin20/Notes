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

    @Query("select * from note WHERE userId = :userId")
    fun getAllNotes(userId: Int) : Flow<List<Note>>
}