package com.mohammed.notes.feature.note.presentation.view_notes_screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohammed.notes.R
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note
import com.mohammed.notes.feature.core.presentation.util.rememberDateLabelTick
import com.mohammed.notes.feature.note.presentation.SharedViewModel
import com.mohammed.notes.feature.note.presentation.components.EmptyReason
import com.mohammed.notes.feature.note.presentation.components.NoteCard
import com.mohammed.notes.feature.note.presentation.components.NoteEmptyState
import com.mohammed.notes.feature.note.presentation.components.NoteSearchBar
import com.mohammed.notes.feature.note.presentation.components.SelectModeActionBar
import com.mohammed.notes.feature.note.presentation.components.SelectModeTopBar
import com.mohammed.notes.ui.theme.GridSingleColumnBelow
import com.mohammed.notes.ui.theme.GridSingleColumnFontScale
import com.mohammed.notes.ui.theme.Motion
import com.mohammed.notes.ui.theme.NotesTheme
import com.mohammed.notes.ui.theme.ReadableMeasure
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space
import com.mohammed.notes.ui.theme.accent
import com.mohammed.notes.ui.theme.listBottomClearance
import com.mohammed.notes.ui.theme.rememberAnimationsEnabled
import com.mohammed.notes.ui.theme.staggeredAppear
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ViewNotesScreen(
    goToSettings: () -> Unit,
    goToAddEditNote: () -> Unit,
    goToHiddenNotes: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModal: ViewNotesScreenViewModal = hiltViewModel(),
    sharedViewModel: SharedViewModel,
) {
    val state by viewModal.state.collectAsStateWithLifecycle()

    // Setup→return is a navigation round-trip, so both halves live here: the event sends
    // the user away, the resume hook completes the hide they originally asked for.
    LaunchedEffect(Unit) {
        viewModal.events.collect { event ->
            when (event) {
                ViewNotesScreenViewModal.Event.SetupPinRequired -> goToHiddenNotes()
            }
        }
    }
    // The composable leaves composition while the gate is open and is recreated on
    // return, so this effect is exactly the "came back" hook that finishes a parked hide.
    LaunchedEffect(Unit) { viewModal.onReturnedFromGate() }

    ViewNotesContent(
        state = state,
        onAction = viewModal::onAction,
        onOpenNote = { note ->
            sharedViewModel.note = note
            goToAddEditNote()
        },
        onCreateNote = {
            sharedViewModel.note = null
            goToAddEditNote()
        },
        goToSettings = goToSettings,
        noteModifier = { note ->
            with(sharedTransitionScope) {
                noteSharedBounds(note, animatedVisibilityScope)
            }
        }
    )
}

