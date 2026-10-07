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

    /**
     * Theme selection is a plain String on purpose: this class is the data layer and must
     * not import the Compose `ThemeMode` enum. `ThemeMode.fromName` maps it back.
     */
    fun setThemeMode(mode: String) {
        prefs.edit {
            putString("theme_mode", mode)
        }
    }

    fun getThemeMode(): String {
        return prefs.getString("theme_mode", null) ?: "SYSTEM"
    }

    /**
     * BCP-47 tag for the interface language, or `null` when the user has never picked one
     * and the device locale should win. Kept as a plain String for the same reason as the
     * theme: this is the data layer, and the tag is what `Configuration` consumes anyway.
     */
    fun setAppLocale(tag: String) {
        prefs.edit {
            putString(KEY_APP_LOCALE, tag)
        }
    }

    fun getAppLocale(): String? {
        return prefs.getString(KEY_APP_LOCALE, null)
    }

    private companion object {
        /** Read from `MainActivity.attachBaseContext`, before Hilt exists — same file name. */
        const val KEY_APP_LOCALE = "app_locale"
    }
}












