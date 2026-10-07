package com.mohammed.notes.feature.core.presentation.startup

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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohammed.notes.R
import com.mohammed.notes.ui.theme.Motion
import com.mohammed.notes.ui.theme.NotesTheme
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space
import com.mohammed.notes.ui.theme.accent
import com.mohammed.notes.ui.theme.rememberAnimationsEnabled
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * The Startup back-stack entry. Shows the branded splash while the session resolves
 * (a local store read, so a handful of milliseconds) and then hands off exactly once to
 * Auth or Notes through the callbacks. The failure case offers a localised Retry instead
 * of either an indefinite splash or a silent sign-out.
 */
@Composable
fun StartupScreen(
    onSignedOut: () -> Unit,
    onAuthenticated: () -> Unit,
    viewModel: StartupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val failureMessage = stringResource(R.string.error_startup_failed)

    when (val current = state) {
        StartupState.Loading -> StartupSplash()

        StartupState.SignedOut -> {
            LaunchedEffect(current) { onSignedOut() }
            StartupSplash()
        }

        StartupState.Authenticated -> {
            LaunchedEffect(current) { onAuthenticated() }
            StartupSplash()
        }

        StartupState.Failed -> StartupSplash(
            message = failureMessage,
            onRetry = viewModel::retry
        )
    }
}

/**
 * The branded loading surface. Background is the active theme's `background` — pale mist
 * with a teal feather in light, navy with a mint feather in dark — and the mark fades in
 * with a small 0.96 → 1.0 scale. When system animations are off it renders statically.
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

@Composable
private fun SplashMark() {
    val animationsOn = rememberAnimationsEnabled()
    val scale = remember { Animatable(0.96f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (animationsOn) {
            coroutineScope {
                launch { scale.animateTo(1f, tween(Motion.enter)) }
                launch { alpha.animateTo(1f, tween(Motion.enter)) }
            }
        } else {
            scale.snapTo(1f)
            alpha.snapTo(1f)
        }
    }

    Image(
        painter = painterResource(R.drawable.memo_logo_foreground),
        contentDescription = null,
        modifier = Modifier
            .size(Size.splashLogo)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            },
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.accent)
    )
}

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