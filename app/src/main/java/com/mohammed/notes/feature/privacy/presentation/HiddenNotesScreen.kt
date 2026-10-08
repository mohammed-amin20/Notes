package com.mohammed.notes.feature.privacy.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohammed.notes.R
import com.mohammed.notes.feature.note.presentation.components.SelectModeActionBar
import com.mohammed.notes.feature.note.presentation.components.SelectModeTopBar
import com.mohammed.notes.ui.theme.Space

/**
 * The private area's single route: picks the gate screen (set PIN, verify, open) from the
 * ViewModel so setup → verify → list transitions happen without a navigation entry per
 * beat, and locks the vault the moment the user backs out.
 */
@Composable
fun HiddenNotesRoute(
    onBack: () -> Unit,
    viewModel: HiddenNotesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state.gate) {
        HiddenNotesViewModel.Gate.SETUP -> PinSetupScreen(
            onDone = viewModel::onUnlocked,
            onCancel = onBack
        )

        HiddenNotesViewModel.Gate.VERIFY -> PinVerifyScreen(
            onVerified = viewModel::onUnlocked,
            onCancel = onBack,
            onResetDone = { viewModel.refreshGate() },
            showForgotPin = true
        )

        HiddenNotesViewModel.Gate.OPEN -> HiddenNotesScreen(
            onBack = { viewModel.onLeave(onBack) },
            state = state,
            onToggleSelect = viewModel::toggleSelect,
            onSelectAll = viewModel::selectAll,
            onClearSelection = viewModel::clearSelection,
            onUnhide = viewModel::unhideSelected,
            onDelete = viewModel::deleteSelected,
            onReset = viewModel::resetPrivacy
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HiddenNotesScreen(
    onBack: () -> Unit,
    state: HiddenNotesViewModel.HiddenState,
    onToggleSelect: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onUnhide: () -> Unit,
    onDelete: () -> Unit,
    onReset: () -> Unit
) {
    SecureScreen()

    Scaffold(
        topBar = {
            if (state.selectMode) {
                SelectModeTopBar(
                    selectedCount = state.selected.size,
                    onClose = onClearSelection,
                    onToggleSelectAll = onSelectAll
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.privacy_hidden_notes)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back)
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (state.selectMode) {
                SelectModeActionBar(
                    selectedCount = state.selected.size,
                    allSelected = state.selected.size == state.items.size && state.items.isNotEmpty(),
                    onPinClick = onUnhide,
                    onDeleteClick = onDelete,
                    primaryLabel = stringResource(R.string.hidden_unhide),
                    primaryIcon = { UnhideIcon() },
                )
            }
        }
    ) { inner ->
        if (state.items.isEmpty()) {
            EmptyHiddenState(modifier = Modifier.padding(inner))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(inner)) {
                items(state.items, key = { it.id }) { item ->
                    HiddenNoteRow(
                        item = item,
                        selected = item.id in state.selected,
                        onClick = { onToggleSelect(item.id) }
                    )
                }
            }
            if (state.items.all { it.broken }) {
                BrokenBanner(
                    modifier = Modifier.padding(inner),
                    onReset = onReset
                )
            }
        }
    }
}

@Composable
private fun UnhideIcon() {
    Icon(
        painter = painterResource(R.drawable.ic_visibility_off),
        contentDescription = null,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
private fun EmptyHiddenState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(Space.lg))
        Text(
            text = stringResource(R.string.hidden_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Every row failed to decrypt — the only honest move is destroying the vault. */
@Composable
private fun BrokenBanner(modifier: Modifier = Modifier, onReset: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Space.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.hidden_broken),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(Space.sm))
        Text(
            text = stringResource(R.string.pin_reset_confirm),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(Space.xs)
                .clickable(onClick = onReset)
        )
    }
}

@Composable
private fun HiddenNoteRow(
    item: HiddenNotesViewModel.HiddenNoteUi,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onClick)
            .padding(Space.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.broken) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(Space.sm))
            Text(
                text = stringResource(R.string.hidden_broken),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        } else {
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.title.ifBlank { stringResource(R.string.note_untitled) },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.snippet.isNotBlank()) {
                    Spacer(Modifier.height(Space.xxs))
                    Text(
                        text = item.snippet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (selected) {
                Spacer(Modifier.width(Space.sm))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
