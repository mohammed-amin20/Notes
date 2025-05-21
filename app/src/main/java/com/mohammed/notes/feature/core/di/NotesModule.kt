package com.mohammed.notes.feature.core.di

import android.app.Application
import androidx.room.Room
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.NotesDB
import com.mohammed.notes.feature.core.data.data_source.local.shared_prefs.NotesPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotesModule {
    @Provides
    @Singleton
    fun provideNotesDB(app : Application): NotesDB {
        return Room.databaseBuilder(
            app,
            NotesDB::class.java,
            NotesDB.DB_NAME
        )
//            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideNotesPrefs(app: Application) : NotesPrefs{
        return NotesPrefs(app)
    }
}