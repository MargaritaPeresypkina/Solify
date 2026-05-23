package com.example.solify.data.remote.firebase.dto

data class ExerciseProgressDto(
    val trainerId: String = "",
    val completedExercises: List<String> = emptyList(),
    val pendingExercises: List<String> = emptyList(),
    val status: String = "NOT_STARTED"
)
