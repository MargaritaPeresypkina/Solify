package com.example.solify.data.remote.firebase.dto

class TestProgressDto(
    val testId: String = "",
    val completedQuestions: List<String> = emptyList(),
    val pendingQuestions: List<String> = emptyList(),
    val status: String = "NOT_STARTED"
)