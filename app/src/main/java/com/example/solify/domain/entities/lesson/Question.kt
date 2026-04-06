package com.example.solify.domain.entities.lesson


data class Question(
    val id: String,
    val text: String,
    val imageUrl: String?,
    val options: List<AnswerOption>,
    val correctOptionId: String,
    val hint: String
)