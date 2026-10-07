package com.mohammed.notes.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Who decides `NotesTheme(darkTheme = ...)`. Stored as its own name in `NotesPrefs`, so a
 * restart lands on the same scheme the user picked.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        /** A missing or corrupt preference falls back to following the system. */
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

/**
 * Hoists the selection to `MainActivity` (which owns both the preference write and the
 * status/navigation bar icon polarity) so Settings can apply a change immediately without
 * threading two callbacks through two nav hosts.
 */
@Stable
class ThemeModeController(
    val mode: ThemeMode,
    private val onModeChange: (ThemeMode) -> Unit,
) {
    fun set(next: ThemeMode) {
        if (next != mode) onModeChange(next)
    }
}

val LocalThemeModeController = staticCompositionLocalOf<ThemeModeController> {
    error("No ThemeModeController provided. Wrap the tree in CompositionLocalProvider(LocalThemeModeController provides ...).")
}
