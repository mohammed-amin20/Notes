package com.mohammed.notes.feature.privacy.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.mohammed.notes.R
import com.mohammed.notes.feature.core.presentation.util.formatNoteDate
import com.mohammed.notes.feature.note.presentation.components.SelectionIndicator
import com.mohammed.notes.ui.theme.Motion
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space
import com.mohammed.notes.ui.theme.accent
import com.mohammed.notes.ui.theme.rememberAnimationsEnabled
import java.time.LocalDate

/**
 * A vault note in the hidden grid — the hidden twin of [com.mohammed.notes.feature.note.presentation.components.NoteCard].
 *
 * Same anatomy: fixed height scaled with the system font, press-scale spring, animated
 * selection colors, and a fixed 24dp indicator slot in the timestamp row so toggling
 * selection never reflows the card. The vault flavor is the lock chip where the normal
 * card shows its pin, plus a broken variant: a row whose payload no longer decrypts
 * renders the error message instead of content, and stays selectable so it can be
 * deleted rather than opening into a screen full of gibberish.
 */
@Composable
internal fun HiddenNoteCard(
    item: HiddenNotesViewModel.HiddenNoteUi,
    today: LocalDate,
    labelTick: Int,
    selected: Boolean,
    selectMode: Boolean,
    onOpen: () -> Unit,
    onToggleSelect: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    maxTitleLines: Int = 2,
    maxBodyLines: Int = 2
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val fontScale = LocalDensity.current.fontScale
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Motion.emphasizedDamping,
            stiffness = Motion.emphasizedStiffness
        ),
        label = "hiddenCardPress"
    )

    val animationsOn = rememberAnimationsEnabled()
    val cardColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = if (animationsOn) tween(Motion.enter) else snap(),
        label = "hiddenCardSelectionBg"
    )
    val cardBorderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = if (animationsOn) tween(Motion.enter) else snap(),
        label = "hiddenCardSelectionBorder"
    )

    val titleText: String
    val preview: String
    if (item.broken) {
        titleText = stringResource(R.string.hidden_broken)
        preview = ""
    } else {
        val hasTitle = item.title.isNotBlank()
        val displayTitle = if (hasTitle) {
            item.title
        } else {
            item.snippet.lineSequence().firstOrNull { it.isNotBlank() }.orEmpty()
        }
        titleText = displayTitle.ifBlank { stringResource(R.string.note_untitled) }
        preview = if (hasTitle) {
            item.snippet
        } else {
            item.snippet.lines().drop(1).joinToString("\n").trimStart('\n')
        }
    }
    val timeText = remember(item.timestamp, today, labelTick) {
        formatNoteDate(context, item.timestamp, today)
    }

    val description = buildString {
        append(titleText)
        append(", ")
        append(timeText)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(Size.noteCardHeight * fontScale)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics(mergeDescendants = true) {
                contentDescription = description
                this.selected = selected
                role = Role.Button
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (selectMode) onToggleSelect() else onOpen()
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                }
            ),
        shape = MaterialTheme.shapes.large,
        color = cardColor,
        border = BorderStroke(
            width = Size.hairline,
            color = cardBorderColor
        )
    ) {
        Column(modifier = Modifier.padding(Space.lg)) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge,
                color = if (item.broken) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = maxTitleLines,
                overflow = TextOverflow.Ellipsis
            )
            if (preview.isNotBlank()) {
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = maxBodyLines,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(Space.sm))
                Icon(
                    painter = painterResource(R.drawable.ic_lock_24),
                    contentDescription = null,
                    tint = if (item.broken) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.accent
                    },
                    modifier = Modifier.size(Size.iconSm)
                )
                Spacer(Modifier.width(Space.xs))
                SelectionIndicator(visible = selectMode, selected = selected)
            }
        }
    }
}
