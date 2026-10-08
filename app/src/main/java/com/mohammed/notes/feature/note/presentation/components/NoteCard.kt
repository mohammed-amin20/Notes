package com.mohammed.notes.feature.note.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.dp
import com.mohammed.notes.R
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note
import com.mohammed.notes.feature.core.presentation.util.formatNoteDate
import com.mohammed.notes.ui.theme.Motion
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space
import com.mohammed.notes.ui.theme.accent
import java.time.LocalDate

/**
 * A note in the list.
 *
 * Selection is shown with a tinted container, a border and a leading indicator. The old
 * card wrapped a 48dp Checkbox in `AnimatedVisibility` *inside* a `combinedClickable`
 * parent, which both reflowed the card on entering select mode and nested a second tap
 * target. Tapping the card already toggles selection in select mode, so the indicator is
 * purely visual now.
 *
 * [today] is the shared calendar day the date label buckets against; [labelTick] bumps
 * on resume and at local midnight so visible cards re-derive their label.
 */
@Composable
fun NoteCard(
    note: Note,
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
        label = "cardPress"
    )

    // A note saved with no title borrows its first line so the card is never blank.
    val hasTitle = note.title.isNotBlank()
    val displayTitle = if (hasTitle) {
        note.title
    } else {
        note.text.lineSequence().firstOrNull { it.isNotBlank() }.orEmpty()
    }
    val preview = if (hasTitle) {
        note.text
    } else {
        note.text.lines().drop(1).joinToString("\n").trimStart('\n')
    }
    val titleText = displayTitle.ifBlank { stringResource(R.string.note_untitled) }
    val timeText = remember(note.timestamp, today, labelTick) {
        formatNoteDate(context, note.timestamp, today)
    }
    val pinnedLabel = stringResource(R.string.cd_note_pinned)

    val description = buildString {
        append(titleText)
        append(", ")
        append(timeText)
        if (note.pinned) {
            append(", ")
            append(pinnedLabel)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            // A fixed height keeps every card in a row identical; scaling it with the system
            // font keeps two title lines + two preview lines + the timestamp inside at 1.3x.
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
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = BorderStroke(
            width = Size.hairline,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(Space.lg),
            verticalAlignment = Alignment.Top
        ) {
            SelectionIndicator(visible = selectMode, selected = selected)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
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
                // Absorbs the slack so the timestamp sits on the card's bottom edge no matter
                // how many title/preview lines were actually needed.
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        // Longer labels (date + year, Arabic) shrink to one line instead of
                        // wrapping over the card's fixed height or crowding the pin icon.
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (note.pinned) {
                        Spacer(Modifier.width(Space.sm))
                        Icon(
                            painter = painterResource(R.drawable.ic_pin_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.accent,
                            modifier = Modifier.size(Size.iconSm)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionIndicator(visible: Boolean, selected: Boolean) {
    val width by animateDpAsState(
        targetValue = if (visible) Size.selectionIndicatorWidth else 0.dp,
        animationSpec = spring(
            dampingRatio = Motion.spatialDamping,
            stiffness = Motion.spatialStiffness
        ),
        label = "selectionWidth"
    )
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(Motion.enter),
        label = "selectionAlpha"
    )
    if (width == 0.dp) return

    Row(modifier = Modifier.width(width)) {
        Spacer(Modifier.width(Space.xs))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .graphicsLayer { this.alpha = alpha }
                .size(width - Space.xs)
        ) {
            Surface(
                shape = CircleShape,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                border = BorderStroke(
                    width = Size.hairline,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(Size.iconMd)
                        )
                    }
                }
            }
        }
    }
}
