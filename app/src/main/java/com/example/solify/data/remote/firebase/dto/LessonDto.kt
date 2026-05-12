package com.example.solify.data.remote.firebase.dto

data class LessonDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val level: String = "", // BEGINNER, INTERMEDIATE, ADVANCED
    val order: Int = 0
)