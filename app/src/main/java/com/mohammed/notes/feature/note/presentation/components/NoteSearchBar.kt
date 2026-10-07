package com.mohammed.notes.feature.note.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohammed.notes.R
import com.mohammed.notes.ui.theme.Motion
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space

/**
 * Persistent search pill. The old field hardcoded `Color.Gray` for its placeholder and
 * leading icon, which measured ~2.2:1 on the dark background, and had no way to clear
 * the query without selecting all and deleting.
 */
@Composable
fun NoteSearchBar(
    query: String,
    resultCount: Int?,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val placeholder = stringResource(R.string.action_search_notes)
    val clearLabel = stringResource(R.string.action_clear_search)
    val searchLabel = stringResource(R.string.action_search_notes)

    TextField(
        modifier = modifier.fillMaxWidth(),
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        shape = CircleShape,
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Default),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = searchLabel,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Size.iconMd)
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = clearLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(Size.iconMd)
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            // The spec's search surface: mint-tinted, so it reads as an input well against the
            // mist background instead of disappearing into it the way `surface` does in light.
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )

    AnimatedVisibility(
        visible = resultCount != null,
        enter = fadeIn(tween(Motion.enter)) + expandVertically(tween(Motion.enter)),
        exit = fadeOut(tween(Motion.exit)) + shrinkVertically(tween(Motion.exit))
    ) {
        val count = resultCount ?: 0
        Text(
            text = pluralStringResource(R.plurals.search_results, count, count),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Space.sm)
        )
    }
}
