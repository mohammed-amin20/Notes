package com.mohammed.notes.feature.core.data.data_source.local.db.notes_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        const val DB_VERSION = 2

        /**
         * v1 -> v2 adds `Note.category`.
         *
         * `ADD COLUMN category TEXT` produces precisely what the entity declares — TEXT,
         * nullable, no default — so Room's post-migration schema check passes, and because
         * the new column is nullable every existing row (and every pre-existing note, which
         * reads as uncategorized) survives untouched. Destructive fallback stays off.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `Note` ADD COLUMN category TEXT")
            }
        }
    }
}