/**
 * Stateless body so it can be rendered in @Preview without Hilt. The previous previews
 * called the screens with their default `hiltViewModel()`, which cannot resolve, so there
 * was effectively no design preview coverage at all.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewNotesContent(
    state: ViewNotesScreenState,
    onAction: (ViewNotesScreenAction) -> Unit,
    onOpenNote: (Note) -> Unit,
    onCreateNote: () -> Unit,
    goToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    noteModifier: @Composable (Note) -> Modifier = { Modifier }
) {
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val fontScale = LocalDensity.current.fontScale

    // One calendar-day snapshot shared by every card; the tick re-takes it when the app
    // resumes (overnight, or after a 12/24-hour change) and at local midnight.
    val labelTick = rememberDateLabelTick()
    val labelDay = remember(labelTick) { LocalDate.now() }

    // Back during selection clears it and stays on the list; the top-bar Close does the
    // same. A confirmation dialog, if open, consumes back first and just dismisses.
    BackHandler(enabled = state.selectMode) {
        onAction(ViewNotesScreenAction.OnSelectModeChange(false))
        onAction(ViewNotesScreenAction.OnSelectedItemsChange(emptyList()))
    }
    val animationsOn = rememberAnimationsEnabled()

    val deletedCount = state.lastDeleted.size
    val deletedMessage = pluralStringResource(R.plurals.notes_deleted, deletedCount, deletedCount)
    val undoLabel = stringResource(R.string.action_undo)

    val hiddenCount = state.lastHiddenCount
    val hiddenMessage = pluralStringResource(R.plurals.notes_hidden, hiddenCount, hiddenCount)
    val hiddenHint = stringResource(R.string.hidden_settings_hint)
    val hideSnackbarMessage =
        if (state.lastHiddenViaSetup) "$hiddenMessage\n$hiddenHint" else hiddenMessage

    LaunchedEffect(state.lastDeleteId) {
        if (deletedCount == 0) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = deletedMessage,
            actionLabel = undoLabel,
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            onAction(ViewNotesScreenAction.OnUndoDelete)
        }
    }

    LaunchedEffect(state.lastHiddenId) {
        if (hiddenCount == 0) return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = hideSnackbarMessage,
            duration = if (state.lastHiddenViaSetup) SnackbarDuration.Long else SnackbarDuration.Short
        )
    }

    val visibleNotes = state.visibleNotes
    val emptyReason: EmptyReason? = when {
        state.isLoading || state.loadFailed -> null
        visibleNotes.isNotEmpty() -> null
        state.notes.isEmpty() -> EmptyReason.NO_NOTES
        state.isSearching -> EmptyReason.NO_RESULTS
        else -> EmptyReason.NO_FILTER_MATCHES
    }

    Scaffold(
        modifier = modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (state.selectMode) {
                SelectModeTopBar(
                    selectedCount = state.selectedItems.size,
                    onClose = {
                        onAction(ViewNotesScreenAction.OnSelectModeChange(false))
                        onAction(ViewNotesScreenAction.OnSelectedItemsChange(emptyList()))
                    },
                    onToggleSelectAll = {
                        val pool = state.visibleNotes
                        val all = state.selectedItems.size == pool.size
                        if (all) {
                            // Deselecting everything also ends selection mode, matching
                            // what tapping the last card off does.
                            onAction(ViewNotesScreenAction.OnSelectedItemsChange(emptyList()))
                            onAction(ViewNotesScreenAction.OnSelectModeChange(false))
                        } else {
                            onAction(ViewNotesScreenAction.OnSelectedItemsChange(pool))
                        }
                    }
                )
            } else {
                MemoTopBar(onSettings = goToSettings)
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !state.selectMode,
                enter = fadeIn(tween(Motion.enter)) + scaleIn(tween(Motion.enter)),
                exit = fadeOut(tween(Motion.exit)) + scaleOut(tween(Motion.exit))
            ) {
                FloatingActionButton(
                    onClick = onCreateNote,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.action_new_note)
                    )
                }
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
                    selectedCount = state.selectedItems.size,
                    allSelected = state.allSelectedPinned,
                    onPinClick = {
                        onAction(ViewNotesScreenAction.OnPinClick)
                        onAction(ViewNotesScreenAction.OnSelectModeChange(false))
                        onAction(ViewNotesScreenAction.OnSelectedItemsChange(emptyList()))
                        scope.launch { gridState.animateScrollToItem(0) }
                    },
                    onDeleteClick = {
                        onAction(ViewNotesScreenAction.OnDeleteDialogVisibleChange(true))
                    },
                    onSecondaryClick = { onAction(ViewNotesScreenAction.OnHideNotesClick) },
                    secondaryLabel = stringResource(R.string.hidden_hide),
                    secondaryIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_lock_24),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!state.selectMode && !state.isLoading && !state.loadFailed) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = ReadableMeasure)
                            .fillMaxWidth()
                            .padding(horizontal = Space.lg, vertical = Space.md)
                    ) {
                        NoteSearchBar(
                            query = state.search,
                            resultCount = if (state.isSearching) state.visibleNotes.size else null,
                            onQueryChange = {
                                onAction(ViewNotesScreenAction.OnSearchChanged(it))
                            }
                        )
                    }

                    FilterChipRow(
                        selected = state.filter,
                        onSelect = { onAction(ViewNotesScreenAction.OnFilterChanged(it)) },
                        modifier = Modifier.widthIn(max = ReadableMeasure)
                    )

                    if (state.notes.isNotEmpty()) {
                        Spacer(Modifier.height(Space.lg))
                        NotesHeading(
                            count = state.filteredNotes.size,
                            modifier = Modifier
                                .widthIn(max = ReadableMeasure)
                                .fillMaxWidth()
                                .padding(horizontal = Space.lg)
                        )
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = if (state.selectMode) Space.xl else Space.md),
                contentAlignment = Alignment.TopCenter
            ) {
                when {
                    state.isLoading -> NotesLoading(
                        modifier = Modifier.widthIn(max = ReadableMeasure)
                    )

                    state.loadFailed -> NotesLoadError(
                        onRetry = { onAction(ViewNotesScreenAction.OnRetryLoad) },
                        modifier = Modifier.widthIn(max = ReadableMeasure)
                    )

                    emptyReason != null -> NoteEmptyState(
                        reason = emptyReason,
                        modifier = Modifier.widthIn(max = ReadableMeasure)
                    )

                    else -> {
                        val singleColumn = maxWidth < GridSingleColumnBelow ||
                            fontScale >= GridSingleColumnFontScale
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
                                count = visibleNotes.size,
                                key = { index -> visibleNotes[index].id ?: index }
                            ) { index ->
                                val note = visibleNotes[index]
                                NoteCard(
                                    note = note,
                                    today = labelDay,
                                    labelTick = labelTick,
                                    selected = note in state.selectedItems,
                                    selectMode = state.selectMode,
                                    onOpen = { onOpenNote(note) },
                                    onToggleSelect = { onToggleSelection(state, note, onAction) },
                                    onLongPress = { onLongPress(state, note, onAction) },
                                    modifier = noteModifier(note).staggeredAppear(index)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.deleteDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                onAction(ViewNotesScreenAction.OnDeleteDialogVisibleChange(false))
            },
            title = {
                Text(
                    text = pluralStringResource(
                        R.plurals.delete_dialog_title,
                        state.selectedItems.size,
                        state.selectedItems.size
                    )
                )
            },
            text = { Text(stringResource(R.string.delete_dialog_body)) },
            confirmButton = {
                TextButton(onClick = { onAction(ViewNotesScreenAction.OnDeleteNotesConfirmed) }) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onAction(ViewNotesScreenAction.OnDeleteDialogVisibleChange(false))
                }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

/**
 * The rebranded header. It is deliberately not a `TopAppBar`: that caps its own height at
 * the 64dp app-bar token and clips to it, which a two-line 30sp title cannot fit inside.
 * As the Scaffold's top bar slot it still sits above `innerPadding`, so it has to take the
 * status-bar inset itself — Scaffold only falls back to `contentWindowInsets.top` when the
 * slot is empty.
 *
 * In RTL the Row reverses, which puts the title at the far right, the feather immediately
 * beside it, and the gear at the far left (the trailing edge).
 */
