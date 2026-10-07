package com.mohammed.notes.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Motion tokens. `LocalMotionDurationScale` is not exposed in this Compose version, so
 * the "remove animations" accessibility setting is read directly and every animated
 * helper in the app is gated on [rememberAnimationsEnabled].
 */
object Motion {
    const val enter = 220
    const val exit = 160
    const val staggerStep = 28
    const val staggerCap = 10
    const val sharedBounds = 280

    const val emphasizedDamping = 0.8f
    const val emphasizedStiffness = 400f
    const val spatialDamping = 0.9f
    const val spatialStiffness = 300f
}

@Composable
fun rememberAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) != 0f
        } catch (_: Exception) {
            true
        }
    }
}

private val AppearOffset = 28.dp

/**
 * Fades and lifts a list item in, offset by its position. Uses alpha and translation
 * only so it never animates the lazy layout's own bounds.
 */
@Composable
fun Modifier.staggeredAppear(index: Int): Modifier {
    val animationsOn = rememberAnimationsEnabled()
    var shown by remember { mutableStateOf(!animationsOn) }

    LaunchedEffect(index, animationsOn) {
        if (!animationsOn) {
            shown = true
            return@LaunchedEffect
        }
        shown = false
        val capped = minOf(index * Motion.staggerStep, Motion.staggerStep * Motion.staggerCap)
        delay(capped.toLong())
        shown = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(Motion.enter),
        label = "appearAlpha"
    )
    val lift by animateFloatAsState(
        targetValue = if (shown) 0f else 1f,
        animationSpec = tween(Motion.enter),
        label = "appearLift"
    )

    return this.graphicsLayer {
        this.alpha = alpha
        translationY = lift * AppearOffset.toPx()
    }
}
