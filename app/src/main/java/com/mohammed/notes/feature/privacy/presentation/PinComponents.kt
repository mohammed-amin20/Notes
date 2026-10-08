package com.mohammed.notes.feature.privacy.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohammed.notes.R
import com.mohammed.notes.ui.theme.Motion
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.Space
import com.mohammed.notes.ui.theme.rememberAnimationsEnabled
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun PinDots(
    length: Int,
    filledCount: Int,
    hasError: Boolean,
    modifier: Modifier = Modifier
) {
    val animationsOn = rememberAnimationsEnabled()
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
            val animatedColor by animateColorAsState(
                targetValue = color,
                animationSpec = if (animationsOn) tween(Motion.enter) else snap(),
                label = "pinDotColor"
            )
            val dotSize by animateDpAsState(
                targetValue = if (filled) 14.dp else 12.dp,
                animationSpec = if (animationsOn) tween(Motion.enter) else snap(),
                label = "pinDotSize"
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(animatedColor)
            )
        }
    }
}

@Composable
fun PinKeypad(
    onDigit: (Char) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        val rows = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9')
        )
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Space.xl)) {
                row.forEach { d -> PinKey(d) { onDigit(d) } }
            }
            Spacer(Modifier.height(Space.lg))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Space.xl)) {
            PinTextKey(label = stringResource(R.string.pin_key_clear), onClick = onClear)
            PinKey('0') { onDigit('0') }
            PinTextKey(
                label = "⌫",
                contentDesc = stringResource(R.string.pin_backspace),
                onClick = onBackspace
            )
        }
    }
}

/**
 * Renders a keypad digit in the locale's numeral system (Arabic-Indic under an Arabic
 * locale, Latin otherwise). The keypad keeps emitting ASCII so PIN comparison and
 * storage stay independent of what the user sees — including leading zeros.
 */
private fun digitLabel(digit: Char): String {
    val zeroDigit = DecimalFormatSymbols(Locale.getDefault()).zeroDigit
    if (zeroDigit == '0') return digit.toString()
    return (zeroDigit + (digit - '0')).toString()
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
        Text(digitLabel(digit), style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
private fun PinTextKey(label: String, onClick: () -> Unit, contentDesc: String? = null) {
    Box(
        modifier = Modifier
            .size(Size.touchTarget * 1.2f)
            .then(if (contentDesc != null) Modifier.semantics { contentDescription = contentDesc } else Modifier)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}