@Composable
private fun MemoTopBar(onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = Space.lg, vertical = Space.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.notes_title),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.notes_subtitle),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(Space.sm))
        Image(
            painter = painterResource(R.drawable.memo_logo),
            contentDescription = null,
            modifier = Modifier.size(Size.logoSize),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.accent)
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onSettings) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.action_settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilterChipRow(
    selected: NoteFilter,
    onSelect: (NoteFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
        contentPadding = PaddingValues(horizontal = Space.lg)
    ) {
        items(NoteFilter.entries) { filter ->
            val isSelected = filter == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(filter) },
                label = {
                    Text(
                        text = stringResource(filter.labelRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier.heightIn(min = Size.filterChipHeight),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = BorderStroke(
                    width = Size.hairline,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                )
            )
        }
    }
}

@Composable
private fun NotesHeading(count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.notes_heading),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = pluralStringResource(R.plurals.notes_count, count, count),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun NotesLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun NotesLoadError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.error_notes_load_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Space.lg))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
}

private fun onToggleSelection(
    state: ViewNotesScreenState,
    note: Note,
    onAction: (ViewNotesScreenAction) -> Unit
) {
    val selected = state.selectedItems
    val next = if (note in selected) selected - note else selected + note
    onAction(ViewNotesScreenAction.OnSelectedItemsChange(next))
    // Clearing the last selected card ends selection mode instead of stranding an
    // empty top/bottom bar.
    if (next.isEmpty()) {
        onAction(ViewNotesScreenAction.OnSelectModeChange(false))
    }
}

