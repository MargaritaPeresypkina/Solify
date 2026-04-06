package com.example.solify.data.db_models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "test_progress",
    foreignKeys = [
        ForeignKey(
            entity = UserDbModel::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TestDbModel::class,
            parentColumns = ["id"],
            childColumns = ["testId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId", "lessonId", "testId"], unique = true),
        Index(value = ["userId", "lessonId"]),
        Index(value = ["testId"]),
        Index(value = ["lessonId"])
    ]
)
data class TestProgressDbModel(
    @PrimaryKey
    val id: Long = 0,
    val userId: String,
    val lessonId: String,
    val testId: String,
    val completedQuestions: String,  // "q1,q2,q3"
    val pendingQuestions: String     // "q4,q5,q6" - важен порядок!
)