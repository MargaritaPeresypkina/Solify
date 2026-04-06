package com.example.solify.domain.entities.progress

data class TestProgress(
    val testId: String,
    val completedQuestions: Set<String>, //QuestionId
    val pendingQuestions: List<String> //QuestionId
)