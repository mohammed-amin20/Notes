package com.mohammed.notes.feature.privacy.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohammed.notes.R
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space

@Composable
fun PinDots(
    length: Int,
    filledCount: Int,
    hasError: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Space.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(length) { index ->
            val filled = index < filledCount
            val color = when {
                hasError -> MaterialTheme.colorScheme.error
                filled -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            Box(
                modifier = Modifier
                    .size(if (filled) 14.dp else 12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun PinKeypad(
    onDigit: (Char) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        val keys = listOf(
            listOf('1','2','3'),
            listOf('4','5','6'),
            listOf('7','8','9'),
            listOf('0')
        )
        keys.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Space.xl)) {
                row.forEach { d -> PinKey(d) { onDigit(d) } }
            }
            Spacer(Modifier.height(Space.lg))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Space.xl)) {
            PinKey('0') { onDigit('0') }
            PinTextKey(stringResource(android.R.string.cancel)) { onClear() }
        }
    }
}

@Composable
private fun PinKey(digit: Char, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(Size.touchTarget * 1.2f)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(digit.toString(), style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
private fun PinIconKey(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDesc: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(Size.touchTarget * 1.2f)) {
        Icon(icon, contentDescription = stringResource(contentDesc))
    }
}

@Composable
private fun PinTextKey(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(Size.touchTarget * 1.2f)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}