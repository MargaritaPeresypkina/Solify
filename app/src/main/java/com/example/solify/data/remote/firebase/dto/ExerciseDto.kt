package com.example.solify.data.remote.firebase.dto

data class ExerciseDto(
    val text: String = "",
    val audio: String = "",
    val correctOptionId: String = "",
    val trainerId: String = ""
)

data class ExerciseAnswerOptionDto(
    val text: String? = null,
    val image: String? = null
)
