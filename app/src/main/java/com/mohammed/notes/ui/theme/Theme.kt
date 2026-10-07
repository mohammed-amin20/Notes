package com.mohammed.notes.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Mint,
    onPrimary = MintInk,
    primaryContainer = MintContainerDark,
    onPrimaryContainer = OnMintContainerDark,
    inversePrimary = MintContainerDark,
    secondary = SlateInkMuted,
    onSecondary = SlateBackground,
    secondaryContainer = SlateSurfaceVariant,
    onSecondaryContainer = SlateInk,
    tertiary = Gold,
    onTertiary = MintInk,
    tertiaryContainer = GoldContainerDark,
    onTertiaryContainer = OnGoldContainerDark,
    background = SlateBackground,
    onBackground = SlateInk,
    surface = SlateSurface,
    onSurface = SlateInk,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = SlateInkMuted,
    surfaceContainerLowest = HarborContainerLowest,
    surfaceContainerLow = HarborContainerLow,
    surfaceContainer = SlateSurface,
    surfaceContainerHigh = HarborContainerHigh,
    surfaceContainerHighest = SlateSurfaceVariant,
    surfaceBright = HarborBright,
    surfaceDim = HarborDim,
    outline = SlateOutline,
    outlineVariant = SlateOutlineVariant,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    scrim = Color.Black,
    inverseSurface = HarborInverseSurface,
    inverseOnSurface = HarborInverseOnSurface,
)

private val LightColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = OnTeal,
    primaryContainer = MintContainerLight,
    onPrimaryContainer = OnMintContainerLight,
    inversePrimary = MintContainerLight,
    secondary = MistInkMuted,
    onSecondary = MistSurface,
    secondaryContainer = MistSurfaceVariant,
    onSecondaryContainer = MistInk,
    tertiary = GoldTextLight,
    onTertiary = MistSurface,
    tertiaryContainer = GoldContainerLight,
    onTertiaryContainer = OnGoldContainerLight,
    background = MistBackground,
    onBackground = MistInk,
    surface = MistSurface,
    onSurface = MistInk,
    surfaceVariant = MistSurfaceVariant,
    onSurfaceVariant = MistInkMuted,
    surfaceContainerLowest = CloudContainerLowest,
    surfaceContainerLow = CloudContainerLow,
    surfaceContainer = CloudContainer,
    surfaceContainerHigh = CloudContainerHigh,
    surfaceContainerHighest = CloudContainerHighest,
    surfaceBright = MistSurface,
    surfaceDim = CloudDim,
    outline = MistOutline,
    outlineVariant = MistOutlineVariant,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    scrim = Color.Black,
    inverseSurface = CloudInverseSurface,
    inverseOnSurface = CloudInverseOnSurface,
)

/**
 * The brand colour tuned to be legible *as text or an icon* on the current background:
 * mint on slate in dark, teal on white/mist in light. [primary] is a fill colour and is
 * not safe to use for small glyphs (white-on-teal is 4.87:1, teal-on-white is too).
 */
val ColorScheme.accent: Color
    get() = if (surface.luminance() > 0.5f) MintTextLight else MintTextDark

/**
 * Material 3 exposes no `onSurfaceDisabled` role in this version, so disabled content is
 * derived from the muted content role at the documented 38% alpha.
 */
val ColorScheme.disabledContent: Color
    get() = onSurfaceVariant.copy(alpha = 0.38f)

val ColorScheme.disabledContainer: Color
    get() = onSurface.copy(alpha = 0.12f)

@Composable
fun NotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Off by default: the mint-on-slate launcher icon is the app's identity, and dynamic
    // extraction replaced the palette on Android 12+ while screens kept hardcoding mint.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val base = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // A scheme change flips every token at once; interpolate them over Motion.enter so the
    // whole app dissolves between light and dark instead of popping. The platform "remove
    // animations" setting snaps the duration to zero.
    val animationsOn = rememberAnimationsEnabled()
    val colorScheme = base.withAnimatedTheme(if (animationsOn) Motion.enter else 0)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = NotesShapes,
        content = content
    )
}

