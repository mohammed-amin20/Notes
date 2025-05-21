//package com.mohammed.notes.feature.note.presentation.view_notes_screen
//
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.safeDrawing
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
//import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
//import androidx.compose.foundation.lazy.staggeredgrid.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ExitToApp
//import androidx.compose.material.icons.automirrored.filled.List
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Done
//import androidx.compose.material.icons.filled.Home
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.DropdownMenu
//import androidx.compose.material3.DropdownMenuItem
//import androidx.compose.material3.FloatingActionButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.IconButtonDefaults
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextField
//import androidx.compose.material3.TextFieldDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.mohammed.notes.R
//import com.mohammed.notes.feature.note.presentation.SharedViewModel
//import com.mohammed.notes.feature.note.presentation.view_notes_screen.componentes.NoteItem
//import com.mohammed.notes.ui.theme.Primary
//
//@Composable
//fun ViewNote(
//    goToAddEditNote: () -> Unit,
//    goToLogin: () -> Unit,
//    viewModal: ViewNotesScreenViewModal = hiltViewModel(),
//    sharedViewModel: SharedViewModel
//) {
//    val state = viewModal.state.collectAsStateWithLifecycle()
//    LaunchedEffect(true) {
//        viewModal.uiAction.collect { uiAction ->
//            when (uiAction) {
//                UiAction.GotoLogin -> {
//                    goToLogin()
//                }
//            }
//        }
//    }
//
//    Scaffold(
//        contentWindowInsets = WindowInsets.safeDrawing,
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = {
//                    sharedViewModel.note = null
//                    goToAddEditNote()
//                },
//                containerColor = Primary,
//                contentColor = Color.White
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = null
//                )
//            }
//        },
//        containerColor = Color.Black
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues = innerPadding)
//                .padding(16.dp)
//        ) {
//            if (state.value.deleteDialogVisible) {
//                AlertDialog(
//                    onDismissRequest = {
//                        viewModal.onAction(ViewNotesScreenAction.OnDeleteDialogVisibleChange(false))
//                    },
//                    confirmButton = {
//                        Button(
//                            onClick = {
//                                viewModal.onAction(ViewNotesScreenAction.OnDeleteNotesConfirmed)
//                            },
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Primary,
//                                contentColor = Color.White
//                            )
//                        ) {
//                            Text("Delete")
//                        }
//                    },
//                    dismissButton = {
//                        OutlinedButton(
//                            onClick = {
//                                viewModal.onAction(
//                                    ViewNotesScreenAction.OnDeleteDialogVisibleChange(
//                                        false
//                                    )
//                                )
//                            },
//                            colors = ButtonDefaults.outlinedButtonColors(
//                                contentColor = Primary
//                            ),
//                            border = BorderStroke(
//                                width = 1.dp,
//                                color = Primary
//                            )
//                        ) {
//                            Text("Cancel")
//                        }
//                    },
//                    title = {
//                        val size = state.value.selectedItems.size
//                        Text("Deleting $size ${if (size == 1) "note" else "notes"}?")
//                    },
//                    text = {
//                        val size = state.value.selectedItems.size
//                        Text("Are you sure you want to delete $size ${if (size == 1) "note" else "notes"}?\nIf you confirm this process, you can't undo it.")
//                    }
//                )
//            }
//            if (!state.value.selectMode) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Spacer(Modifier)
//                    Row {
//                        IconButton(
//                            onClick = {},
//                            colors = IconButtonDefaults.iconButtonColors(
//                                contentColor = Primary
//                            )
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Home,
//                                contentDescription = "Home"
//                            )
//                        }
//                        IconButton(
//                            onClick = {},
//                            colors = IconButtonDefaults.iconButtonColors(
//                                contentColor = Color.Gray
//                            )
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.CheckCircle,
//                                contentDescription = "check"
//                            )
//                        }
//                    }
//                    Box {
//                        IconButton(
//                            onClick = {
//                                viewModal.onAction(
//                                    ViewNotesScreenAction.OnDropDownMenuExpandedChange(
//                                        true
//                                    )
//                                )
//                            },
//                            colors = IconButtonDefaults.iconButtonColors(
//                                contentColor = if (state.value.dropDownMenuExpanded) Primary else Color.Gray
//                            )
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Settings,
//                                contentDescription = "Setting"
//                            )
//                        }
//                        DropdownMenu(
//                            expanded = state.value.dropDownMenuExpanded,
//                            onDismissRequest = {
//                                viewModal.onAction(
//                                    ViewNotesScreenAction.OnDropDownMenuExpandedChange(
//                                        false
//                                    )
//                                )
//                            }
//                        ) {
//                            DropdownMenuItem(
//                                text = {
//                                    Text(
//                                        text = "Log out"
//                                    )
//                                },
//                                onClick = {
//                                    viewModal.onAction(ViewNotesScreenAction.OnLogOut)
//                                },
//                                leadingIcon = {
//                                    Icon(
//                                        imageVector = Icons.AutoMirrored.Default.ExitToApp,
//                                        contentDescription = "Log out"
//                                    )
//                                }
//                            )
//                        }
//                    }
//                }
//            }
//
//            if (state.value.selectMode) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    IconButton(
//                        onClick = {},
//
//                        ) {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = "Cancel"
//                        )
//                    }
//                    Text(
//                        text = "${state.value.selectedItems.size} item selected",
//                        fontSize = 24.sp
//
//                    )
//                    IconButton(
//                        onClick = {},
//
//                        ) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Default.List,
//                            contentDescription = "Select all"
//                        )
//                    }
//                }
//                Row {
//                    if (state.value.selectedItems.isNotEmpty()) {
//                        IconButton(
//                            onClick = {
//                                viewModal.onAction(
//                                    ViewNotesScreenAction.OnDeleteDialogVisibleChange(
//                                        true
//                                    )
//                                )
//                            },
//                            colors = IconButtonDefaults.iconButtonColors(
//                                contentColor = Color.White
//                            )
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Delete,
//                                contentDescription = "Delete"
//                            )
//                        }
//                    }
//                    Spacer(Modifier.width(8.dp))
//                    IconButton(
//                        onClick = {
//                            viewModal.onAction(
//                                ViewNotesScreenAction.OnSelectModeChange(
//                                    false
//                                )
//                            )
//                            viewModal.onAction(
//                                ViewNotesScreenAction.OnSelectedItemsChange(
//                                    emptyList()
//                                )
//                            )
//                        },
//                        colors = IconButtonDefaults.iconButtonColors(
//                            contentColor = Primary
//                        )
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Done,
//                            contentDescription = "Done"
//                        )
//                    }
//                }
//            }
//        }
//        Column(
//            modifier = Modifier
//                .fillMaxWidth(),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            TextField(
//                modifier = Modifier.fillMaxWidth(),
//                value = state.value.search,
//                onValueChange = {
//                    viewModal.onAction(ViewNotesScreenAction.OnSearchChanged(it))
//                },
//                placeholder = {
//                    Text(
//                        text = "Search notes",
//                        color = Color.LightGray,
//                    )
//                },
//                shape = CircleShape,
//                colors = TextFieldDefaults.colors(
//                    focusedIndicatorColor = Color.Transparent,
//                    unfocusedIndicatorColor = Color.Transparent
//                ),
//                leadingIcon = {
//                    Icon(
//                        Icons.Default.Search,
//                        contentDescription = "search"
//                    )
//                },
//                singleLine = true
//            )
//
//            Row(
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Box(
//                    contentAlignment = Alignment.Center,
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(8.dp))
//                        .size(30.dp)
//                        .background(color = Color(0xFF262007).copy(0.1f))
//                ) {
//                    Text(
//                        text = "All",
//                        color = Color.White
//                    )
//                }
//                Spacer(modifier = Modifier.width(8.dp))
//                IconButton(
//                    onClick = {},
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(8.dp))
//                        .size(30.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(R.drawable.ic_folder),
//                        contentDescription = "folder",
//                        tint = Primary
//                    )
//                }
//            }
//
//            LazyVerticalStaggeredGrid(
//                columns = StaggeredGridCells.Fixed(2),
//                modifier = Modifier.fillMaxWidth(),
//                verticalItemSpacing = 8.dp,
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                items(
//                    if (state.value.searchList.isEmpty()) {
//                        state.value.notes
//                    } else {
//                        state.value.searchList
//                    },
//                    key = { it.id!! }
//                ) { note ->
//                    NoteItem(
//                        note = note,
//                        onClick = {
//                            sharedViewModel.note = note
//                            goToAddEditNote()
//                        },
//                        selectMode = state.value.selectMode,
//                        enableSelectMode = {
//                            viewModal.onAction(ViewNotesScreenAction.OnSelectModeChange(true))
//                        },
//                        selectedItems = state.value.selectedItems,
//                        onCheckedChange = { isChecked, note ->
//                            if (isChecked) {
//                                viewModal.onAction(
//                                    ViewNotesScreenAction.OnSelectedItemsChange(
//                                        state.value.selectedItems + note
//                                    )
//                                )
//                            } else {
//                                viewModal.onAction(
//                                    ViewNotesScreenAction.OnSelectedItemsChange(
//                                        state.value.selectedItems - note
//                                    )
//                                )
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
