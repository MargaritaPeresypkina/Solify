package com.example.solify.domain.entities.progress

data class UserProgress(
    val userId: String,
    val completedLessons: Set<String>, //LessonId
    val completedExercisesCount: Int = 0,
)