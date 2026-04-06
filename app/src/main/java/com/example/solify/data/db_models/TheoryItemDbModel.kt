package com.example.solify.data.db_models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "theory_items",
    foreignKeys = [ForeignKey(
        entity = LessonDbModel::class,
        parentColumns = ["id"],
        childColumns = ["lessonId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["lessonId"])
    ]
)
data class TheoryItemDbModel(
    @PrimaryKey
    val id: String,
    val lessonId: String,
    val title: String,
    val description: String
)