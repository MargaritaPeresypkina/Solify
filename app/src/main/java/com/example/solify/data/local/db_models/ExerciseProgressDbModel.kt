package com.example.solify.data.local.db_models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.solify.domain.entities.progress.Status

@Entity(
    tableName = "exercise_progress",
    indices = [
        Index(value = ["userId", "trainerId"], unique = true),
        Index(value = ["userId"]),
        Index(value = ["trainerId"])
    ]
)
data class ExerciseProgressDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val trainerId: String,
    val completedExercises: List<String>,
    val pendingExercises: List<String>,
    val status: String = Status.NOT_STARTED.name
)
