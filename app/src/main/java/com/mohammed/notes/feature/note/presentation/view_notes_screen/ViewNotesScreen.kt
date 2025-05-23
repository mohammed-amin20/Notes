package com.mohammed.notes.feature.note.presentation.view_notes_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohammed.notes.R
import com.mohammed.notes.feature.note.presentation.SharedViewModel
import com.mohammed.notes.feature.note.presentation.view_notes_screen.componentes.NoteItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewNotesScreen(
    goToLogin: () -> Unit,
    goToAddEditNote: () -> Unit,
    viewModal: ViewNotesScreenViewModal = hiltViewModel(),
    sharedViewModel: SharedViewModel,
) {
    val listState = rememberLazyStaggeredGridState()
    val scope = rememberCoroutineScope()

    val state = viewModal.state.collectAsStateWithLifecycle()
    LaunchedEffect(true) {
        viewModal.uiAction.collect { uiAction ->
            when (uiAction) {
                ViewNotesScreenViewModal.UiAction.GotoLogin -> {
                    goToLogin()
                }
            }
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (!state.value.selectMode) {
                FloatingActionButton(
                    modifier = Modifier.padding(end = 16.dp),
                    shape = CircleShape,
                    onClick = {
                        sharedViewModel.note = null
                        goToAddEditNote()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }

        },
        bottomBar = {
            if (state.value.selectMode) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSecondary,
                                disabledContainerColor = Color.Transparent
                            ),
                            enabled = state.value.selectedItems.isNotEmpty()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Hide"
                                )
                                Text("Hide")
                            }

                        }
                        Button(
                            onClick = {
                                viewModal.apply {
                                    onAction(ViewNotesScreenAction.OnPinClick)
                                    onAction(ViewNotesScreenAction.OnSelectModeChange(false))
                                    onAction(ViewNotesScreenAction.OnSelectedItemsChange(emptyList()))
                                    scope.launch {
                                        listState.animateScrollToItem(0)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSecondary,
                                disabledContainerColor = Color.Transparent
                            ),
                            enabled = state.value.selectedItems.isNotEmpty()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if(state.value.selectedItems.isEmpty()) {
                                    Icon(
                                        painter = painterResource(R.drawable.pin_24),
                                        contentDescription = "Pin"
                                    )
                                    Text("Pin")
                                } else if (state.value.selectedItems.all { it.pinned }) {
                                    Icon(
                                        painter = painterResource(R.drawable.unpin),
                                        contentDescription = "unPin",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text("Unpin")
                                } else  {
                                    Icon(
                                        painter = painterResource(R.drawable.pin_24),
                                        contentDescription = "Pin"
                                    )
                                    Text("Pin")
                                }
                            }
                        }
                        Button(
                            enabled = state.value.selectedItems.isNotEmpty(),
                            onClick = {
                                viewModal.onAction(
                                    ViewNotesScreenAction.OnDeleteDialogVisibleChange(
                                        true
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSecondary,
                                disabledContainerColor = Color.Transparent
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                                Text("Delete")
                            }
                        }
                    }
                }
            }

        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            @OptIn(ExperimentalMaterial3Api::class)
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            if (state.value.deleteDialogVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        viewModal.onAction(ViewNotesScreenAction.OnDeleteDialogVisibleChange(false))
                    },
                    sheetState = sheetState,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    val size = state.value.selectedItems.size
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Deleting $size ${if (size == 1) "note" else "notes"}?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Are you sure you want to delete $size ${if (size == 1) "note" else "notes"}?",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModal.onAction(
                                        ViewNotesScreenAction.OnDeleteDialogVisibleChange(
                                            false
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    viewModal.onAction(ViewNotesScreenAction.OnDeleteNotesConfirmed)
                                    viewModal.onAction(
                                        ViewNotesScreenAction.OnDeleteDialogVisibleChange(
                                            false
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Delete")
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
            if (!state.value.selectMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(Modifier)
                    Row {
                        IconButton(
                            onClick = {},

                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        }
                        IconButton(
                            onClick = {},
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color.Gray
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "check"
                            )
                        }
                    }
                    Box {
                        IconButton(
                            onClick = {
                                viewModal.onAction(
                                    ViewNotesScreenAction.OnDropDownMenuExpandedChange(true)
                                )
                            },
                            colors = IconButtonDefaults.iconButtonColors(
//                                contentColor = Color.Gray
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Setting"
                            )
                        }
                        DropdownMenu(
                            expanded = state.value.dropDownMenuExpanded,
                            onDismissRequest = {
                                viewModal.onAction(
                                    ViewNotesScreenAction.OnDropDownMenuExpandedChange(false)
                                )
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Log out",
                                        color = Color.Red
                                    )
                                },
                                onClick = {
                                    viewModal.onAction(
                                        ViewNotesScreenAction.OnLogOut
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.ExitToApp,
                                        contentDescription = "Log out",
                                        tint = Color.Red
                                    )
                                }
                            )
                        }
                    }
                }
            }
            if (state.value.selectMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            viewModal.onAction(ViewNotesScreenAction.OnSelectModeChange(false))
                            viewModal.onAction(ViewNotesScreenAction.OnSelectedItemsChange(emptyList()))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                    Text(
                        text = "${state.value.selectedItems.size} item selected",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    IconButton(
                        onClick = {
                            if (state.value.selectedItems.size < state.value.notes.size) {
                                viewModal.onAction(
                                    ViewNotesScreenAction.OnSelectedItemsChange(state.value.notes)
                                )
                            } else {
                                viewModal.onAction(
                                    ViewNotesScreenAction.OnSelectedItemsChange(emptyList())
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.List,
                            contentDescription = "Select all",
                            tint = MaterialTheme.colorScheme.onSecondary

                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = state.value.search,
                    onValueChange = {
                        viewModal.onAction(ViewNotesScreenAction.OnSearchChanged(it))
                    },
                    placeholder = {
                        Text(
                            text = "Search notes",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray.copy(.8f)
                        )
                    },
                    shape = CircleShape,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Gray.copy(.2f),
                        unfocusedContainerColor = Color.Gray.copy(.1f)
//                        focusedContainerColor = MaterialTheme.colorScheme.surface,
//                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "search",
                            tint = Color.Gray.copy(.8f)
                        )
                    },
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .size(30.dp)
                            .background(color = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "All"
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .size(30.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_folder),
                            contentDescription = "folder",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth(),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    state = listState
                ) {
                    items(
                        if (state.value.searchList.isEmpty()) {
                            state.value.notes
                        } else {
                            state.value.searchList
                        },
                        key = { it.id!! }
                    ) { note ->
                        NoteItem(
                            note = note,
                            onClick = {
                                sharedViewModel.note = note
                                goToAddEditNote()
                            },
                            selectMode = state.value.selectMode,
                            enableSelectMode = {
                                viewModal.onAction(ViewNotesScreenAction.OnSelectModeChange(true))
                            },
                            selectedItems = state.value.selectedItems,
                            onCheckedChange = { isChecked, note ->
                                if (isChecked) {
                                    viewModal.onAction(
                                        ViewNotesScreenAction.OnSelectedItemsChange(
                                            state.value.selectedItems + note
                                        )
                                    )
                                } else {
                                    viewModal.onAction(
                                        ViewNotesScreenAction.OnSelectedItemsChange(
                                            state.value.selectedItems - note
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

