package com.example.solify.domain.entities.progress

data class TestsCompletionSnapshot(
    val completedFromLessonProgress: Int = 0,
    val completedFromLocalLessonProgress: Int = 0,
    val testProgressDocumentCount: Int = 0,
    val testProgressCompletedLikeCount: Int = 0
)
