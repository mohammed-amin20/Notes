package com.mohammed.notes.feature.core.presentation.util

import android.content.Context
import android.os.Build
import android.text.format.DateFormat as PlatformDateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mohammed.notes.R
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

private fun currentLocale(context: Context) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale
    }

/**
 * Note-card label: "Today, 10:32" and "Yesterday, 10:32" while the stamp falls in the
 * current day or the one before it, then day + abbreviated month inside the current
 * calendar year, then day + abbreviated month + year further back. [today] is the
 * caller's snapshot of the local day so every card buckets against the same day even
 * when the app crosses midnight between compositions. The timezone is the device's,
 * the locale the app's, and the time part honours the system 12/24-hour preference.
 *
 * The only content timestamp a note has is its creation time — there is no
 * last-modified field — so this labels `Note.timestamp`, exactly as the old
 * time-only label did, without changing what is stored or how notes are sorted.
 */
fun formatNoteDate(context: Context, timestamp: Long, today: LocalDate): String {
    val locale = currentLocale(context)
    val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    return when {
        date == today -> context.getString(
            R.string.date_today_time,
            formatClock(context, locale, timestamp)
        )

        // Checked before the year bucket so 31 Dec -> 1 Jan reads "Yesterday".
        date == today.minusDays(1) -> context.getString(
            R.string.date_yesterday_time,
            formatClock(context, locale, timestamp)
        )

        date.year == today.year -> SimpleDateFormat(
            PlatformDateFormat.getBestDateTimePattern(locale, "MMMd"),
            locale
        ).format(Date(timestamp))

        else -> SimpleDateFormat(
            PlatformDateFormat.getBestDateTimePattern(locale, "yMMMd"),
            locale
        ).format(Date(timestamp))
    }
}

fun formatDateTime(context: Context, timestamp: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, currentLocale(context))
        .format(Date(timestamp))

/**
 * Bumps whenever card labels must be re-derived: on resume (the app may have been
 * stopped across midnight, or the 12/24-hour preference may have changed in system
 * settings) and at local midnight while it stays open. The calendar day itself is
 * snapshotted separately so all cards share one.
 */
@Composable
fun rememberDateLabelTick(): Int {
    val lifecycleOwner = LocalLifecycleOwner.current
    var tick by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) tick++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) {
        while (true) {
            val zone = ZoneId.systemDefault()
            val nextMidnight = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant()
            delay(Duration.between(Instant.now(), nextMidnight).toMillis().coerceAtLeast(1L))
            tick++
        }
    }
    return tick
}

/** Whitespace split works for both Latin and Arabic runs. */
fun wordCount(text: String): Int =
    if (text.isBlank()) 0 else text.trim().split(WHITESPACE).count { it.isNotBlank() }

private fun formatClock(context: Context, locale: Locale, timestamp: Long): String {
    val skeleton = if (PlatformDateFormat.is24HourFormat(context)) "Hm" else "hm"
    return SimpleDateFormat(
        PlatformDateFormat.getBestDateTimePattern(locale, skeleton),
        locale
    ).format(Date(timestamp))
}

private val WHITESPACE = Regex("\\s+")
