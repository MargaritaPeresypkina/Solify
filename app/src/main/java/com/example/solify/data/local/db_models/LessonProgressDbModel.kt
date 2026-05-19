package com.example.solify.data.local.db_models

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
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val lessonId: String,
    val completedTests: List<String>,  // "testId1,testId2,testId3"
    val pendingTests: List<String>     // "testId5,testId6,testId4"
)