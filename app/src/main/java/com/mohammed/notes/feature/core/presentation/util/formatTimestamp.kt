package com.mohammed.notes.feature.core.presentation.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val moreThanWeek = moreThanWeek(timestamp)
    val moreThanDay = moreThanDay(timestamp)

    val formatter = if(!moreThanDay){
        SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    }else if (moreThanWeek){
        SimpleDateFormat("dd MMM" , Locale.ENGLISH)
    } else{
        SimpleDateFormat("EEEE hh:mm a", Locale.ENGLISH)
    }
    return formatter.format(date)
}

private fun moreThanWeek(timestamp: Long): Boolean {
    val now = System.currentTimeMillis()
    val finalWeek = now - timestamp
    return finalWeek.toFloat() / 1000 / 60 / 60 / 24 / 7 > 1
}

private fun moreThanDay(timestamp: Long): Boolean {
    val now = System.currentTimeMillis()
    val finalDay = now - timestamp
    return finalDay.toFloat() / 1000 / 60 / 60 / 24 > 1
}

