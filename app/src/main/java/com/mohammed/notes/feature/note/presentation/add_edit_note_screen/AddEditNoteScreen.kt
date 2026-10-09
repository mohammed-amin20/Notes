package com.mohammed.notes.feature.note.presentation.add_edit_note_screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohammed.notes.R
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.NoteCategory
import com.mohammed.notes.feature.core.presentation.util.formatDateTime
import com.mohammed.notes.feature.privacy.presentation.SecureScreen
import com.mohammed.notes.feature.note.presentation.SharedViewModel
import com.mohammed.notes.feature.note.presentation.add_edit_note_screen.AddEditNoteScreenViewModel.UiAction
import com.mohammed.notes.ui.theme.Motion
import com.mohammed.notes.ui.theme.NotesTheme
import com.mohammed.notes.ui.theme.ReadableMeasure
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AddEditNoteScreen(
    goToHome: () -> Unit,
    goToHiddenNotes: () -> Unit,
    sharedViewModel: SharedViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: AddEditNoteScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

    val bodyFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.onAction(AddEditNoteScreenAction.OnNoteLoaded(sharedViewModel.note))
        viewModel.uiAction.collect { uiAction ->
            when (uiAction) {
                UiAction.OnBackNavigation -> goToHome()
                UiAction.AskDiscardConfirmation -> showDiscardDialog = true
                UiAction.SetupPinRequired -> goToHiddenNotes()
            }
        }
    }

    // Recreated after the setup gate closes: finishes a hide that was parked mid-edit.
    LaunchedEffect(Unit) { viewModel.onReturnedFromGate() }

    LaunchedEffect(state.hydrated, state.isNewNote) {
        if (state.hydrated && state.isNewNote) {
            bodyFocusRequester.requestFocus()
        }
    }

    BackHandler {
        viewModel.onAction(AddEditNoteScreenAction.OnBackClicked)
    }

    // Keyed off the origin note instead of hydration state: a vault note decrypts
    // asynchronously, and its card must still morph on the destination's first frame.
    val originNoteId = sharedViewModel.note?.id
    val sharedKey = if (originNoteId != null) "note-$originNoteId" else "note-new"
    val sharedModifier = with(sharedTransitionScope) {
        Modifier.sharedBounds(
            sharedContentState = rememberSharedContentState(key = sharedKey),
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = { _, _ -> tween(Motion.sharedBounds) }
        )
    }

    // Vault content stays out of screenshots while its note is on screen.
    if (state.hidden || sharedViewModel.note?.hidden == true) {
        SecureScreen()
    }

    AddEditNoteContent(
        state = state,
        showDiscardDialog = showDiscardDialog,
        onDismissDiscardDialog = { showDiscardDialog = false },
        onAction = { viewModel.onAction(it) },
        bodyFocusRequester = bodyFocusRequester,
        modifier = sharedModifier
    )
}

