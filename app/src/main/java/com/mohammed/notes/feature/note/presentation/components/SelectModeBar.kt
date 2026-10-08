package com.mohammed.notes.feature.note.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohammed.notes.R
import com.mohammed.notes.ui.theme.Space
import com.mohammed.notes.ui.theme.accent
import com.mohammed.notes.ui.theme.disabledContent

/**
 * Contextual bar shown instead of the app bar while selecting.
 *
 * The previous version used filled `Button`s with a transparent container and no alpha
 * change when disabled, so Pin and Delete looked fully live with nothing selected. The
 * disabled state is explicit now, and the row mirrors for RTL automatically. Actions
 * size themselves from their content (with a comfortable minimum) so labels stay readable
 * at large system font scales instead of overflowing a fixed-height box.
 */
@Composable
fun SelectModeTopBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onToggleSelectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.systemBars
                        .union(WindowInsets.displayCutout)
                        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )
                .padding(horizontal = Space.sm, vertical = Space.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.action_clear_selection),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = pluralStringResource(R.plurals.selected_count, selectedCount, selectedCount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggleSelectAll) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = stringResource(R.string.action_select_all),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SelectModeActionBar(
    selectedCount: Int,
    allSelected: Boolean,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryLabel: String = stringResource(
        if (allSelected) R.string.action_unpin else R.string.action_pin
    ),
    primaryIcon: @Composable () -> Unit = {
        Icon(
            painter = painterResource(
                if (allSelected) R.drawable.ic_pin_24 else R.drawable.ic_pin_outline_24
            ),
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
    },
    /** Present only on the note list — the private area has nothing left to hide into. */
    onSecondaryClick: (() -> Unit)? = null,
    secondaryLabel: String = "",
    secondaryIcon: (@Composable () -> Unit)? = null
) {
    val enabled = selectedCount > 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.systemBars
                        .union(WindowInsets.displayCutout)
                        .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                )
                .padding(horizontal = Space.md, vertical = Space.sm),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SelectAction(
                label = primaryLabel,
                enabled = enabled,
                tint = MaterialTheme.colorScheme.accent,
                onClick = onPinClick,
                icon = primaryIcon
            )
            if (onSecondaryClick != null && secondaryIcon != null) {
                SelectAction(
                    label = secondaryLabel,
                    enabled = enabled,
                    tint = MaterialTheme.colorScheme.accent,
                    onClick = onSecondaryClick,
                    icon = secondaryIcon
                )
            }
            SelectAction(
                label = stringResource(R.string.action_delete),
                enabled = enabled,
                tint = MaterialTheme.colorScheme.error,
                onClick = onDeleteClick,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun SelectAction(
    label: String,
    enabled: Boolean,
    tint: Color,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val resolved = if (enabled) tint else MaterialTheme.colorScheme.disabledContent
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = Space.sm)
            .sizeIn(minWidth = 88.dp, minHeight = 64.dp)
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = resolved,
                disabledContentColor = MaterialTheme.colorScheme.disabledContent
            )
        ) {
            icon()
        }
        Spacer(Modifier.height(Space.xxs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = resolved
        )
    }
}
