package com.example.solify.data.local.db_models

import com.example.solify.domain.entities.progress.Status
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "test_progress",
    indices = [
        Index(value = ["userId", "testId"], unique = true),
        Index(value = ["userId"]),
        Index(value = ["testId"])
    ]
)
data class TestProgressDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val testId: String,
    val completedQuestions: List<String>,  // "q1,q2,q3"
    val pendingQuestions: List<String>,    // "q4,q5,q6" - важен порядок!
    val status: String = Status.NOT_STARTED.name
)