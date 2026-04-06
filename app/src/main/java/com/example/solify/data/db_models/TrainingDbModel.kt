package com.example.solify.data.db_models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trainings")
data class TrainingDbModel(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val category: String, // EAR, RHYTHM
)