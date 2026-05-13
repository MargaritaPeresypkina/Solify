package com.example.solify.data.remote.firebase.dto

data class LessonProgressDto(
    val lessonId: String = "",
    val completedTests: List<String> = emptyList(),
    val pendingTests: List<String> = emptyList()
)