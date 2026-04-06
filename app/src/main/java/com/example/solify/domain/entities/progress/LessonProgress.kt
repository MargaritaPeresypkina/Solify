package com.example.solify.domain.entities.progress


data class LessonProgress(
    val lessonId: String,
    val completedTests: Set<String>, //TestId
    //val testProgress: Map<String, TestProgress> // TestId
)