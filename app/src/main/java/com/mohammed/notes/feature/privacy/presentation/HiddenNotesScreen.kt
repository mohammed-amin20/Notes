package com.mohammed.notes.feature.privacy.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohammed.notes.R
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note
import com.mohammed.notes.feature.core.presentation.util.rememberDateLabelTick
import com.mohammed.notes.feature.note.presentation.components.SelectModeActionBar
import com.mohammed.notes.feature.note.presentation.components.SelectModeTopBar
import com.mohammed.notes.ui.theme.GridSingleColumnBelow
import com.mohammed.notes.ui.theme.GridSingleColumnFontScale
import com.mohammed.notes.ui.theme.Motion
import com.mohammed.notes.ui.theme.ReadableMeasure
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space
import com.mohammed.notes.ui.theme.accent
import com.mohammed.notes.ui.theme.listBottomClearance
import com.mohammed.notes.ui.theme.rememberAnimationsEnabled
import com.mohammed.notes.ui.theme.staggeredAppear
import java.time.LocalDate

/**
 * The private area's single route: picks the gate screen (set PIN, verify, open) from the
 * ViewModel so setup → verify → list transitions happen without a navigation entry per
 * beat, and locks the vault the moment the user backs out.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HiddenNotesRoute(
    onBack: () -> Unit,
    onOpenNote: (Note) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: HiddenNotesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state.gate) {
        HiddenNotesViewModel.Gate.SETUP -> PinSetupScreen(
            onDone = {
                viewModel.onUnlocked()
                onBack()
            },
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
            onOpenNote = onOpenNote,
            onToggleSelect = viewModel::toggleSelect,
            onSelectAll = viewModel::selectAll,
            onClearSelection = viewModel::clearSelection,
            onUnhide = viewModel::unhideSelected,
            onDelete = viewModel::deleteSelected,
            onReset = viewModel::resetPrivacy,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

/**
 * The vault list — the hidden twin of the note grid: same two-column card grid,
 * staggered entrance, select mode, and card-to-editor morph, with a branded lock bar
 * and vault-specific empty/broken states in place of the normal list's chrome.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun HiddenNotesScreen(
    onBack: () -> Unit,
    state: HiddenNotesViewModel.HiddenState,
    onOpenNote: (Note) -> Unit,
    onToggleSelect: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onUnhide: () -> Unit,
    onDelete: () -> Unit,
    onReset: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    SecureScreen()

    val animationsOn = rememberAnimationsEnabled()
    val gridState = rememberLazyGridState()
    val fontScale = LocalDensity.current.fontScale
    val labelTick = rememberDateLabelTick()
    val labelDay = remember(labelTick) { LocalDate.now() }

    // Select mode owns the back gesture, matching the normal list: one tap clears the
    // selection instead of closing the whole private area behind it.
    BackHandler(enabled = state.selectMode) { onClearSelection() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (state.selectMode) {
                SelectModeTopBar(
                    selectedCount = state.selected.size,
                    onClose = onClearSelection,
                    onToggleSelectAll = onSelectAll
                )
            } else {
                HiddenTopBar(onBack = onBack)
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = state.selectMode,
                enter = if (animationsOn) {
                    expandVertically(tween(Motion.enter)) + fadeIn(tween(Motion.enter))
                } else {
                    EnterTransition.None
                },
                exit = if (animationsOn) {
                    shrinkVertically(tween(Motion.exit)) + fadeOut(tween(Motion.exit))
                } else {
                    ExitTransition.None
                }
            ) {
                SelectModeActionBar(
                    selectedCount = state.selected.size,
                    allSelected = state.selected.size == state.items.size && state.items.isNotEmpty(),
                    onPinClick = onUnhide,
                    onDeleteClick = onDelete,
                    primaryLabel = stringResource(R.string.hidden_unhide),
                    primaryIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_visibility_off),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                )
            }
        }
    ) { inner ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                state.loading -> HiddenLoading(
                    modifier = Modifier.widthIn(max = ReadableMeasure)
                )

                state.items.isEmpty() -> HiddenEmptyState(
                    modifier = Modifier.widthIn(max = ReadableMeasure)
                )

                else -> {
                    val singleColumn = maxWidth < GridSingleColumnBelow ||
                        fontScale >= GridSingleColumnFontScale
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(if (singleColumn) 1 else 2),
                            modifier = Modifier
                                .widthIn(max = ReadableMeasure)
                                .fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = Space.lg,
                                end = Space.lg,
                                top = Space.xs,
                                bottom = listBottomClearance
                            ),
                            horizontalArrangement = Arrangement.spacedBy(Space.md),
                            verticalArrangement = Arrangement.spacedBy(Space.md)
                        ) {
                            items(
                                count = state.items.size,
                                key = { index -> state.items[index].id }
                            ) { index ->
                                val item = state.items[index]
                                val noteModifier = with(sharedTransitionScope) {
                                    hiddenNoteSharedBounds(item, animatedVisibilityScope)
                                }
                                HiddenNoteCard(
                                    item = item,
                                    today = labelDay,
                                    labelTick = labelTick,
                                    selected = item.id in state.selected,
                                    selectMode = state.selectMode,
                                    // Broken rows stay in the grid so they can be selected
                                    // and deleted, but there is no plaintext to open.
                                    onOpen = { if (!item.broken) onOpenNote(item.note) },
                                    onToggleSelect = { onToggleSelect(item.id) },
                                    onLongPress = { onToggleSelect(item.id) },
                                    modifier = noteModifier.staggeredAppear(index)
                                )
                            }
                        }
                        if (state.items.all { it.broken }) {
                            BrokenBanner(
                                onReset = onReset,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = Space.lg)
                                    .widthIn(max = ReadableMeasure)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * The vault's own bar — MemoTopBar's composition (back, two-line title stack, badge)
 * without the settings entry, plus a lock badge that says where the notes are.
 */
