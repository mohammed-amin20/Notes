package com.mohammed.notes.feature.core.presentation.util

import android.content.Context
import android.os.Build
import java.text.DateFormat
import java.util.Date

private fun currentLocale(context: Context) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale
    }

/** Time only — a note card carries a single timestamp and the grid has no date headers. */
fun formatTime(context: Context, timestamp: Long): String =
    DateFormat.getTimeInstance(DateFormat.SHORT, currentLocale(context))
        .format(Date(timestamp))

fun formatDateTime(context: Context, timestamp: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, currentLocale(context))
        .format(Date(timestamp))

/** Whitespace split works for both Latin and Arabic runs. */
fun wordCount(text: String): Int =
    if (text.isBlank()) 0 else text.trim().split(WHITESPACE).count { it.isNotBlank() }

private val WHITESPACE = Regex("\\s+")
