package com.mohammed.notes.feature.core.data.data_source.local.shared_prefs

import android.content.Context
import androidx.core.content.edit

class NotesPrefs(
    context: Context,
) {
    private var prefs = context.getSharedPreferences("NotesPrefs" , Context.MODE_PRIVATE)

    fun setUserId(id : Int){
        prefs.edit {
            putInt("user_id", id)
        }
    }

    fun getUserId() : Int{
       return prefs.getInt("user_id", 0)
    }

    fun setLoggedIn (loggedIn : Boolean){
        prefs.edit{
            putBoolean("logged_in", loggedIn)
        }
    }

    fun getLoggedIn(): Boolean {
        return prefs.getBoolean("logged_in", false)
    }
}