private fun onLongPress(
    state: ViewNotesScreenState,
    note: Note,
    onAction: (ViewNotesScreenAction) -> Unit
) {
    onAction(ViewNotesScreenAction.OnSelectModeChange(true))
    if (note !in state.selectedItems) {
        onAction(ViewNotesScreenAction.OnSelectedItemsChange(state.selectedItems + note))
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.noteSharedBounds(
    note: Note,
    animatedVisibilityScope: AnimatedVisibilityScope
): Modifier {
    val id = note.id ?: return Modifier
    return Modifier.sharedBounds(
        sharedContentState = rememberSharedContentState(key = "note-$id"),
        animatedVisibilityScope = animatedVisibilityScope,
        boundsTransform = { _, _ -> tween(Motion.sharedBounds) }
    )
}

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

private fun previewNotes(): List<Note> {
    val now = System.currentTimeMillis()
    return listOf(
        Note(
            id = 1,
            title = "Weekly review",
            timestamp = now - 2 * HOUR,
            text = "Anything to follow up on from the last sprint?",
            userId = 1,
            pinned = true,
            pinTimestamp = now,
            category = "work"
        ),
        Note(
            id = 2,
            title = "",
            timestamp = now - 4 * HOUR,
            text = "Coffee with Sam at four.",
            userId = 1,
            pinned = true,
            pinTimestamp = now - HOUR
        ),
        Note(
            id = 3,
            title = "Grocery list",
            timestamp = now - 5 * HOUR,
            text = "Olive oil\nSourdough\nLemons\nCoffee beans",
            userId = 1,
            pinned = false,
            pinTimestamp = Long.MIN_VALUE,
            category = "work"
        ),
        Note(
            id = 4,
            title = "Reading notes",
            timestamp = now - 30 * HOUR,
            text = "Chapter three makes the same argument twice in two different ways.",
            userId = 1,
            pinned = false,
            pinTimestamp = Long.MIN_VALUE
        ),
        Note(
            id = 5,
            title = "Trip packing",
            timestamp = now - 3 * DAY,
            text = "Passport, charger, the blue jacket.",
            userId = 1,
            pinned = false,
            pinTimestamp = Long.MIN_VALUE
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ViewNotesLightPreview() {
    NotesTheme(darkTheme = false) {
        ViewNotesContent(
            state = ViewNotesScreenState(notes = previewNotes(), isLoading = false),
            onAction = {},
            onOpenNote = {},
            onCreateNote = {},
            goToSettings = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ViewNotesDarkPreview() {
    NotesTheme(darkTheme = true) {
        ViewNotesContent(
            state = ViewNotesScreenState(notes = previewNotes().reversed(), isLoading = false),
            onAction = {},
            onOpenNote = {},
            onCreateNote = {},
            goToSettings = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ViewNotesSearchNoResultsPreview() {
    NotesTheme(darkTheme = true) {
        ViewNotesContent(
            state = ViewNotesScreenState(
                notes = previewNotes(),
                search = "kubernetes",
                isLoading = false
            ),
            onAction = {},
            onOpenNote = {},
            onCreateNote = {},
            goToSettings = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ViewNotesEmptyPreview() {
    NotesTheme(darkTheme = false) {
        ViewNotesContent(
            state = ViewNotesScreenState(isLoading = false),
            onAction = {},
            onOpenNote = {},
            onCreateNote = {},
            goToSettings = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ViewNotesFilterNoMatchPreview() {
    NotesTheme(darkTheme = false) {
        ViewNotesContent(
            state = ViewNotesScreenState(
                notes = previewNotes(),
                filter = NoteFilter.PERSONAL,
                isLoading = false
            ),
            onAction = {},
            onOpenNote = {},
            onCreateNote = {},
            goToSettings = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ViewNotesSelectModePreview() {
    val notes = previewNotes()
    NotesTheme(darkTheme = true) {
        ViewNotesContent(
            state = ViewNotesScreenState(
                notes = notes,
                selectMode = true,
                selectedItems = notes.take(2),
                isLoading = false
            ),
            onAction = {},
            onOpenNote = {},
            onCreateNote = {},
            goToSettings = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ViewNotesSearchingPreview() {
    NotesTheme(darkTheme = false) {
        val notes = previewNotes()
        ViewNotesContent(
            state = ViewNotesScreenState(
                notes = notes,
                search = "list",
                isLoading = false
            ),
            onAction = {},
            onOpenNote = {},
            onCreateNote = {},
            goToSettings = {}
        )
    }
}
