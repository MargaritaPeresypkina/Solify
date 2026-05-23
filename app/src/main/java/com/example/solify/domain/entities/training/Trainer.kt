package com.example.solify.domain.entities.training

data class Trainer(
    val id: String,
    val description: String,
    val title: String,
    val exercisesIds: List<String>,
    val order: Int = 0
)
