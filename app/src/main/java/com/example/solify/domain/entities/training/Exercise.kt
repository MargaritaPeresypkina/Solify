package com.example.solify.domain.entities.training

data class Exercise(
    val id: String,
    val text: String,
    val audio: String,
    val options: List<ExercisesAnswerOption>,
    val correctAnswerId: String
)
