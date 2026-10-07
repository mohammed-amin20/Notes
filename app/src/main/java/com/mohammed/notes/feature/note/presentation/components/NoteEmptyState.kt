package com.mohammed.notes.feature.note.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohammed.notes.R
import com.mohammed.notes.ui.theme.Motion
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space
import com.mohammed.notes.ui.theme.accent

enum class EmptyReason { NO_NOTES, NO_RESULTS, NO_FILTER_MATCHES }

/**
 * The old list rendered nothing for "no notes yet", "no results" and (once the filter chips
 * landed) "nothing in this chip". These states are drawn from primitives so no image assets
 * are needed, and they crossfade so a change of reason reads as intentional.
 */
@Composable
fun NoteEmptyState(
    reason: EmptyReason,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = reason,
        transitionSpec = {
            (fadeIn(tween(Motion.enter)) togetherWith
                fadeOut(tween(Motion.exit))).using(SizeTransform(clip = false))
        },
        label = "emptyState",
        modifier = modifier.fillMaxSize()
    ) { current ->
        val titleRes = when (current) {
            EmptyReason.NO_NOTES -> R.string.empty_no_notes_title
            EmptyReason.NO_RESULTS -> R.string.empty_no_results_title
            EmptyReason.NO_FILTER_MATCHES -> R.string.empty_filter_title
        }
        val bodyRes = when (current) {
            EmptyReason.NO_NOTES -> R.string.empty_no_notes_body
            EmptyReason.NO_RESULTS -> R.string.empty_no_results_body
            EmptyReason.NO_FILTER_MATCHES -> R.string.empty_filter_body
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Space.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (current == EmptyReason.NO_RESULTS) EmptySearchArt() else NoteStackArt()
            Spacer(Modifier.height(Space.xl))
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Space.sm))
            Text(
                text = stringResource(bodyRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NoteStackArt() {
    val accent = MaterialTheme.colorScheme.accent
    Box(
        modifier = Modifier.size(width = 132.dp, height = 108.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(92.dp)
                .height(84.dp)
                .offset(x = 16.dp, y = (-8).dp),
            fill = MaterialTheme.colorScheme.surfaceContainerHigh,
            accent = accent,
            showBars = false
        )
        Card(
            modifier = Modifier
                .width(100.dp)
                .height(88.dp)
                .offset(x = (-8).dp, y = 8.dp),
            fill = MaterialTheme.colorScheme.surfaceContainerHighest,
            accent = accent,
            showBars = false
        )
        Card(
            modifier = Modifier
                .width(88.dp)
                .height(80.dp),
            fill = MaterialTheme.colorScheme.surfaceVariant,
            accent = accent,
            showBars = true,
            // The front card is the note-card role, which is white in light — the same hairline
            // that separates a real note from the background keeps it from dissolving here.
            border = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun Card(
    modifier: Modifier,
    fill: Color,
    accent: Color,
    showBars: Boolean,
    border: Color? = null
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .then(if (border != null) Modifier.border(Size.hairline, border, shape) else Modifier)
            .padding(Space.lg)
    ) {
        if (!showBars) return@Column
        listOf(1f, 1f, 0.55f).forEach { fraction ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .padding(bottom = Space.sm)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accent.copy(alpha = 0.4f))
            )
        }
    }
}

@Composable
private fun EmptySearchArt() {
    Box(
        modifier = Modifier.size(112.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        )
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.accent,
            modifier = Modifier.size(44.dp)
        )
    }
}
