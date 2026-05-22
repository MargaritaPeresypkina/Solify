package com.example.solify.data.remote.firebase.dto

data class QuestionDto(
    val text: String = "",
    val imageUrl: String? = null,
    val hint: String = "",
    val correctOptionId: String = "",
    val testId: String = ""
)

data class AnswerOptionDto(
    val text: String = ""
)
