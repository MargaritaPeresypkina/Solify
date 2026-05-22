package com.example.solify.domain.entities.progress

data class TestProgress(
    val testId: String,
    val completedQuestions: Set<String> = emptySet(),
    val pendingQuestions: List<String> = emptyList(),
    val lessonId: String? = null,
    val status: Status = Status.NOT_STARTED
)
