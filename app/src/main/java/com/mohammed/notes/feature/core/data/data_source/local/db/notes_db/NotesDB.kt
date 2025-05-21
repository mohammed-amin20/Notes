package com.mohammed.notes.feature.core.data.data_source.local.db.notes_db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.NotesDB.Companion.DB_VERSION
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.dao.NoteDao
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.dao.UserDao
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.User

@Database(
    entities = [User::class, Note::class],
    version = DB_VERSION
)
abstract class NotesDB : RoomDatabase(){

    abstract val userDao : UserDao
    abstract val noteDao : NoteDao

    companion object{
        const val DB_NAME = "notes_db"
        const val DB_VERSION = 1
    }
}