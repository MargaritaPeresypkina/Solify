package com.example.solify.domain.entities.progress

data class TestsCompletionOverview(
    val percent: Int = 0,
    val completedTests: Int = 0,
    val totalTests: Int = 0
)
