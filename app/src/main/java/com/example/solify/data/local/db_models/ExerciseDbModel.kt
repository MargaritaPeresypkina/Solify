package com.example.solify.data.local.db_models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    foreignKeys = [ForeignKey(
        entity = TrainerDbModel::class,
        parentColumns = ["id"],
        childColumns = ["trainerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["trainerId"])
    ]
)
data class ExerciseDbModel(
    @PrimaryKey
    val id: String,
    val trainerId: String,
    val text: String,
    val audio: String,
    val correctOptionId: String
)
