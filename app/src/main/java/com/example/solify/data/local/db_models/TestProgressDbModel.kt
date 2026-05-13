package com.example.solify.data.local.db_models

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
        Index(value = ["userId", "testId"], unique = true),
        Index(value = ["userId", ]),
        Index(value = ["testId"])
    ]
)
data class TestProgressDbModel(
    @PrimaryKey
    val id: Long = 0,
    val userId: String,
    val testId: String,
    val completedQuestions: List<String>,  // "q1,q2,q3"
    val pendingQuestions: List<String>     // "q4,q5,q6" - важен порядок!
)