package com.example.solify.data.remote.firebase.dto

data class TrainerDto(
    val id: String = "",
    val description: String = "",
    val title: String = "",
    val exercisesIds: List<String> = emptyList()
)
