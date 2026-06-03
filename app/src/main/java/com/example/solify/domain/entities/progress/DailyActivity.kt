package com.example.solify.domain.entities.progress

data class DailyActivity(
    val date: String,
    val completedTestsCount: Int
)

data class WeeklyActivityDay(
    val date: String,
    val dayLabel: String,
    val completedTestsCount: Int
)
