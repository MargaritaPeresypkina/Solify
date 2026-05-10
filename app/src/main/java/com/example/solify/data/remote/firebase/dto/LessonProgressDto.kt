package com.example.solify.data.remote.firebase.dto

data class LessonProgressDto(
    val lessonId: String = "",
    val completedTests: Set<String> = emptySet()
)