/**
 * Stateless editor body so the screen's infrastructure (navigation, vault gate, shared
 * element) stays out of @Preview. All edits funnel through [onAction]; the discard dialog
 * is owned by the caller and mirrored here through [showDiscardDialog].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddEditNoteContent(
    state: AddEditNoteScreenState,
    showDiscardDialog: Boolean,
    onDismissDiscardDialog: () -> Unit,
    onAction: (AddEditNoteScreenAction) -> Unit,
    bodyFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (state.isNewNote) R.string.editor_new_note else R.string.editor_edit_note
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onAction(AddEditNoteScreenAction.OnBackClicked)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Hidden notes are never pinned, so the toggle only exists outside the vault.
                    if (!state.hidden) {
                        IconButton(onClick = {
                            onAction(AddEditNoteScreenAction.OnPinToggled)
                        }) {
                            Icon(
                                painter = painterResource(
                                    if (state.pinned) R.drawable.ic_pin_24 else R.drawable.ic_pin_outline_24
                                ),
                                contentDescription = stringResource(
                                    if (state.pinned) R.string.action_unpin else R.string.action_pin
                                ),
                                tint = if (state.pinned) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                    if (state.hidden || state.hasContent) {
                        IconButton(onClick = {
                            onAction(
                                if (state.hidden) {
                                    AddEditNoteScreenAction.OnUnhideClicked
                                } else {
                                    AddEditNoteScreenAction.OnHideClicked
                                }
                            )
                        }) {
                            Icon(
                                painter = painterResource(
                                    if (state.hidden) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                                ),
                                contentDescription = stringResource(
                                    if (state.hidden) R.string.hidden_unhide else R.string.hidden_hide
                                ),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (state.hasContent) {
                        IconButton(onClick = {
                            onAction(AddEditNoteScreenAction.OnSaveClicked)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = stringResource(R.string.action_save),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = ReadableMeasure)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .then(modifier)
            ) {
                TextField(
                    value = state.title,
                    onValueChange = {
                        onAction(AddEditNoteScreenAction.OnTitleChanged(it))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.editor_title_hint),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = editorFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { bodyFocusRequester.requestFocus() }
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Space.xs, vertical = Space.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!state.isNewNote) {
                        Text(
                            text = stringResource(
                                R.string.editor_edited_at,
                                formatDateTime(context, state.timestamp)
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    Text(
                        text = pluralStringResource(
                            R.plurals.word_count,
                            state.wordCount,
                            state.wordCount
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "  ·  " + pluralStringResource(
                            R.plurals.characters_count,
                            state.charCount,
                            state.charCount
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                CategoryRow(
                    category = state.category,
                    onCategoryChange = {
                        onAction(AddEditNoteScreenAction.OnCategoryChanged(it))
                    }
                )

                TextField(
                    value = state.text,
                    onValueChange = {
                        onAction(AddEditNoteScreenAction.OnTextChanged(it))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp)
                        .focusRequester(bodyFocusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.editor_body_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = editorFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    )
                )
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = onDismissDiscardDialog,
            title = { Text(stringResource(R.string.editor_discard_title)) },
            text = { Text(stringResource(R.string.editor_discard_body)) },
            confirmButton = {
                TextButton(onClick = {
                    onDismissDiscardDialog()
                    onAction(AddEditNoteScreenAction.OnDiscardConfirmed)
                }) {
                    Text(
                        text = stringResource(R.string.action_discard),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDiscardDialog) {
                    Text(stringResource(R.string.action_keep_editing))
                }
            },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun CategoryRow(
    category: String?,
    onCategoryChange: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Space.lg, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.editor_category),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        listOf<Pair<String?, String>>(
            null to stringResource(R.string.category_none),
            NoteCategory.WORK.key to stringResource(R.string.filter_work),
            NoteCategory.PERSONAL.key to stringResource(R.string.filter_personal)
        ).forEach { (key, label) ->
            Spacer(Modifier.width(Space.sm))
            CategoryChip(
                label = label,
                selected = category == key,
                onSelect = { onCategoryChange(key) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onSelect,
        label = {
            Text(
                text = label,
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
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    )
}

@Composable
private fun editorFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.primary,
    selectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    )
)

// --- Previews --------------------------------------------------------------

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddEditNoteLightPreview() {
    NotesTheme(darkTheme = false) {
        AddEditNoteContent(
            state = previewEditorState(),
            showDiscardDialog = false,
            onDismissDiscardDialog = {},
            onAction = {},
            bodyFocusRequester = remember { FocusRequester() }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddEditNoteDarkPreview() {
    NotesTheme(darkTheme = true) {
        AddEditNoteContent(
            state = previewEditorState(),
            showDiscardDialog = false,
            onDismissDiscardDialog = {},
            onAction = {},
            bodyFocusRequester = remember { FocusRequester() }
        )
    }
}

/** Safe, synthetic content for previews — never real user data. */
private fun previewEditorState(): AddEditNoteScreenState {
    val now = System.currentTimeMillis()
    return AddEditNoteScreenState(
        title = "A walk by the river",
        text = "Cold morning, fog over the water. Jotting ideas for the weekend.",
        wordCount = 11,
        charCount = 64,
        timestamp = now - 3 * 60 * 60 * 1000L,
        isNewNote = false,
        hydrated = true,
        category = NoteCategory.WORK.key
    )
}