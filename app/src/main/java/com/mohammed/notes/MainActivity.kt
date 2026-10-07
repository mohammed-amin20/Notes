package com.mohammed.notes

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.LocaleList
import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mohammed.notes.feature.auth.presentation.AuthNavHost
import com.mohammed.notes.feature.core.data.data_source.local.shared_prefs.NotesPrefs
import com.mohammed.notes.feature.note.presentation.NoteNavHost
import com.mohammed.notes.ui.locale.AppLocale
import com.mohammed.notes.ui.locale.AppLocaleController
import com.mohammed.notes.ui.locale.LocalAppLocaleController
import com.mohammed.notes.ui.theme.LocalThemeModeController
import com.mohammed.notes.ui.theme.NotesTheme
import com.mohammed.notes.ui.theme.ThemeMode
import com.mohammed.notes.ui.theme.ThemeModeController
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notesPrefs: NotesPrefs

    override fun attachBaseContext(newBase: Context) {
        // Hilt has not injected `notesPrefs` yet at this point, so read the same prefs file
        // directly. A null tag means "no explicit choice" and the device locale wins.
        val tag = NotesPrefs(newBase).getAppLocale()
        super.attachBaseContext(
            if (tag.isNullOrBlank() || tag == newBase.resources.configuration.locales[0].language) {
                newBase
            } else {
                newBase.withLocale(tag)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Before setContent: the decor view has to stop fitting system windows before the
        // first measure, otherwise the first frame lays out with the wrong insets.
        applyEdgeToEdge(resolveDarkTheme(ThemeMode.fromName(notesPrefs.getThemeMode())))

        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.fromName(notesPrefs.getThemeMode())) }
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Re-applies icon polarity when the user forces a scheme that disagrees with the
            // system night mode; without this a forced-light app on a dark system gets light
            // status-bar icons on a light background.
            LaunchedEffect(darkTheme) { applyEdgeToEdge(darkTheme) }

            val themeModeController = remember(themeMode) {
                ThemeModeController(themeMode) { next ->
                    themeMode = next
                    notesPrefs.setThemeMode(next.name)
                }
            }

            // Every tap records an explicit choice (so the device can never silently reclaim
            // the interface) but only an effective change needs the recreation that re-runs
            // `attachBaseContext` and re-reads all Resources, layouts and date formats.
            val localeController = remember {
                AppLocaleController { tag ->
                    notesPrefs.setAppLocale(tag)
                    if (tag != this@MainActivity.resources.configuration.locales[0].language) {
                        recreate()
                    }
                }
            }

            CompositionLocalProvider(
                LocalThemeModeController provides themeModeController,
                LocalAppLocaleController provides localeController
            ) {
                NotesTheme(darkTheme = darkTheme) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Route.AuthNavHost
                    ) {
                        composable<Route.AuthNavHost> {
                            AuthNavHost(
                                goToHome = {
                                    navController.popBackStack()
                                    navController.navigate(Route.NoteNavHost)
                                }
                            )
                        }
                        composable<Route.NoteNavHost> {
                            NoteNavHost(
                                goToLogin = {
                                    navController.popBackStack()
                                    navController.navigate(Route.AuthNavHost)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun resolveDarkTheme(mode: ThemeMode): Boolean = when (mode) {
        ThemeMode.SYSTEM -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    /**
     * `SystemBarStyle.auto` keeps `nightMode == MODE_NIGHT_AUTO`, which is what preserves the
     * platform's enforced navigation-bar contrast on API 29+; only `detectDarkMode` is
     * overridden, and it is the single thing that decides icon polarity.
     */
    private fun applyEdgeToEdge(isDark: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isDark },
            navigationBarStyle = SystemBarStyle.auto(
                Color.argb(0xe6, 0xFF, 0xFF, 0xFF),
                Color.argb(0x80, 0x1b, 0x1b, 0x1b)
            ) { isDark }
        )
    }
}

private sealed interface Route {
    @Serializable
    data object AuthNavHost : Route
    @Serializable
    data object NoteNavHost : Route
}

/**
 * Returns a new context whose resources serve [tag] (BCP-47) before the Activity attaches,
 * so every lookup — strings, plural rules, layout direction, date/measurement formats —
 * follows the stored pick. `createConfigurationContext` recreates the configuration on top
 * of the device one; nothing here registers the app with the platform per-app locale
 * service, so the stored value stays the single source of truth.
 */
// The lint findings about Play Core apply to locale-split App Bundles; this project builds
// a single APK, so the `setLocales` override above is the whole story.
@SuppressLint("AppBundleLocaleChanges")
private fun Context.withLocale(tag: String): Context {
    val locale = Locale.forLanguageTag(tag)
    Locale.setDefault(locale)
    val configuration = Configuration(resources.configuration)
    configuration.setLocales(LocaleList(locale))
    configuration.setLayoutDirection(locale)
    return createConfigurationContext(configuration)
}
