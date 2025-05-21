package com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User (
    @PrimaryKey(autoGenerate = true)
    val id : Int? = null,
    val username : String ,
    val email : String,
    val password : String
)