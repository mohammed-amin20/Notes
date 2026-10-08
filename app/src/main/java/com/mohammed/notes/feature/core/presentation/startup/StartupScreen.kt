package com.mohammed.notes.feature.core.presentation.startup

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohammed.notes.R
import com.mohammed.notes.ui.theme.NotesTheme
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space
import com.mohammed.notes.ui.theme.accent
import com.mohammed.notes.ui.theme.rememberAnimationsEnabled
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The Startup back-stack entry. Shows the branded splash while the session resolves
 * (a local store read, so a handful of milliseconds) and then hands off exactly once to
 * Auth or Notes through the callbacks. The failure case offers a localised Retry instead
 * of either an indefinite splash or a silent sign-out.
 *
 * The handoff waits out only the reveal itself — a fast resolution still plays the mark
 * in whole, a slow one fires the moment it lands — so there is never a held splash beyond
 * the animation and authenticated users crossfade straight to Notes without seeing Login.
 */
@Composable
fun StartupScreen(
    onSignedOut: () -> Unit,
    onAuthenticated: () -> Unit,
    viewModel: StartupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val failureMessage = stringResource(R.string.error_startup_failed)

    val animationsOn = rememberAnimationsEnabled()
    val startedAt = remember { SystemClock.elapsedRealtime() }
    // Reduced motion gets out of the way instantly — there is no reveal to finish.
    val splashFloor = if (animationsOn) REVEAL_MS else 0L

    when (val current = state) {
        StartupState.Loading -> StartupSplash()

        StartupState.SignedOut -> {
            LaunchedEffect(current) {
                holdSplash(splashFloor, startedAt)
                onSignedOut()
            }
            StartupSplash()
        }

        StartupState.Authenticated -> {
            LaunchedEffect(current) {
                holdSplash(splashFloor, startedAt)
                onAuthenticated()
            }
            StartupSplash()
        }

        StartupState.Failed -> StartupSplash(
            message = failureMessage,
            onRetry = viewModel::retry
        )
    }
}

private suspend fun holdSplash(floorMs: Long, startedAt: Long) {
    val remaining = floorMs - (SystemClock.elapsedRealtime() - startedAt)
    if (remaining > 0) delay(remaining)
}

/**
 * The branded loading surface: the approved Memo mark wipes up from the bottom, then
 * the app name lifts in beneath it — one clear sequence on the same background the
 * launch window opened with, then hands off through the host's crossfade. When system
 * animations are off (and in previews) it renders the finished state statically.
 */
@Composable
fun StartupSplash(
    modifier: Modifier = Modifier,
    message: String? = null,
    onRetry: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SplashMark()

            if (message != null) {
                Spacer(Modifier.height(Space.xl))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            if (onRetry != null) {
                Spacer(Modifier.height(Space.lg))
                Button(onClick = onRetry) {
                    Text(text = stringResource(R.string.action_retry))
                }
            }
        }
    }
}

/**
 * The reveal: a bottom-to-top wipe across the mark, then the app name lifting in.
 * Tied to the handoff floor above — when animations are off there is no sequence.
 */
@Composable
private fun SplashMark() {
    val animationsOn = rememberAnimationsEnabled()
    val inPreview = LocalInspectionMode.current
    val animate = animationsOn && !inPreview
    val reveal = remember { Animatable(if (animate) 0f else 1f) }
    val nameAlpha = remember { Animatable(if (animate) 0f else 1f) }
    val nameLift = remember { Animatable(if (animate) 1f else 0f) }
    val coverColor = MaterialTheme.colorScheme.background
    val liftPx = with(LocalDensity.current) { 8.dp.toPx() }

    LaunchedEffect(animate) {
        if (animate) {
            reveal.animateTo(1f, tween(WIPE_MS))
            coroutineScope {
                launch { nameAlpha.animateTo(1f, tween(NAME_MS)) }
                launch { nameLift.animateTo(0f, tween(NAME_MS)) }
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(R.drawable.memo_logo),
            contentDescription = null,
            modifier = Modifier
                .size(Size.splashLogo)
                .drawWithContent {
                    drawContent()
                    val progress = reveal.value
                    if (progress < 1f) {
                        drawRect(
                            color = coverColor,
                            size = androidx.compose.ui.geometry.Size(
                                size.width,
                                size.height * (1f - progress)
                            )
                        )
                    }
                },
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.accent)
        )

        Spacer(Modifier.height(Space.lg))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.graphicsLayer {
                alpha = nameAlpha.value
                translationY = nameLift.value * liftPx
            }
        )
    }
}

private const val WIPE_MS = 600
private const val NAME_MS = 300

/** The full reveal sequence — the handoff floor above waits out exactly this. */
private val REVEAL_MS = (WIPE_MS + NAME_MS).toLong()

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StartupSplashLightPreview() {
    NotesTheme(darkTheme = false) {
        StartupSplash(
            message = "Couldn't start Memo.",
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StartupSplashDarkPreview() {
    NotesTheme(darkTheme = true) {
        StartupSplash()
    }
}
