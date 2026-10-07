package com.mohammed.notes.ui.theme

import androidx.compose.ui.unit.dp

/** 4/8dp rhythm. Every gap in the app comes from here. */
object Space {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val huge = 48.dp
}

object Size {
    /** Android's minimum comfortable touch target. */
    val touchTarget = 48.dp
    val iconSm = 16.dp
    val iconMd = 20.dp
    val iconLg = 24.dp
    val hairline = 1.dp
    val buttonHeight = 52.dp

    /**
     * Chips carry the full touch target themselves, so they are 48dp tall, not the M3 32dp.
     */
    val filterChipHeight = 48.dp

    /** Feather mark beside the app title. */
    val logoSize = 40.dp

    /**
     * A grid card is a fixed height so every card in a row has the same surface — the grid
     * top-aligns unequal children instead of stretching them. Scales with the system font so
     * two title lines plus two preview lines plus the timestamp still fit at 1.3x.
     */
    val noteCardHeight = 176.dp

    /** Select-mode indicator: the 28dp circle plus its gap. The whole card is the tap target. */
    val selectionIndicatorWidth = 32.dp
}

/**
 * Long-form text stops being readable once the line runs much wider than this, so both the
 * note list and the editor cap their measure and centre it on large screens.
 */
val ReadableMeasure = 680.dp

/** Forms should not stretch across a tablet either. */
val FormMeasure = 420.dp

/**
 * Below this the note grid drops to one column — two 158dp cards do not fit a 320dp device.
 * The font-scale gate does the same thing for large accessibility text.
 */
val GridSingleColumnBelow = 340.dp
val GridSingleColumnFontScale = 1.3f

/** Keeps the last grid row clear of the FAB and the navigation bar. */
val listBottomClearance = 120.dp