@Composable
private fun HiddenTopBar(onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.systemBars
                        .union(WindowInsets.displayCutout)
                        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )
                .padding(horizontal = Space.lg, vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(Space.xs))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.privacy_hidden_notes),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.hidden_top_bar_subtitle),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(Space.sm))
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(
                    width = Size.hairline,
                    color = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.size(Size.logoSize)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.accent,
                    modifier = Modifier.padding(Space.sm)
                )
            }
        }
    }
}

/** First decrypt after unlocking takes long enough to warrant a spinner, matching the note list. */
@Composable
private fun HiddenLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * Composed empty state in the NoteEmptyState idiom: a stack of ghost cards with a
 * locked face card, then title and body.
 */
@Composable
private fun HiddenEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(Space.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LockedCardArt()
        Spacer(Modifier.height(Space.xl))
        Text(
            text = stringResource(R.string.hidden_empty),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Space.sm))
        Text(
            text = stringResource(R.string.hidden_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** The note-stack art with a lock on the face card instead of the text bars. */
@Composable
private fun LockedCardArt() {
    Box(
        modifier = Modifier.size(width = 132.dp, height = 108.dp),
        contentAlignment = Alignment.Center
    ) {
        GhostCard(
            modifier = Modifier
                .width(92.dp)
                .height(84.dp)
                .offset(x = 16.dp, y = (-8).dp),
            fill = MaterialTheme.colorScheme.surfaceContainerHigh
        )
        GhostCard(
            modifier = Modifier
                .width(100.dp)
                .height(88.dp)
                .offset(x = (-8).dp, y = 8.dp),
            fill = MaterialTheme.colorScheme.surfaceContainerHighest
        )
        Box(
            modifier = Modifier
                .width(88.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    Size.hairline,
                    MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_lock_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.accent,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun GhostCard(modifier: Modifier, fill: Color) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(fill)
    )
}

/** Every row failed to decrypt — the only honest move is destroying the vault. */
@Composable
private fun BrokenBanner(modifier: Modifier = Modifier, onReset: () -> Unit) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = Size.hairline,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(Space.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_lock_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(Space.sm))
            Text(
                text = stringResource(R.string.hidden_broken),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Space.xs))
            TextButton(onClick = onReset) {
                Text(
                    text = stringResource(R.string.pin_reset_confirm),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.hiddenNoteSharedBounds(
    item: HiddenNotesViewModel.HiddenNoteUi,
    animatedVisibilityScope: AnimatedVisibilityScope
): Modifier {
    return Modifier.sharedBounds(
        sharedContentState = rememberSharedContentState(key = "note-${item.id}"),
        animatedVisibilityScope = animatedVisibilityScope,
        boundsTransform = { _, _ -> tween(Motion.sharedBounds) }
    )
}
