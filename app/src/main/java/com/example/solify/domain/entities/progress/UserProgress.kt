package com.example.solify.domain.entities.progress

data class UserProgress(
    val userId: String,
    val completedLessons: Set<String>, //LessonId
    //val lessonProgress: Map<String, LessonProgress> //LessonId, LessonProgress
)