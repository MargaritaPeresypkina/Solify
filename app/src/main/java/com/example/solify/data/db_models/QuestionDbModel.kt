package com.example.solify.data.db_models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    foreignKeys = [ForeignKey(
        entity = TestDbModel::class,
        parentColumns = ["id"],
        childColumns = ["testId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["testId"])
    ]
)
data class QuestionDbModel(
    @PrimaryKey
    val id: String,
    val testId: String,
    val text: String,
    val imageUrl: String?,
    val hint: String,
    val correctOptionId: String
)