package com.example.solify.domain.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DailyActivityDates {
    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun todayKey(): String = formatKey(Calendar.getInstance())

    fun formatKey(calendar: Calendar): String = dateKeyFormat.format(calendar.time)

    fun dayLabel(dateKey: String): String {
        val calendar = parseKey(dateKey)
        val locale = Locale.getDefault()
        return SimpleDateFormat("EEE", locale).format(calendar.time)
            .replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(locale) else char.toString()
            }
    }

    fun lastSevenDayKeys(): List<String> {
        val today = Calendar.getInstance()
        return (6 downTo 0).map { daysAgo ->
            val day = today.clone() as Calendar
            day.add(Calendar.DAY_OF_YEAR, -daysAgo)
            formatKey(day)
        }
    }

    fun sevenDaysAgoKey(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        return formatKey(calendar)
    }

    private fun parseKey(dateKey: String): Calendar {
        val parsed = dateKeyFormat.parse(dateKey)
            ?: throw IllegalArgumentException("Invalid date key: $dateKey")
        return Calendar.getInstance().apply { time = parsed }
    }
}
