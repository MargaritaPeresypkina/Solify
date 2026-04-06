package com.example.solify.data.db_models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_answer_options",
    foreignKeys = [ForeignKey(
        entity = ExerciseDbModel::class,
        parentColumns = ["id"],
        childColumns = ["exerciseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["exerciseId"])
    ]
    )
data class ExerciseAnswerOptionDbModel(
    @PrimaryKey
    val id: String,
    val exerciseId: String,
    val text: String,
    val image: String?
)