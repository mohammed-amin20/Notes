package com.mohammed.notes.feature.settings.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space

@Composable
fun SettingsRow(
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = Size.touchTarget)
            .selectable(selected = false, role = Role.Button, onClick = onClick)
            .padding(vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(Size.iconLg)) { icon() }
        Spacer(Modifier.width(Space.md))
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}