package com.mohammed.notes.feature.note.presentation.view_notes_screen.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohammed.notes.R
import com.mohammed.notes.feature.core.data.data_source.local.db.notes_db.entity.Note
import com.mohammed.notes.feature.core.presentation.util.formatTimestamp
import com.mohammed.notes.ui.theme.Primary

@Composable
fun NoteItem(
    note: Note,
    onClick: (Note) -> Unit,
    selectMode: Boolean,
    enableSelectMode: () -> Unit,
    selectedItems: List<Note>,
    onCheckedChange: (Boolean, Note) -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color = MaterialTheme.colorScheme.tertiary)
            .combinedClickable(
                onClick = {
                    if (selectMode){
                        onCheckedChange(!selectedItems.contains(note), note)
                    }else{
                        onClick(note)
                    }
                },
                onLongClick = {
                    enableSelectMode()
                    onCheckedChange(true, note)
                }
            )
            .padding(8.dp)
    ) {
        Text(
            text = note.title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = note.text,
            color = MaterialTheme.colorScheme.onTertiary,
            maxLines = 6,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                Text(
                    text = formatTimestamp(note.timestamp),
                    color = MaterialTheme.colorScheme.onTertiary,
                    fontSize = 10.sp
                )
                if(note.pinned) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.pin_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                    )
                }
            }
            AnimatedVisibility(selectMode) {
                Checkbox(
                    checked = selectedItems.contains(note),
                    onCheckedChange = {
                        onCheckedChange(it, note)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Primary
                    )
                )
            }
        }
    }
}