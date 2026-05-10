package com.example.solify.data.remote.firebase.dto

data class UserProgressDto(
    val completedLessons: Set<String> = emptySet()
)