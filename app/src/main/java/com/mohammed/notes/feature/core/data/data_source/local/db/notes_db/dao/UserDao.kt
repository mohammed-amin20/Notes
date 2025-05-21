package com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.User

@Dao
interface UserDao  {

    @Upsert
    suspend fun upsertUser(user : User)

    @Query("Select * from user Where username = :username and password = :password")
    suspend fun getUser(username : String , password : String) : User?

}