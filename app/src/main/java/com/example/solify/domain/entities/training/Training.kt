package com.example.solify.domain.entities.training

data class Training(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val category: TrainingCategory,
    val trainers: List<Trainer>
)
enum class TrainingCategory { EAR, RHYTHM }