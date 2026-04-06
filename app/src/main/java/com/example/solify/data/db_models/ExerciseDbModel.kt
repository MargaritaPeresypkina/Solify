package com.example.solify.data.db_models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    foreignKeys = [ForeignKey(
        entity = TrainingDbModel::class,
        parentColumns = ["id"],
        childColumns = ["trainingId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["trainingId"])
    ]
    )
data class ExerciseDbModel(
    @PrimaryKey
    val id: String,
    val trainingId: String,
    val text: String,
    val audio: String,
    val correctAnswerId: String
)
