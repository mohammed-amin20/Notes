package com.mohammed.notes.feature.core.presentation.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd MMM hh:mm a", Locale.ENGLISH)
    return formatter.format(date)
}