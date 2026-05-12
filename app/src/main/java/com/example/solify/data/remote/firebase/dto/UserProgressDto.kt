package com.example.solify.data.remote.firebase.dto

data class UserProgressDto(
    val completedLessons: List<String> = emptyList()
)