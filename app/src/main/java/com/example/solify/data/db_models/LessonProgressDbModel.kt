package com.example.solify.data.db_models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lesson_progress",
    foreignKeys = [
        ForeignKey(
            entity = UserDbModel::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LessonDbModel::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId", "lessonId"], unique = true),
        Index(value = ["lessonId"]),
        Index(value = ["userId"])
    ]
)
data class LessonProgressDbModel(
    @PrimaryKey
    val id: Long = 0,
    val userId: String,
    val lessonId: String,
    val completedTests: String  // "testId1,testId2,testId3"
)