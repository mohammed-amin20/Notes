package com.mohammed.notes.feature.note.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
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
import androidx.compose.ui.graphics.Color
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
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note
import com.mohammed.notes.feature.core.presentation.util.formatNoteDate
import com.mohammed.notes.ui.theme.Motion
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space
import com.mohammed.notes.ui.theme.accent
import com.mohammed.notes.ui.theme.rememberAnimationsEnabled
import java.time.LocalDate

/**
 * A note in the list.
 *
 * Long-press enters selection mode, where a 24dp circle sits in the card's bottom-end
 * corner. The timestamp row reserves that space at all times, so appearing, toggling, or
 * clearing selection never reflows the title, preview, or timestamp — the card keeps its
 * size, shape, padding, and text positions. Selected cards get a tinted container and a
 * primary border; the whole card (indicator included) is the tap target and toggles
 * selection once per tap without opening the note.
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

    // Selection highlight transitions with the indicator instead of snapping; the whole
    // set of selection animations is gated on the remove-animations accessibility setting.
    val animationsOn = rememberAnimationsEnabled()
    val cardColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = if (animationsOn) tween(Motion.enter) else snap(),
        label = "cardSelectionBg"
    )
    val cardBorderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = if (animationsOn) tween(Motion.enter) else snap(),
        label = "cardSelectionBorder"
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
                    modifier = Modifier.weight(1f)
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
                Spacer(Modifier.width(Space.xs))
                SelectionIndicator(visible = selectMode, selected = selected)
            }
        }
    }
}

/**
 * The select-mode circle parked in the card's bottom-end corner.
 *
 * Its slot is part of the timestamp row layout at all times, so only alpha, scale, and
 * color animate here — selection never moves text. Motion is tweened (no overshoot) and
 * snaps instantly when the system's remove-animations setting is on; cards composed
 * mid-scroll land on their final value because `animate*AsState` seeds from the first
 * target it sees rather than replaying an entrance.
 */
/** Shared with the hidden list's card so both lists animate selection identically. */
@Composable
internal fun SelectionIndicator(visible: Boolean, selected: Boolean) {
    val animationsOn = rememberAnimationsEnabled()

    val appear by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = if (animationsOn) tween(Motion.enter) else snap(),
        label = "selectionAppear"
    )
    val check by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = if (animationsOn) tween(Motion.enter) else snap(),
        label = "selectionCheck"
    )
    val circleColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        animationSpec = if (animationsOn) tween(Motion.enter) else snap(),
        label = "selectionCircle"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        },
        animationSpec = if (animationsOn) tween(Motion.enter) else snap(),
        label = "selectionBorder"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(Size.selectionIndicator)
            .graphicsLayer {
                alpha = appear
                scaleX = 0.85f + 0.15f * appear
                scaleY = 0.85f + 0.15f * appear
            }
    ) {
        Surface(
            shape = CircleShape,
            color = circleColor,
            border = BorderStroke(
                width = Size.hairline,
                color = borderColor
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (selected || check > 0f) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(Size.iconSm)
                            .graphicsLayer {
                                alpha = check
                                scaleX = 0.6f + 0.4f * check
                                scaleY = 0.6f + 0.4f * check
                            }
                    )
                }
            }
        }
    }
}
