package com.mohammed.notes.feature.note.presentation.add_edit_note_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.mohammed.notes.ui.theme.Primary
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohammed.notes.feature.core.presentation.util.formatTimestamp
import com.mohammed.notes.feature.note.presentation.SharedViewModel
import com.mohammed.notes.feature.note.presentation.add_edit_note_screen.AddEditNoteScreenViewModel.UiAction


@Composable
fun AddEditNoteScreen(
    viewModel: AddEditNoteScreenViewModel = hiltViewModel(),
    goToHome: () -> Unit,
    sharedViewModel: SharedViewModel
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(true) {
        viewModel.uiAction.collect {
            when (it) {
                UiAction.OnBackNavigation -> {
                    goToHome()
                }
            }
        }
    }

    val note = sharedViewModel.note
    if (note != null){
        viewModel.apply {
            onAction(AddEditNoteScreenAction.OnTitleChanged(note.title))
            onAction(AddEditNoteScreenAction.OnTextChanged(note.text))
            onAction(AddEditNoteScreenAction.OnTimestampChanged(note.timestamp))
            onAction(AddEditNoteScreenAction.OnPinnedChanged(note.pinned))
            onAction(AddEditNoteScreenAction.OnPinTimestampChanged(note.pinTimestamp))
        }
    }

    val textFocusRequester = FocusRequester()
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        goToHome()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                if (state.value.text.isNotBlank() or state.value.title.isNotBlank())
                    IconButton(
                        onClick = {
                            viewModel.onAction(
                                AddEditNoteScreenAction.OnDoneClicked(note?.id)
                            )
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Done"
                        )
                    }
            }
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),

                ) {
                TextField(
                    value = state.value.title,
                    onValueChange = {
                        viewModel.onAction(AddEditNoteScreenAction.OnTitleChanged(it))
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        cursorColor = Primary,
                        selectionColors = TextSelectionColors(
                            handleColor = Primary,
                            backgroundColor = Primary.copy(0.4f)
                        ),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "Title",
                            color = Color.Gray.copy(.5f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            textFocusRequester.requestFocus()
                        }
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = formatTimestamp(state.value.timestamp),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "  |  ${state.value.text.length} characters",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                TextField(
                    value = state.value.text,
                    onValueChange = {
                        viewModel.onAction(AddEditNoteScreenAction.OnTextChanged(it))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .focusRequester(textFocusRequester),
                    colors = TextFieldDefaults.colors(
                        cursorColor = Primary,
                        selectionColors = TextSelectionColors(
                            handleColor = Primary,
                            backgroundColor = Primary.copy(0.4f)
                        ),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    placeholder = {
                        Text(
                            text = "Start Typing",
                            color = Color.Gray.copy(.2f),
                            fontSize = 16.sp
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                    )
                )
            }
        }
    }
}