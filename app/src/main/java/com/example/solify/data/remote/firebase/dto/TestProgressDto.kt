package com.example.solify.data.remote.firebase.dto

class TestProgressDto(
    val testId: String = "",
    val completedQuestions: Set<String> = emptySet(),
    val pendingQuestions: List<String> = emptyList()
)