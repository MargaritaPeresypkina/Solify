package com.example.solify.data.local.db_models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trainers",
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
data class TrainerDbModel(
    @PrimaryKey
    val id: String,
    val trainingId: String,
    val title: String,
    val description: String,
    val exercisesIds: List<String> = emptyList()
)
