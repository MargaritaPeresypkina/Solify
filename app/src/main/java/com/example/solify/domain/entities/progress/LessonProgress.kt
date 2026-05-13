package com.example.solify.domain.entities.progress


data class LessonProgress(
    val lessonId: String,
    val completedTests: Set<String> = emptySet(),
    val pendingTests: List<String> = emptyList()
)