package com.example.solify.data.db_models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "answer_options",
    foreignKeys = [ForeignKey(
        entity = QuestionDbModel::class,
        parentColumns = ["id"],
        childColumns = ["questionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["questionId"])
    ]
)
data class AnswerOptionDbModel(
    @PrimaryKey
    val id: String,
    val questionId: String,
    val text: String
)