/** Material3's `ColorScheme` exposes no animated helper, so return a `copy` of the target
 * scheme whose tokens are `animateColorAsState` backings. */
@Composable
private fun ColorScheme.withAnimatedTheme(durationMillis: Int): ColorScheme {
    val spec: FiniteAnimationSpec<Color> = if (durationMillis > 0) tween(durationMillis) else snap()
    return copy(
        primary = animateThemeColor(spec, "primary", primary),
        onPrimary = animateThemeColor(spec, "onPrimary", onPrimary),
        primaryContainer = animateThemeColor(spec, "primaryContainer", primaryContainer),
        onPrimaryContainer = animateThemeColor(spec, "onPrimaryContainer", onPrimaryContainer),
        inversePrimary = animateThemeColor(spec, "inversePrimary", inversePrimary),
        secondary = animateThemeColor(spec, "secondary", secondary),
        onSecondary = animateThemeColor(spec, "onSecondary", onSecondary),
        secondaryContainer = animateThemeColor(spec, "secondaryContainer", secondaryContainer),
        onSecondaryContainer = animateThemeColor(spec, "onSecondaryContainer", onSecondaryContainer),
        tertiary = animateThemeColor(spec, "tertiary", tertiary),
        onTertiary = animateThemeColor(spec, "onTertiary", onTertiary),
        tertiaryContainer = animateThemeColor(spec, "tertiaryContainer", tertiaryContainer),
        onTertiaryContainer = animateThemeColor(spec, "onTertiaryContainer", onTertiaryContainer),
        background = animateThemeColor(spec, "background", background),
        onBackground = animateThemeColor(spec, "onBackground", onBackground),
        surface = animateThemeColor(spec, "surface", surface),
        onSurface = animateThemeColor(spec, "onSurface", onSurface),
        surfaceVariant = animateThemeColor(spec, "surfaceVariant", surfaceVariant),
        onSurfaceVariant = animateThemeColor(spec, "onSurfaceVariant", onSurfaceVariant),
        surfaceTint = animateThemeColor(spec, "surfaceTint", surfaceTint),
        inverseSurface = animateThemeColor(spec, "inverseSurface", inverseSurface),
        inverseOnSurface = animateThemeColor(spec, "inverseOnSurface", inverseOnSurface),
        error = animateThemeColor(spec, "error", error),
        onError = animateThemeColor(spec, "onError", onError),
        errorContainer = animateThemeColor(spec, "errorContainer", errorContainer),
        onErrorContainer = animateThemeColor(spec, "onErrorContainer", onErrorContainer),
        outline = animateThemeColor(spec, "outline", outline),
        outlineVariant = animateThemeColor(spec, "outlineVariant", outlineVariant),
        scrim = animateThemeColor(spec, "scrim", scrim),
        surfaceBright = animateThemeColor(spec, "surfaceBright", surfaceBright),
        surfaceDim = animateThemeColor(spec, "surfaceDim", surfaceDim),
        surfaceContainer = animateThemeColor(spec, "surfaceContainer", surfaceContainer),
        surfaceContainerHigh = animateThemeColor(spec, "surfaceContainerHigh", surfaceContainerHigh),
        surfaceContainerHighest = animateThemeColor(spec, "surfaceContainerHighest", surfaceContainerHighest),
        surfaceContainerLow = animateThemeColor(spec, "surfaceContainerLow", surfaceContainerLow),
        surfaceContainerLowest = animateThemeColor(spec, "surfaceContainerLowest", surfaceContainerLowest),
    )
}

@Composable
private fun animateThemeColor(
    spec: FiniteAnimationSpec<Color>,
    label: String,
    target: Color,
): Color =
    animateColorAsState(targetValue = target, animationSpec = spec, label = label).value
