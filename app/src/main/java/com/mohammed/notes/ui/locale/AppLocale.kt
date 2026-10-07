package com.mohammed.notes.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration

/**
 * The two interface languages Memo ships. The settings screen renders their native names
 * from non-translatable resources and persists the BCP-47 tag below.
 */
object AppLocale {
    const val Arabic = "ar"
    const val English = "en"
}

/**
 * Hoists the language switch to `MainActivity`, which owns the preference write and the
 * `attachBaseContext` override that actually changes resources, layout direction and date
 * formats. Applying a locale requires a configuration change, so the controller persists
 * the pick and lets the activity recreate itself.
 */
@Stable
class AppLocaleController(
    private val onLocaleChange: (String) -> Unit,
) {
    fun set(tag: String) {
        onLocaleChange(tag)
    }
}

val LocalAppLocaleController = staticCompositionLocalOf<AppLocaleController> {
    error("No AppLocaleController provided. Wrap the tree in CompositionLocalProvider(LocalAppLocaleController provides ...).")
}

/**
 * The tag currently in effect for this configuration. The app only ever resolves to the two
 * languages it ships; anything else (an unsupported device locale when the user has not
 * chosen yet) falls back to English, which is the default resource locale.
 */
@Composable
fun currentAppLocale(): String {
    val configuration = LocalConfiguration.current
    val language = configuration.locales[0].language
    return if (language == AppLocale.Arabic) AppLocale.Arabic else AppLocale.English
}